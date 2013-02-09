

module memchan


    use mersenne
    use matrix

    implicit none

    private
    public :: chanpop, gcpop, synpop, fill_matrices, fill_cumulative_matrices, memchan_init, memchan_advance
    public :: memsyn_init, memsyn_advance, memsyn_event,  fill_decay_factors

    ! population of a single channel type across the cell: positions (compartment
    ! number) and numbers of channels for compartments with channels
    ! stochastic and continuous populations are handled separately. One may be empty
    type chanpop
        integer :: altid
        integer :: totninst

        real :: erev
        real :: gbase
        real :: vmin
        real :: vstep
        integer :: nv
        integer :: ngc
        logicaL :: onebyone = .false.
        type(gcpop), dimension(:), pointer :: gcs

        integer :: ncont_cpts
        integer, dimension(:), pointer :: num_cont
        integer, dimension(:), pointer :: pos_cont

        integer :: nstoch_cpts
        integer, dimension(:), pointer :: num_stoch
        integer, dimension(:), pointer :: pos_stoch


        integer :: nrec = 0
        integer, dimension(3,100) :: prec
        ! second index is 1,nrec;  first is  recorder index, pos in cont array, pos in stoch array
        real, dimension(100) :: recwk
    end type chanpop


    ! matrices and states. There is one gcpop for each gating complex in the
    ! channel type.  The state arrays hold the number of channels in each state
    ! for each compartment on which they occur in that form (stochastic or continuous)
    type gcpop
        integer :: nstates
        integer :: ninstances
        real, dimension(:), pointer :: grel
        real, dimension(:,:,:), pointer :: matrices
        real, dimension(:,:,:,:), pointer :: destprob
        integer, dimension(:,:,:), pointer :: destindex
        real, dimension(:,:), pointer :: states_cont
        integer, dimension(:,:), pointer :: states_stoch
        real, dimension(:), pointer :: evec
    end type gcpop



     type synpop
        character*12 :: id
        integer :: numid
        integer :: ndecays
        integer :: nrises
        real :: normalization
        real :: gbase
        real :: erev
        real, dimension(6) :: fdec
        real, dimension(6) :: tdec
        real :: trise

        real, dimension(6) :: decfactors
        real :: risefactor

        integer :: nsyn
        integer :: ncpt
        integer, dimension(:), pointer :: nums
        integer, dimension(:), pointer :: dests

        real, dimension(:,:), pointer :: xdecs
        real, dimension(:), pointer :: xrise
        real, dimension(:), pointer :: weights

        logical :: onewieght ! currently unused
        ! TODO - there's a big optimization that could be done if the weights are all the same -
        ! you only need one xdecs, xrise per compartment, rather than one pers synapse
    end type synpop



    real, dimension(300000), save :: ranwk

