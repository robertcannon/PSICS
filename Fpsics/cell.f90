
module cell

     use model
     use memchan
     use passprop
     use stimrec
     use activity

    implicit none


    ! cell structure
    integer, save :: ncompartments
    integer, save :: nconnections
    integer, save :: nfixed
    real, dimension(:), allocatable, save :: cpt_capacitance
    real, dimension(:), allocatable, save :: con_capacitance, con_conductance
    integer, dimension(:), allocatable, save :: con_from, con_to, con_fixed


    ! stimulation
    integer, save :: ncommands
    type(stimset), dimension(:), allocatable, target, save :: stimsets


    ! channel tables
    integer, save :: nchanpops
    type(chanpop), dimension(:), target, allocatable, save :: chanpops


    ! synapse tables
    integer, save :: nsynpops
    type(synpop), dimension(:), allocatable, target, save :: synpops

    integer, save :: ngenerators
    type(generator), dimension(:), allocatable, target, save :: generators


    type(stimset), pointer :: stim

    integer, save :: outstep


contains

    subroutine cell_check()
        call check_connections(ncompartments, nconnections, con_from, con_to)
    end subroutine cell_check


    subroutine cell_run(ncommands, nsteps, v0, dt, ftimediff, output_root)
        integer, intent(in) :: ncommands, nsteps
        real, intent(in) :: v0, dt, ftimediff
        character (len=128) :: output_root


        real, dimension(:), allocatable :: v, echan, gchan, chdat

        integer :: irun, istep, i, nrec, igen
        real(kind=k8) :: time
        real :: floattime
        integer :: openstat

       ! print *, "opening for unformatted output ", trim(output_root)//".dat"
        open(unit=20, file=trim(output_root)//".dat", form="unformatted", status="replace", iostat=openstat)
        if (openstat .ne. 0) then
            print *, "ERROR: cant open file ", output_root
            stop
        end if

        allocate(v(ncompartments), echan(ncompartments), gchan(ncompartments))
        write (20) ncommands

        do irun = 1, ncommands
            time = 0.
            floattime = 0.
            v = v0
            call memchan_init(v0, chanpops)
            call memsyn_init(synpops)
            stim => stimsets(irun)
            call stiminit(stim)

            do igen = 1, ngenerators
                 call init_generator(generators(igen))
            end do

! this isn't quite right - should get the conductances from the channel states TODO
            gchan = 0
            echan = 0

            nrec = stim%nvcgr
            allocate (chdat(nrec))

            ! column headings for output file
            write (20) nsteps/outstep+1, stim%nout
            write (20) (stim%outids(i), i = 1, stim%nout)

            call get_chanstates(stim, v, echan, gchan, nrec, chdat)

            if (stim%recordClamps) then
                write (20)  floattime, (gchan(stim%pvc(i)) * (v(stim%pvc(i)) - echan(stim%pvc(i))), i = 1, stim%nvc), &
                     (v(stim%pcc(i)), i = 1, stim%ncc), (v(stim%pgc(i)), i = 1, stim%ngc), &
                     (chdat(i), i = 1, stim%nvcgr)
            else
                write (20)  floattime, (chdat(i), i = 1, stim%nvcgr)
            end if


            do istep = 1, nsteps
                 ! read the stimuli into ccvals and vcvals, advancing cck, vck as necessary
                 call stimvalues(stim, time, dt)

                 v(stim%pvc) = stim%vcvals

                 gchan = 0
                 echan = 0

                 do igen = 1, ngenerators
                    call advance_generator(generators(igen), synpops(generators(igen)%destpop), dt, v(1), time)
                 end do

                 call memchan_advance(ncompartments, v, chanpops,  gchan, echan)

                 call memsyn_advance(ncompartments, synpops,  gchan, echan)

                 where (gchan .ne. 0.) echan = echan / gchan


                 ! voltage propagation
                 call vupdate(ncompartments, gchan, echan, cpt_capacitance,             &
                       nconnections, con_from, con_to, con_conductance, con_capacitance, &
                       nfixed, con_fixed, stim, dt, ftimediff,   v)
                time = time + dt

                floattime = time
                call get_chanstates(stim, v, echan, gchan, nrec, chdat)
                if (mod(istep-1, outstep) .eq. 0) then
                    if (stim%recordClamps) then
                        write (20)  floattime,        &
                         (gchan(stim%pvc(i)) * (v(stim%pvc(i)) - echan(stim%pvc(i))), i = 1, stim%nvc), &
                         (v(stim%pcc(i)), i = 1, stim%ncc), (v(stim%pgc(i)), i = 1, stim%ngc), &
                         (chdat(i), i = 1, stim%nvcgr)
                    else
                        write (20)  floattime, (chdat(i), i = 1, stim%nvcgr)
                    end if


                end if
            end do
            deallocate(chdat)
            call stimclean(stim)
        end do


        deallocate(v, echan, gchan)
        close(unit=20)

    end subroutine cell_run




    subroutine get_chanstates(stim, v, echan, gchan, nrec, chdat)
        integer, intent(in) :: nrec
        type(stimset), intent(in) :: stim
        real, dimension(:), intent(in) :: v, echan, gchan
! TODO parameterize dims
        real, dimension(nrec), intent(out) :: chdat
        type(chanpop), pointer :: pop
        real, dimension(nrec) :: recwk
        integer :: irec, icp, itgt
        real :: g


             do irec = 1, stim%nvcgr
                 recwk(irec) = 0
             end do

             do icp = 1, nchanpops
                 pop => chanpops(icp)
                 do irec = 1, pop%nrec
                    recwk(pop%prec(1, irec)) = pop%recwk(irec)
                 end do
             end do

             do irec = 1, stim%nvcgr
                  itgt = stim%pvcgr(1, irec)
                  if (stim%pvcgr(2, irec) .eq. 0) then
                    ! need current recording here
                    chdat(irec) = gchan(itgt) * (v(itgt) - echan(itgt))

                  else if (stim%pvcgr(2, irec) .eq. 1) then
                      chdat(irec) = v(itgt)
                  else
                     g = recwk(irec)
                     if (stim%pvcgr(2, irec) .eq. 3) then
                         chdat(irec) = g * (v(itgt) - chanpops(stim%pvcgr(3, irec))%erev)

                     else if (stim%pvcgr(2, irec) .eq. 4) then
                         chdat(irec) = g
                     end if
                  end if
             end do
      end subroutine get_chanstates



    ! cellinit processes the model structure from module model and converts it
    ! into the arrays used for the calculation
    subroutine cell_init(mdef)

        type(modeldef) :: mdef

        integer :: n, igc, ich, ichalt, ns, nc, ipop, icpt, icmd, ichpr, iclmp, icon, irec, nv, iout
        integer :: ntimesteps, ncont, nstoch, ncc, nvc, ngc, icmdused, icc, ivc, typ, pcont, pstoch, itgt
        integer :: ipt, igen, iid
        real :: dt

        integer, dimension(:,:), allocatable :: wknn
        integer, dimension(:,:,:), allocatable :: chwk, wknnn

        logical, dimension(:), allocatable :: wkfixed

        type(compartment), pointer :: cpt
        type(kschannel), pointer :: kschan
        type(gating_complex), pointer :: kscplx
        type(chanpop), pointer :: pop, popalt
        type(gcpop), pointer :: gc
        type(stimset), pointer :: sset
        type(clamp), pointer :: clmp
        type(synpop), pointer :: sp
        type(synapse), pointer :: syn
        type(evtgen), pointer :: evg


        ! set up the connection tables capacitance and conductance
        ncommands = mdef%ncommands
        dt = mdef%timestep
        ntimesteps = nint(mdef%runtime / dt)
        ncompartments = size(mdef%compartments)
        allocate(cpt_capacitance(ncompartments))
        outstep = mdef%iout

        n = 0
        do icpt = 1, ncompartments
            cpt => mdef%compartments(icpt)
            cpt_capacitance(icpt) = cpt%capacitance
            n = n + size(cpt%con_ids)
        end do

        ! each connection will have been counted twice
        nconnections = n / 2
        allocate(con_capacitance(nconnections))
        allocate(con_conductance(nconnections))
        allocate(con_from(nconnections))
        allocate(con_to(nconnections))


        n = 0
        do icpt = 1, ncompartments
            cpt => mdef%compartments(icpt)
            if (cpt%numid .ne. icpt-1) print *, "messed up i and numid ", icpt, cpt%numid

            do icon = 1, size(cpt%con_ids)
                if (cpt%con_ids(icon) .lt. cpt%numid) then
                    n = n + 1
                    con_from(n) = cpt%con_ids(icon) + 1
                    con_to(n) = cpt%numid + 1
                    con_conductance(n) = cpt%con_gs(icon)
                    con_capacitance(n) = 0.

     !               print *, "cpt ", icpt, " has con from ", con_from(n), " to ", con_to(n)
                endif
            end do
        end do


        nchanpops = size(mdef%kschannels)
        allocate(chanpops(nchanpops))
        do ich = 1, nchanpops
            kschan => mdef%kschannels(ich)
            pop => chanpops(ich)
            pop%erev = kschan%erev
            pop%gbase = kschan%gbase
            pop%vmin = kschan%vmin
            pop%vstep = kschan%vstep
            pop%nv = kschan%nv
            pop%onebyone = kschan%onebyone

            if (kschan%altid .ge. 0) then
                pop%altid = kschan%altid + 1
            else
                pop%altid = -1
            end if

            pop%ngc = size(kschan%complexes)
            allocate(pop%gcs(pop%ngc))

            pop%totninst = 0

            nv = pop%nv
            do igc = 1, pop%ngc
                kscplx => kschan%complexes(igc)
                gc => pop%gcs(igc)
                gc%ninstances = kscplx%ninstances
                pop%totninst = pop%totninst + gc%ninstances
                ns = size(kscplx%states)

                gc%nstates = ns
                allocate(gc%grel(ns))
                gc%grel(1:ns) = kscplx%states%grel

                allocate(gc%matrices(ns, ns, nv))
                allocate(gc%destprob(2, ns, ns, nv), gc%destindex(ns, ns, nv), gc%evec(ns))


                call fill_matrices(ns, nv, size(kscplx%transitions),   &
                                kscplx%transitions%fromidx, kscplx%transitions%toidx, &
                             kscplx%rates, dt, pop%vmin, pop%vstep, mdef%v0,   gc%evec, gc%matrices)

                call fill_cumulative_matrices(ns, nv, gc%matrices,    gc%destprob, gc%destindex)
            end do

            pop%ncont_cpts = 0
            pop%nstoch_cpts = 0
        end do

        allocate(chwk(2, ncompartments, nchanpops))
        chwk = 0

        do icpt = 1, ncompartments
            cpt => mdef%compartments(icpt)
            ichpr = -1
            do ipop = 1, size(cpt%popnos)
                nc = cpt%popnos(ipop)
                if (nc .gt. 0) then
                    ich = cpt%popids(ipop)

                    if ((nc .gt. mdef%kschannels(ich)%stoch_threshold) .or. &
                        mdef%kschannels(ich)%non_gated) then


                        ! this is for switching from the single scheme channel table to the
                        ! multi-scheme version for continuous calculations
                        ! TODO need a flag to decide whether to do this at all (just for testing)
                        if (chanpops(ich)%altid .ge. 1) then
                             ich = chanpops(ich)%altid
                        end if

                        chanpops(ich)%ncont_cpts = chanpops(ich)%ncont_cpts + 1
                        chwk(1, icpt, ich) = nc

                    else
                        chanpops(ich)%nstoch_cpts = chanpops(ich)%nstoch_cpts + 1
                        chwk(2, icpt, ich) = nc
                    end if
                end if
            end do
        end do
!        print *, "cont  ", wkn(1, 1, 1), wkn(1, 1, 2), wkn(1, 1, 3), wkn(1, 1, 4)
!        print *, "stoch ", wkn(2, 1, 1), wkn(2, 1, 2), wkn(2, 1, 3), wkn(2, 1, 4)



        do ipop = 1, nchanpops
            pop => chanpops(ipop)
            allocate(pop%pos_cont(pop%ncont_cpts))
            allocate(pop%num_cont(pop%ncont_cpts))

            allocate(pop%pos_stoch(pop%nstoch_cpts))
            allocate(pop%num_stoch(pop%nstoch_cpts))
            ncont = 0;
            nstoch = 0;
            do icpt = 1, ncompartments
                if (chwk(1, icpt, ipop) .gt. 0) then
                    ncont = ncont + 1
                    pop%pos_cont(ncont) = icpt
                    pop%num_cont(ncont) = chwk(1, icpt, ipop)
                end if

                if (chwk(2, icpt, ipop) .gt. 0) then
                    nstoch = nstoch + 1
                    pop%pos_stoch(nstoch) = icpt
                    pop%num_stoch(nstoch) = chwk(2, icpt, ipop)
                end if
            end do
        end do



        do ipop = 1, nchanpops
            pop => chanpops(ipop)
            do igc = 1, pop%ngc
                gc => pop%gcs(igc)
                allocate(gc%states_cont(gc%nstates, pop%ncont_cpts))
                allocate(gc%states_stoch(gc%nstates, pop%nstoch_cpts))
            end do
        end do

        ncc = 0
        nvc = 0
        ngc = 0

        allocate(wkfixed(ncompartments))
        wkfixed = .false.

        do iclmp = 1, size(mdef%clamps)
            typ = mdef%clamps(iclmp)%typecode
            if (typ .eq. 0) then
                ncc = ncc + 1
            else if (typ .eq. 1) then
                nvc = nvc + 1
                wkfixed(mdef%clamps(iclmp)%tgt) = .true.

            else if (typ .eq. 2) then
                ngc = ngc + 1
            else
                print *, "ERROR - unrecognized clamp type ", typ
            end if
        end do

        nfixed = 0
        do icon = 1, nconnections
            if (wkfixed(con_from(icon)) .or. wkfixed(con_to(icon))) then
                nfixed = nfixed + 1
            end if
        end do
        allocate(con_fixed(nfixed))
        nfixed = 0
        do icon = 1, nconnections
            if (wkfixed(con_from(icon)) .or. wkfixed(con_to(icon))) then
                nfixed = nfixed + 1
                con_fixed(nfixed) = icon
            end if
        end do

        deallocate(wkfixed)



        nsynpops = mdef%sypopmap%npop
        allocate(synpops(nsynpops))
        ! several populations mnay use the same base synapse type. Copy the properties into each population
        ! for convenience later. Need to keep populatiosn separate because they are independent input targets
        do ipop = 1, nsynpops
            ipt = mdef%sypopmap%poptypes(ipop)
            syn => mdef%synapses(ipt)
            sp => synpops(ipop)
            sp%numid = ipop
            sp%normalization = syn%normalization
            sp%gbase = syn%gbase
            sp%erev = syn%erev
            sp%ndecays = syn%ndecays
            sp%nrises = syn%nrises
            sp%fdec = syn%fdec
            sp%tdec = syn%tdec
            sp%trise = syn%trise


            sp%nsyn = 0
            sp%ncpt = 0
        end do

        allocate(wknn(2, nsynpops))
        allocate(wknnn(2, nsynpops, ncompartments))
        wknn = 0
        wknnn = 0

        do icpt = 1, ncompartments
            cpt => mdef%compartments(icpt)
            do ipop = 1, size(cpt%sypopids)
                iid = cpt%sypopids(ipop)
                wknn(1, iid) = wknn(1, iid) + 1
                wknn(2, iid) = wknn(2, iid) + cpt%sypopnos(ipop)
                wknnn(1, iid, wknn(1, iid)) = icpt
                wknnn(2, iid, wknn(1, iid)) = cpt%sypopnos(ipop)
            end do
       end do

       do ipop = 1, nsynpops
           sp => synpops(ipop)
           sp%ncpt = wknn(1, ipop)
           sp%nsyn = wknn(2, ipop)
           allocate(sp%dests(sp%ncpt))
           allocate(sp%nums(sp%ncpt))
           sp%dests = wknnn(1, ipop, 1:sp%ncpt)
           sp%nums = wknnn(2, ipop, 1:sp%ncpt)

           allocate(sp%xdecs(sp%ndecays, sp%nsyn))
           sp%xdecs = 0
           if (sp%nrises .gt. 0) then
                allocate(sp%xrise(sp%nsyn))
                sp%xrise = 0
           end if
           allocate(sp%weights(sp%nsyn))
           sp%weights = sp%gbase
       end do
       deallocate(wknn, wknnn)

        call fill_decay_factors(synpops, dt)


        allocate(stimsets(mdef%ncommands))
        do icmd = 1, mdef%ncommands
             sset => stimsets(icmd)
             sset%ncc = ncc
             sset%nvc = nvc
             sset%ngc = ngc
             sset%nvcgr = size(mdef%recorders)

             if (mdef%recordClamps .eq. 1) then
                 sset%recordClamps = .true.
                 sset%nout = ncc + nvc + ngc + sset%nvcgr

             else
                sset%recordClamps = .false.
                sset%nout = sset%nvcgr
             end if

             allocate(sset%outids(sset%nout))
             allocate(sset%pcc(sset%ncc))
             allocate(sset%pvc(sset%nvc))
             allocate(sset%pgc(sset%ngc))
             allocate(sset%pvcgr(3, sset%nvcgr))

             allocate(sset%ccs(sset%ncc))
             allocate(sset%vcs(sset%nvc))
             allocate(sset%gcs(sset%ngc))

             icc = 0
             ivc = 0
             igc = 0
             do iclmp = 1, size(mdef%clamps)
                 clmp => mdef%clamps(iclmp)

                 icmdused = icmd
                 if (icmdused .gt. size(clmp%profiles)) then
                     icmdused = 1
                 end if

                 typ = clmp%typecode
                 if (typ .eq. 0) then
                      ! current clamp
                     icc = icc + 1
                     sset%pcc(icc) = clmp%tgt
                     call import_cmd(clmp%profiles(icmdused), dt, sset%ccs(icc))
                     if (sset%recordClamps) then
                         ! NB the output order for clamps is voltage clamps, current clamps, g clamps
                         ! so the current clamp ids are offset by sset%nvc
                         sset%outids(sset%nvc + icc) = clmp%id
                     end if

                 else if (typ .eq. 1) then
                     ! voltage clamp
                     ivc = ivc + 1
                     sset%pvc(ivc) = clmp%tgt
                     call import_cmd(clmp%profiles(icmdused), dt, sset%vcs(ivc))
                     if (sset%recordClamps) then
                         sset%outids(ivc) = clmp%id
                     end if

                else if (typ .eq. 2) then
                     ! couductance clamp
                     igc = igc + 1
                     sset%pgc(igc) = clmp%tgt
                     sset%gcs(igc)%aux = clmp%aux
                     call import_cmd(clmp%profiles(icmdused), dt, sset%gcs(igc))
                     if (sset%recordClamps) then
                         sset%outids(sset%nvc + sset%ncc + igc) = clmp%id
                     end if


                else
                    print *, "ERROR - missing code for clamp type ", typ
                end if
            end do

            if (sset%recordClamps) then
                iout = sset%nvc + sset%ncc + sset%ngc
            else
                iout = 0
            end if


            ! pvcgr array contains, for each recorder:    target, type, channel, pcont, pstoch
            ! where pcont and pstoch are the indexes in the

            do irec = 1, size(mdef%recorders)
                sset%pvcgr(1, irec) = mdef%recorders(irec)%tgt
                iout = iout + 1
                sset%outids(iout) = mdef%recorders(irec)%id

                if (mdef%recorders(irec)%typecode .eq. 0) then ! current recorder
                    sset%pvcgr(2, irec) = 0
                else if (mdef%recorders(irec)%typecode .eq. 1) then ! voltage recorder
                    sset%pvcgr(2, irec) = 1

                else if (mdef%recorders(irec)%typecode .eq. 3) then ! channel conductance recorder
                    sset%pvcgr(2, irec) = 3
                else if  (mdef%recorders(irec)%typecode .eq. 4) then  ! channel current recorder
                    sset%pvcgr(2, irec) = 4
                else
                    print *, "unrecognized recorder type: ", mdef%recorders(irec)%typecode
                end if



                if (sset%pvcgr(2, irec) .ge. 3) then
                    ! we're a smart channel recorder - either want hte current or conductance for a
                    ! single channel type

                    ! the difficulty here is that there could be two channel definitions for the channel
                    ! - the single-complex one, ich, and a multi-complex one ichalt, used for continuous calculations
                    ich = mdef%recorders(irec)%chan
                    ichalt = ich
                    ! the altid one is used for continuous calculations
                    if (chanpops(ich)%altid .ge. 1) then
                        ichalt = chanpops(ich)%altid
                    end if
                    sset%pvcgr(3, irec) = ich

                    pop => chanpops(ich)
                    popalt => chanpops(ichalt)

                    pcont = 0
                    pstoch = 0
                    itgt = sset%pvcgr(1, irec)
                    do icpt = 1, itgt
                        if (chwk(1, icpt, ichalt) .gt. 0) pcont = pcont + 1
                       if (chwk(2, icpt, ich) .gt. 0) pstoch = pstoch + 1
                    end do


                    ! continuous version of channel in popalt;
                    if (chwk(1, itgt, ichalt) .gt. 0) then
                        popalt%nrec = popalt%nrec + 1
                        popalt%prec(1, popalt%nrec) = irec
                        popalt%prec(2, popalt%nrec) = pcont
                    end if

                    ! stochastic one in pop
                    if (chwk(2, itgt, ich) .gt. 0) then
                        pop%nrec = pop%nrec + 1
                        pop%prec(1, pop%nrec) = irec
                        pop%prec(3, pop%nrec) = pstoch
                    end if

                    if (pop%nrec .ge. 100 .or. popalt%nrec .ge. 100) then ! TODO parameterize
                        print *, "ERROR - overrun array size: recomplile with more space for channel recorders in memchan.f90"
                        stop
                    end if
                end if

            end do
        end do

        deallocate(chwk)


        ngenerators = size(mdef%evtgens)
        allocate(generators(ngenerators))
        do igen = 1, ngenerators
            evg => mdef%evtgens(igen)
            if (evg%typ .eq. 10) then
             call alloc_explicit_generator(generators(igen), evg%typ, evg%popid, synpops(evg%popid)%nsyn, &
                evg%times, evg%targets)
            else
                call alloc_generator(generators(igen), evg%typ, evg%popid, synpops(evg%popid)%nsyn, &
                evg%seed, evg%q1)
            end if
        end do

    end subroutine cell_init



!
!    subroutine synaptic_event(ipop, isyn)
!        integer, intent(in) :: ipop, isyn
!
!        call memsyn_event(synpops(ipop) isyn)
!
!    end subroutine synaptic_event
!





    subroutine cell_clean()
         integer :: ich, igc, icmd, ic, ipop, igen
         type(chanpop), pointer :: pop
        type(gcpop), pointer :: gc
        type(stimset), pointer :: sset
        type(synpop), pointer :: sp


         deallocate(cpt_capacitance)
        deallocate(con_capacitance, con_conductance, con_from, con_to, con_fixed)


        do ich = 1, size(chanpops)
            pop => chanpops(ich)
            do igc = 1, pop%ngc
                 gc => pop%gcs(igc)
                 deallocate(gc%grel, gc%matrices, gc%destprob, gc%destindex, gc%states_cont, gc%states_stoch, gc%evec)
            end do
            deallocate(pop%gcs)
            deallocate(pop%pos_cont, pop%num_cont, pop%pos_stoch, pop%num_stoch)
        end do
        deallocate(chanpops)


        do ipop = 1, size(synpops)
            sp => synpops(ipop)
            deallocate(sp%dests, sp%nums, sp%xdecs, sp%weights)
           if (sp%nrises .gt. 0) then
                 deallocate(sp%xrise)
           end if
        end do


        do icmd = 1, size(stimsets)
             sset => stimsets(icmd)
             do ic = 1, size(sset%ccs)
                 deallocate(sset%ccs(ic)%times, sset%ccs(ic)%values, sset%ccs(ic)%transtypes)
             end do
             do ic = 1, size(sset%vcs)
                 deallocate(sset%vcs(ic)%times, sset%vcs(ic)%values, sset%vcs(ic)%transtypes)
             end do

             deallocate(sset%pcc, sset%pvc, sset%ccs, sset%vcs, sset%outids)
        end do
        deallocate(stimsets)


        do igen = 1, size(generators)
            call clear_generator(generators(igen))
        end do
        deallocate(generators)

    end subroutine cell_clean




    subroutine import_cmd(prof, dt, cmd)
        type(profile), intent(in) :: prof
        real, intent(in) :: dt
        type(stimcmd), intent(inout) :: cmd
        integer :: i, np

        cmd%step_style = prof%step_style

        if (cmd%step_style .eq. 0 .or. cmd%step_style .eq. 1) then

        np = size(prof%points) / 3
        cmd%npoints = 1 + np
        allocate(cmd%times(1 + np), cmd%values(1 + np), cmd%transtypes(1 + np))
        cmd%times(1) = 0
        cmd%values(1) = prof%start_value
        cmd%transtypes(1) = 0

        do i = 1, np
            cmd%times(1 + i) = prof%points(3 * i - 2)
            cmd%values(1 + i) = prof%points(3 * i - 1)
            cmd%transtypes(1 + i) = nint(prof%points(3 * i))
        end do


        else
            ! just plain time value data
             np = size(prof%points) / 2
            cmd%npoints = 1 + np
            allocate(cmd%times(1 + np), cmd%values(1 + np), cmd%transtypes(1))
            cmd%times(1) = 0
            cmd%values(1) = prof%start_value
            cmd%transtypes(1) = 0
            do i = 1, np
                    cmd%times(1 + i) = prof%points(2 * i - 1)
                    cmd%values(1 + i) = prof%points(2 * i)
            end do
        end if




        cmd%noised = .false.
        if (prof%noised) then
            cmd%noised = .true.
            cmd%zmean = prof%noisemean
            cmd%zgamma = exp(-dt / prof%noisets)
            cmd%zsigma = sqrt((1. - cmd%zgamma**2) * prof%noiseamp)
            cmd%seed = prof%noiseseed
            ! print *, "noise params ", cmd%zmean, cmd%zgamma, cmd%zsigma
        end if






    end subroutine import_cmd


end module cell