contains



    subroutine fill_matrices(nstates, nv, ntrans, trans_from, trans_to, rates, dt, vmin, vstep, v0,   evec, m)
        integer, intent(in) :: nstates, nv, ntrans
        integer, dimension(ntrans), intent(in) :: trans_from, trans_to
        real, dimension(2 * ntrans, nv), intent(in) :: rates
        real, intent(in) ::  vmin, vstep, dt, v0
        real, dimension(nstates), intent(out) :: evec
        real, dimension(nstates, nstates, nv), intent(out) :: m

        real, dimension(nstates, nstates) :: w

        integer :: itrans, iv, ia, ib, ic, ind
        real :: fwd, rev, f, wf

        do iv = 1, nv
            w = 0
            do itrans = 1, ntrans
                ia = trans_from(itrans) + 1
                ib = trans_to(itrans) + 1
                fwd = rates(2 * itrans-1, iv)
                rev = rates(2 * itrans, iv)

                w(ia, ia) = w(ia, ia) - fwd
                w(ia, ib) = w(ia, ib) + rev

                w(ib, ib) = w(ib, ib) - rev
                w(ib, ia) = w(ib, ia) + fwd
             end do


            w = expmatrix(nstates, dt * w)
            do ic = 1, nstates
                w(:,ic) = w(:,ic) / sum(w(:,ic))
            end do
            m(:,:,iv) = w
        end do

        ! now for the eigenvector at the starting potential
        w = 0


       call getindex(vmin, 1. / vstep, nv, v0, ind, f, wf)


        do itrans = 1, ntrans
            ia = trans_from(itrans) + 1
            ib = trans_to(itrans) + 1
            fwd = wf * rates(2 * itrans-1, ind) + f * rates(2 * itrans-1, ind+1)
            rev = wf * rates(2 * itrans, ind) + f * rates(2 * itrans, ind+1)

            w(ia, ia) = w(ia, ia) - fwd
            w(ia, ib) = w(ia, ib) + rev

            w(ib, ib) = w(ib, ib) - rev
            w(ib, ia) = w(ib, ia) + fwd
       end do
       w = expmatrix(nstates, 1. * w)
       do ic = 1, nstates
            w(:,ic) = w(:,ic) / sum(w(:,ic))
       end do
       evec = ev1vec(nstates, w, 16)


    end subroutine fill_matrices




    subroutine fill_cumulative_matrices(ns, nv, m, cm, ocm)
        integer, intent(in) :: ns, nv
        real, dimension(ns, ns, nv), intent(in) :: m
        real, dimension(2, ns, ns, nv), intent(out) :: cm
        integer, dimension(ns, ns, nv), intent(out) :: ocm

        real, dimension(ns) :: col, colb
        integer, dimension(ns) :: isrt
        real :: xa, xb

        integer :: iv, isrc, j


        do iv = 1, nv
            do isrc = 1, ns
                col = m(:, isrc, iv)
                if (iv .lt. nv) then
                    colb = m(:, isrc, iv+1)
                else
                    colb = m(:, isrc, iv)
                end if

                call sort_indexes(col, isrt)


                ocm(:, isrc, iv) = isrt

                xa = 0.
                xb = 0.
                do j = 1, ns
                    cm(1, j, isrc, iv) = xa
                    cm(2, j, isrc, iv) = xb
                    xa = xa + col(isrt(j))
                    xb = xb + colb(isrt(j))
                end do
            end do
        end do

    end subroutine fill_cumulative_matrices




    subroutine memchan_init(v0, pops)
        real, intent(in) :: v0
        type(chanpop), dimension(:), intent(inout) :: pops
        integer :: icp

        call array_grnd(size(ranwk), ranwk)

        do icp = 1, size(pops)
            call init_population(pops(icp), v0)
        end do
    end subroutine memchan_init






    subroutine getindex(vmin, rvs, nv, v, ind, f, wf)
         real, intent(in) :: vmin, rvs, v
         integer, intent(in) :: nv
         integer, intent(out) :: ind
         real, intent(out) :: f, wf
         real :: vup
         vup = v - vmin
         if (vup .lt. 0) vup = 0
         ind = floor(vup * rvs)
         f = (rvs * vup - ind)
         ind = ind + 1
         if (ind .gt. nv-1) then
             ind = nv-1
             f = 1.
         end if
         wf = 1. - f
    end subroutine getindex




    subroutine init_population(pop, v)
        type(chanpop), intent(inout) :: pop
         real, intent(in) :: v
        real, dimension(:), allocatable :: evec
        real, dimension(:), allocatable :: nch
        integer :: nstates, igc, icpt, j, p, ind
        real :: f, wf
        integer :: nneed, ngot

        call getindex(pop%vmin, 1. / pop%vstep, pop%nv, v, ind, f, wf)

        do igc = 1, pop%ngc
            nstates = pop%gcs(igc)%nstates
            allocate(evec(nstates))
            allocate(nch(nstates))
            nch = 0


            evec = pop%gcs(igc)%evec

!            evec = wf * ev1vec(nstates, pop%gcs(igc)%matrices(:,:,ind), 16) + &
!                    f * ev1vec(nstates, pop%gcs(igc)%matrices(:,:,ind+1), 16)



            forall (icpt = 1:pop%ncont_cpts)
                pop%gcs(igc)%states_cont(:,icpt) = evec
            end forall

            nneed = 0
            ngot = 0
            do icpt = 1, pop%nstoch_cpts
                nneed = nneed + pop%num_stoch(icpt)
                !nch = floor(evec * pop%num_stoch(icpt))
                ! above floor will give too few channels on some compartments.
                ! Alllocate the remainder to states randomly according to
                ! the evec weightings
                nch = 0
                do j = 1, pop%num_stoch(icpt) - nint(sum(nch))
                    p = random_index(nstates, evec)
                    nch(p) = nch(p) + 1
                end do


                ngot = ngot + sum(nch)
                pop%gcs(igc)%states_stoch(:,icpt) = nch

            end do

            if (nneed .ne. ngot) then
                print *, "ERROR - channel allocation miscount ", nneed, ngot
                stop
            end if


            deallocate(evec, nch)
        end do
    end subroutine init_population



    subroutine memchan_advance(ncompartments, v, chanpops, gchan, echan)
        integer, intent(in) :: ncompartments
        real, dimension(ncompartments), intent(in) :: v
        type(chanpop), dimension(:) :: chanpops
        real, dimension(ncompartments), intent(inout) :: gchan, echan

        integer :: icp

        do icp = 1, size(chanpops)
            call advance_population(chanpops(icp), ncompartments, v, gchan, echan)
        end do

    end subroutine memchan_advance





    subroutine advance_population(pop, ncompartments, v, gchan, echan)
        type(chanpop), intent(inout) :: pop
        integer, intent(in) :: ncompartments
        real, dimension(ncompartments), intent(in) :: v
        real, dimension(ncompartments), intent(inout) :: gchan, echan

        real, dimension(ncompartments) ::  gwk
        integer :: ncpts, nstates, igc, ic, i, ipos


        do i = 1, pop%nrec
           pop%recwk(i) = 0
        end do

        ncpts = pop%ncont_cpts
        if (ncpts .gt. 0) then
            gwk(1:ncpts) = pop%gbase * pop%num_cont
            do igc = 1, pop%ngc
                nstates = pop%gcs(igc)%nstates
                call gc_contin(ncompartments, ncpts, v, pop%pos_cont, nstates, pop%gcs(igc)%grel,   &
                               pop%vmin, pop%vstep, pop%nv, pop%gcs(igc)%ninstances, &
                               pop%gcs(igc)%matrices, pop%gcs(igc)%states_cont, gwk(1:ncpts))
            end do


          do i = 1, ncpts
              ic = pop%pos_cont(i)
              gchan(ic) = gchan(ic) + gwk(i)
              echan(ic) = echan(ic) + gwk(i) * pop%erev
          end do




         do i = 1, pop%nrec
              ipos = pop%prec(2, i)
              if (ipos .gt. 0) pop%recwk(i) = gwk(ipos)
         end do

        !    gchan(pop%pos_cont) = gchan(pop%pos_cont) + gwk(1:ncpts)
        !    echan(pop%pos_cont) = echan(pop%pos_cont) + gwk(1:ncpts) * pop%erev
        end if

        ncpts = pop%nstoch_cpts
        if (ncpts .gt. 0) then
            if (pop%ngc > 1) then
                print *, "ERROR - stochastic update only works for single-complex channels"
                stop
            end if

            gwk(1:ncpts) = pop%gbase
            do igc = 1, pop%ngc
                nstates = pop%gcs(igc)%nstates

                call gc_stoch(ncompartments, ncpts, v, pop%pos_stoch, nstates, pop%gcs(igc)%grel,   &
                              pop%vmin, pop%vstep, pop%nv,  pop%onebyone, &
                              pop%gcs(igc)%destindex, pop%gcs(igc)%destprob, &
                              pop%gcs(igc)%states_stoch, gwk(1:ncpts))

!				call gc_stoch_mtx(ncompartments, ncpts, v, pop%pos_stoch, nstates, pop%gcs(igc)%grel,   &
!                			  pop%vmin, pop%vstep, pop%nv,   pop%gcs(igc)%matrices, &
!                			  pop%gcs(igc)%states_stoch, gwk(1:ncpts))



            end do

            gchan(pop%pos_stoch) = gchan(pop%pos_stoch) + gwk(1:ncpts)
            echan(pop%pos_stoch) = echan(pop%pos_stoch) + gwk(1:ncpts) * pop%erev

            do i = 1, pop%nrec
                ipos = pop%prec(3, i)
                if (ipos .gt. 0) pop%recwk(i) = pop%recwk(i) + gwk(ipos)
            end do
        end if

    end subroutine advance_population



    ! single complex continuous channel update
    subroutine gc_contin(ncompartments, ncpts, v, pos, nstates, grel, vmin, vstep, nv, ninst, m, h, geff)
        integer, intent(in) :: ncompartments, ncpts, nstates, nv, ninst
        real, dimension(ncompartments), intent(in) :: v
        integer, dimension(ncpts), intent(in) :: pos
        real, dimension(nstates), intent(in) :: grel
        real, intent(in) :: vmin, vstep
        real, dimension(nstates, nstates, nv), intent(in) :: m
! 		real, dimension(2, 2, 100), intent(in) :: m
        real, dimension(nstates, ncpts), intent(inout) :: h  ! population states
!		real, dimension(2, 13) :: h
        real, dimension(ncpts), intent(inout) :: geff

        integer :: i, j, k, ind, ipos
        real :: f, wf, a, b, vup, rvs
        real, dimension(10) :: wk


! PERFORMANCE - the first, data parallel, version takes twice as long as the equivalent do loop
! with gfortran (any optimisation level) and the do loop with matmul takes more than twice as long as
! the explicit oeprations

!        forall (i = 1:ncpts)
!            h(:,i) = (1. - fmx(i)) * matmul(m(:,:,imx(i)), h(:,i)) +     &
!                            fmx(i) * matmul(m(:,:,imx(i)+1), h(:,i))
!        end forall

! 		do i = 1, ncpts
!            h(:,i) = (1. - fmx(i)) * matmul(m(:,:,imx(i)), h(:,i)) +     &
!                            fmx(i) * matmul(m(:,:,imx(i)+1), h(:,i))
!        end do


        rvs = 1. / vstep

        if (nstates .eq. 2) then
            do i = 1, ncpts
                 ipos = pos(i)

                ! call getindex(vmin, rvs, nv, v(ipos), ind, f, wf)
                ! inlining this manually makes a > 10% difference ot test case performance...
                vup = v(ipos) - vmin
                if (vup .lt. 0) vup = 0
                ind = floor(rvs * vup)
                f = (rvs * vup - ind)
                ind = ind + 1
                if (ind .gt. nv-1) then
                    ind = nv-1
                    f = 1.
                end if
                wf = 1. - f

                 a = (wf * m(1,1,ind) + f * m(1,1,ind+1)) * h(1,i) +  &
                     (wf * m(1,2,ind) + f * m(1,2,ind+1)) * h(2,i)
                 b = (wf * m(2,1,ind) + f * m(2,1,ind+1)) * h(1,i) +  &
                     (wf * m(2,2,ind) + f * m(2,2,ind+1)) * h(2,i)

                h(1,i) = a
                h(2,i) = b
            end do
        else
        do i = 1, ncpts
            ipos = pos(i)

            call getindex(vmin, rvs, nv, v(ipos), ind, f, wf)

            do j = 1, nstates
               wk(j) = 0
               do k = 1, nstates
                    wk(j) = wk(j) + (wf * m(j,k,ind) + f * m(j,k,ind+1)) * h(k,i)
                end do
            end do
            do j = 1, nstates
               h(j,i) = wk(j)
            end do
        end do
        end if

        if (ninst .le. 1) then
            geff = geff * matmul(grel, h)
        else
            geff = geff * matmul(grel, h)**ninst
        end if

    end subroutine gc_contin



    subroutine gc_stoch(ncompartments, ncpts, v, pos, nstates, grel, vmin, vstep, nv, obo, &
    idest, pdest, h, geff)
        integer, intent(in) :: ncompartments, ncpts, nstates, nv
        logical, intent(in) :: obo
        real, dimension(ncompartments), intent(in) :: v
        integer, dimension(ncpts), intent(in) :: pos
        real, dimension(nstates), intent(in) :: grel
        real, intent(in) :: vmin, vstep
        real, dimension(2, nstates, nstates, nv), intent(in) :: pdest
        integer, dimension(nstates, nstates, nv), intent(in) :: idest
        integer, dimension(nstates, ncpts), intent(inout) :: h
        real, dimension(ncpts), intent(inout) :: geff

        integer :: iel, ipos, ind, isrc, ichan, istat, ir, id, npos, ntot, irn, nranmax
        real :: f, wf, ln, ppow
        real :: r

        integer, dimension(20) :: wk
        real, dimension(20) :: col

        real :: rvs, fs, ff

       
        nranmax = size(ranwk)

        rvs = 1. / vstep
        col = 0.
        do iel = 1, ncpts
            ipos = pos(iel)

            call getindex(vmin, rvs, nv, v(ipos), ind, f, wf)


            wk = 0

            irn = 1
            do isrc = 1, nstates
                do ir = 2, nstates
                    col(ir) = f * pdest(2, ir, isrc, ind) + wf * pdest(1, ir, isrc, ind)
                end do
                ntot = h(isrc, iel)
                if (ntot .gt. 0) then
                    npos = ntot
                    fs = 1.
                    ff = col(nstates)


                    if ((.not. obo) .and. (ntot .gt. 50) .and. (ff .lt. 0.2)) then
                       if (ntot .gt. 2000) then
                       !	TODO could get a better fit than this
                         npos = ntot * ff + 5 * sqrt(ntot * ff)


                      else if (ff .lt. 0.003) then
                           npos = 4 + 8 * sqrt(ntot * ff);

                      else
                       ! the following is an empirical fit to calculate npos such that we only miss one in 10^7 or fewer
                        ln = log10(1. * ntot)
                    !  ppow = 0.26 + 0.19 * ln
                    !  npos = 3 * ln + ntot * ff**ppow

                    ! following fits better but costs more
                      ppow = 0.5 + 0.12 * ln
                      npos =   6 + 5 *  ln + ntot * ff**ppow +  4* log10(ff)

                      end if

                      if (npos .gt. ntot) npos = ntot
                      fs =  (1. * npos) / ntot
                      id = idest(nstates, isrc, ind)
                      wk(id) = wk(id) + (ntot - npos)
                    end if

                    if (irn + npos .ge.  nranmax) then
                          print *, "ERROR - overran random number work array: ", irn, "/", size(ranwk)
                          print *, "recompile with a larger declaration for ranwk in memchan.f90"
                          stop
                    end if

                     do ichan = 1, npos
                        r = fs * ranwk(irn + ichan)
                        do ir = nstates, 1, -1
                             if (r .ge.  col(ir)) exit
                         end do
                         id = idest(ir, isrc, ind)
                        wk(id) = wk(id) + 1
                    end do
                     irn = irn + npos
                  end if
            end do

            do istat = 1, nstates
                h(istat, iel) = wk(istat)
            end do

            if (irn .gt.  size(ranwk)) then
                ! probably got a seg falut by now, but just in case
                print *, "ERROR - overran random number work array: ", irn, "/", size(ranwk)
                print *, "recompile with a larger declaration for ranwk in memchan.f90"
                stop
            end if
            call array_grnd(irn, ranwk)

        end do

        geff = geff * matmul(grel, h)
    end subroutine gc_stoch



    subroutine dumpstates(nstates, ncpts, h)
       integer, intent(in) :: nstates, ncpts
       integer, dimension(nstates, ncpts), intent(in) :: h
       integer, dimension(nstates) :: nps
       integer :: icpt, istat

       do istat = 1, nstates
             nps(istat) = 0
       end do

       do icpt = 1, ncpts
              do istat = 1, nstates
                  nps(istat) = nps(istat) + h(istat, icpt)
              end do
       end do

    end subroutine dumpstates



  subroutine gc_stoch_mtx(ncompartments, ncpts, v, pos, nstates, grel, vmin, vstep, nv, m, h, geff)
        integer, intent(in) :: ncompartments, ncpts, nstates, nv
        real, dimension(ncompartments), intent(in) :: v
        integer, dimension(ncpts), intent(in) :: pos
        real, dimension(nstates), intent(in) :: grel
        real, intent(in) :: vmin, vstep
        real, dimension(nstates, nstates, nv), intent(in) :: m
        integer, dimension(nstates, ncpts), intent(inout) :: h
        real, dimension(ncpts), intent(inout) :: geff

        integer :: iel, ipos, ind, isrc, ichan, istat, ir
        real :: f, wf
        real :: r

        integer, dimension(20) :: wk
        real, dimension(20) :: col

        real :: rvs

        rvs = 1. / vstep
        col = 0.
        do iel = 1, ncpts
            ipos = pos(iel)

            call getindex(vmin, rvs, nv, v(ipos), ind, f, wf)
            wk = 0

            do isrc = 1, nstates
                do ir = 1, nstates
                    col(ir) = f * m(ir, isrc, ind+1) + wf * m(ir, isrc, ind)
                end do
                call array_grnd(h(isrc, iel), ranwk)

                do ichan = 1, h(isrc, iel)
                    r = ranwk(ichan)
                    do ir = 1, nstates
                          r = r - col(ir)
                         if (r .le. 0) exit
                     end do
                    wk(ir) = wk(ir) + 1
                end do
            end do



            do istat = 1, nstates
                h(istat, iel) = wk(istat)
            end do


        end do
        geff = geff * matmul(grel, h)

    end subroutine gc_stoch_mtx


    function arraysum(n, a)
        integer, intent(in) :: n
        real, dimension(n), intent(in) :: a
        real :: arraysum
        integer :: i

        arraysum = 0
        do i = 1, n
            arraysum = arraysum + a(i)
        end do
    end function arraysum


    subroutine fill_decay_factors(pops, dt)
        type(synpop), dimension(:), intent(inout) :: pops
        real, intent(in) ::  dt
        integer :: icp

        do icp = 1, size(pops)
            call fill_pop_decays(pops(icp), dt)
         end do
    end subroutine fill_decay_factors


    subroutine fill_pop_decays(sp, dt)
      real, intent(in) ::  dt
      integer :: idec
      type(synpop), intent(inout) :: sp
              do idec = 1, sp%ndecays
                  sp%decfactors(idec) = exp(-1. * dt / sp%tdec(idec))
              end do
              if (sp%nrises .gt. 0) then
                  sp%risefactor = exp(-1. * dt / sp%trise)
              end if
    end subroutine fill_pop_decays



    subroutine memsyn_init(pops)
        type(synpop), dimension(:), intent(inout) :: pops
        integer :: icp, isy

        do icp = 1, size(pops)
              pops(icp)%xdecs = 0
              if (pops(icp)%nrises .gt. 0) then
                    pops(icp)%xrise = 0
               end if
               do isy = 1, pops(icp)%nsyn
                   pops(icp)%weights(isy) = pops(icp)%gbase
               end do
        end do
    end subroutine memsyn_init



  subroutine memsyn_advance(ncompartments, synpops, gchan, echan)
        integer, intent(in) :: ncompartments
        type(synpop), dimension(:), intent(inout) :: synpops
        real, dimension(ncompartments), intent(inout) :: gchan, echan

        integer :: icp


        do icp = 1, size(synpops)
            call advance_sypop(synpops(icp), ncompartments, gchan, echan)
        end do


    end subroutine memsyn_advance





    subroutine advance_sypop(pop, ncompartments, gchan, echan)
        type(synpop), intent(inout) :: pop
        integer, intent(in) :: ncompartments
        real, dimension(ncompartments), intent(inout) :: gchan, echan

        integer :: idec, id, isyn, icpt, icptsyn
        real :: g, gcpt

        do isyn = 1, pop%nsyn
            do idec = 1, pop%ndecays
                pop%xdecs(idec, isyn) = pop%xdecs(idec, isyn) * pop%decfactors(idec)
            end do
            if (pop%nrises .gt. 0) then
                pop%xrise(isyn) = pop%xrise(isyn) * pop%risefactor
            end if
        end do

        isyn = 1
        do icpt = 1, pop%ncpt
            gcpt = 0
            do icptsyn = 1, pop%nums(icpt)
                g = 0
                do idec = 1, pop%ndecays
                    g = g + pop%xdecs(idec, isyn)
                end do
                if (pop%nrises .gt. 0) then
                    g = g - pop%xrise(isyn)
                end if
                gcpt = gcpt + pop%weights(isyn) * g
                isyn = isyn + 1
             end do
             id = pop%dests(icpt)
             gchan(id) = gchan(id) + gcpt
             echan(id) = echan(id) + gcpt * pop%erev
        end do
    end subroutine advance_sypop



    subroutine memsyn_event(sp, isyn)
        type(synpop), intent(inout) :: sp
        integer, intent(in) :: isyn
        integer :: i

        do i = 1, sp%ndecays
            sp%xdecs(i, isyn) = sp%xdecs(i, isyn) + sp%normalization * sp%fdec(i)
        end do
        if (sp%nrises .gt. 0) then
            sp%xrise(isyn) = sp%xrise(isyn) + sp%normalization
        end if
    end subroutine memsyn_event



end module memchan
