
! These derived types mirror the structures in the input file.
! The use of pointers and arrays with the target attribute make them
! unsuitable (hard for the compiler to optimise) for the calculation
! itself so they are further processed into tables after reading

module model

    implicit none

    type ksstate
        character(12) :: id
        real         :: grel
    end type ksstate


    type kstransition
        character*12 :: id
        integer :: fromidx
        integer :: toidx
        integer :: typeCode
        real, dimension(6) :: data
    end type kstransition


    type gating_complex
        character*12 :: id
        integer :: ninstances
        type(ksstate), dimension(:), pointer  ::  states  ! => null()
        type(kstransition), dimension(:), pointer :: transitions  ! => null()
        real, dimension(:,:), pointer :: rates
    end type gating_complex


    type kschannel
        character*12 :: id
        integer :: numid
        integer :: altid
        real :: gbase
        real :: erev
        real :: vmin
        real :: vstep
        logical :: onebyone = .false.
        logical :: non_gated = .false.
        integer :: stoch_threshold
        integer :: nv
        type(gating_complex), dimension(:), pointer :: complexes => null()
    end type kschannel


    type synapse
        character*12 :: id
        integer :: numid
        integer :: ndecays
        integer :: nrises
        real :: normalization
        real :: gbase;
        real :: erev;
        real, dimension(6) :: fdec
        real, dimension(6) :: tdec
        real :: trise
    end type


    type tsypopmap
        integer :: npop
        integer, dimension(:), pointer :: poptypes
    end type tsypopmap


    type compartment
        integer :: numid
        integer, dimension(:), pointer :: popids
        integer, dimension(:), pointer :: popnos

        integer, dimension(:), pointer :: sypopids
        integer, dimension(:), pointer :: sypopnos

        integer, dimension(:), pointer :: con_ids
        real, dimension(:), pointer :: con_gs
        real :: volume
        real :: capacitance
        real :: x
        real :: y
        real :: z
    end type compartment


    type clamp
        character (len=64) :: id
        integer :: tgt
        integer :: typecode
        integer :: record
        real :: aux  ! used for additional data if needed - eg the potential for a conductance clamp
        type(profile), dimension(:), pointer :: profiles
    end type clamp


    type recorder
        character (len=64) :: id
        integer :: tgt
        integer :: typecode
        integer :: chan
        integer :: clmpidx ! for a current recorder that needs to know the voltage applied by the clamp
                           !  nb, not currently used since
    end type recorder


    type profile
        real :: start_value
        integer :: step_style
        logical :: noised
        integer :: noiseseed
        real :: noisemean, noiseamp, noisets
        real, dimension(:), pointer :: points
    end type profile


    type evtgen
        integer :: typ
        integer :: popid
        integer :: seed
        real :: q1   ! quantity 1 - either the frequency or the threshold depending on generator type
        integer :: nevent
        integer :: position
        real, dimension (:), pointer :: times
        integer, dimension(:), pointer :: targets
    end type evtgen


    type modeldef
        real :: runtime = 0.
        real :: timestep = 0.
        real :: v0 = -70
        real :: ftimediff = 0.51  ! implicit weighting of time differencing
        integer :: obo
        integer :: recordClamps
        integer :: ncommands = 0
        integer :: iout
        logical :: sepfiles = .false.
        character (len=10) :: sf_extension = ".txt"

        type(kschannel), dimension(:), pointer :: kschannels
        type(synapse), dimension(:), pointer :: synapses
        type(tsypopmap) :: sypopmap
        type(compartment), dimension(:), pointer :: compartments
        type(clamp), dimension(:), pointer :: clamps
        type(recorder), dimension(:), pointer :: recorders
        type(evtgen), dimension(:), pointer :: evtgens
        character (len=128) :: output_file
    end type modeldef


contains


    function mdef(fname, namelength)
        implicit none
        character (len=128) :: fname
        character (len=128) :: wkname
        integer :: namelength, isub
        character (len=20) :: magic
        character (len=8) :: text8

        integer :: nkschannels, nsynapses, ncompartments, nstates, ntransitions, ncomplexes
        integer :: nconnections, npopulations, nsypop, nevtgen, nevt
        integer :: nclamps, nrecorders, nprofiles, npoints, nnoise, nclamprec
        integer :: ichan, igc, itrans, iv, isyn, icpt, ntt, i, j, ievg, ictr, itgt, iclmp, iprof, noise0, nppl, ntr
        integer :: ird, chanidx, nsf, ipop
        real :: vwk, dsamp
        real, dimension(3) :: noisedat

        type(kschannel), pointer :: chandef
        type(gating_complex), pointer :: cplx
        type(synapse), pointer :: sydef
        type(compartment), pointer :: cpt
        type(clamp), pointer :: clmp
        type(profile), pointer :: prof
        type(evtgen), pointer :: evg

        type(modeldef) :: mdef
        integer, dimension(100) :: iwk, jwk
        real, dimension(100) :: rwk

        open (unit=20, file=fname(1:namelength))
        read(20,*) magic;

        read(20,*) mdef%runtime, mdef%timestep, mdef%v0, mdef%ftimediff, mdef%obo, mdef%recordClamps
        read(20, *) mdef%ncommands
        read(20, *) nkschannels


        allocate(mdef%kschannels(nkschannels))
        do ichan = 1, nkschannels
            chandef => mdef%kschannels(ichan)
            read(20, *) chandef%id
            read(20, *) chandef%numid, chandef%stoch_threshold, chandef%nv, ncomplexes, chandef%altid
            read(20, *) chandef%gbase, chandef%erev, chandef%vmin, chandef%vstep

            if (mdef%obo .eq. 1) then
                chandef%onebyone = .true.
            end if

            if (ncomplexes .eq. 0) then
                chandef%non_gated = .true.
            end if

            allocate(chandef%complexes(ncomplexes))
            do igc = 1, ncomplexes
                cplx => chandef%complexes(igc)
                read (20, *) cplx%ninstances, nstates, ntransitions, (iwk(i), i = 1, 2 * ntransitions)
                allocate(cplx%states(nstates))
                allocate (cplx%transitions(ntransitions))
                do itrans = 1, ntransitions
                    cplx%transitions(itrans)%fromidx = iwk(2 * itrans - 1)
                    cplx%transitions(itrans)%toidx = iwk(2 * itrans)
                end do
                read (20, *) cplx%states%grel  ! reads grel for each state in states array
                ntt = 2 * ntransitions
                allocate(cplx%rates(ntt, chandef%nv))
                do iv = 1, chandef%nv
                    read (20, *) vwk, cplx%rates(1:ntt, iv)
                end do
            end do
        end do

        read (20, *) nsynapses
        allocate(mdef%synapses(nsynapses))
        do isyn = 1, nsynapses
            sydef => mdef%synapses(isyn)
            read(20, *) sydef%id
            read(20, *) sydef%numid, sydef%ndecays, sydef%nrises
            read(20, *) sydef%normalization, sydef%gbase, sydef%erev, &
                 (rwk(i), i = 1, 2 * sydef%ndecays + sydef%nrises)

            ictr = 1
            do i = 1, sydef%ndecays
                sydef%fdec(i) = rwk(ictr)
                ictr = ictr + 1
                sydef%tdec(i) = rwk(ictr)
                ictr = ictr + 1
            end do
            if (sydef%nrises .gt. 0) then
                sydef%trise = rwk(ictr)
            end if
        end do


        read (20, *) mdef%sypopmap%npop
        allocate(mdef%sypopmap%poptypes(mdef%sypopmap%npop))
        read (20, *) (mdef%sypopmap%poptypes(i), i = 1, mdef%sypopmap%npop)
        do ipop = 1, mdef%sypopmap%npop
            mdef%sypopmap%poptypes(ipop) = mdef%sypopmap%poptypes(ipop) + 1
        end do


        read (20, *) ncompartments
        allocate(mdef%compartments(ncompartments))



        do icpt = 1, ncompartments
            cpt => mdef%compartments(icpt)
            read (20, *) cpt%numid, nconnections, npopulations, nsypop, &
            (iwk(i), i = 1, 2 * npopulations), (jwk(i), i = 1, 2 * nsypop)

            allocate(cpt%popids(npopulations), cpt%popnos(npopulations))
            allocate(cpt%con_ids(nconnections), cpt%con_gs(nconnections))
            allocate(cpt%sypopids(nsypop), cpt%sypopnos(nsypop))

            do i = 1, npopulations
                cpt%popids(i) = iwk(2 * i - 1) + 1
                cpt%popnos(i) = iwk(2 * i)
            end do
            do i = 1, nsypop
                cpt%sypopids(i) = jwk(2 * i - 1) + 1
                cpt%sypopnos(i) = jwk(2 * i)
            end do

            read (20, *) (cpt%con_ids(i), i = 1, nconnections), (cpt%con_gs(i), i = 1, nconnections), &
                        cpt%volume, cpt%capacitance, cpt%x, cpt%y, cpt%z

        end do

        read (20, *) nclamps, nrecorders, nsf
        if (nsf .eq. 1) then
              mdef%sepfiles = .true.
        end if
        read (20, *) dsamp

        if (nsf .eq. 1) then
            read (20, *) mdef%sf_extension
            print *, "read the ext ", mdef%sf_extension
        end if

        mdef%iout = 1;
        if (dsamp .gt. mdef%timestep) then
            mdef%iout = nint(dsamp / mdef%timestep)
        end if

        nclamprec = 0
        allocate (mdef%clamps(nclamps))
        do iclmp = 1, nclamps
            clmp => mdef%clamps(iclmp)
            read (20, *) clmp%id
            read (20, *) itgt, clmp%typecode, clmp%record, nprofiles

            ! typecodes are:  0:current, 1:voltage, 2:conductance
            clmp%tgt = itgt + 1
            if (clmp%record .eq. 1) then
                nclamprec = nclamprec + 1
            end if

            if (clmp%typecode .eq. 2) then  ! ADHOC - put these magic numbers as parameters somewhere
                read (20, *) clmp%aux
            end if

            allocate (clmp%profiles(nprofiles))
            do iprof = 1, nprofiles
                prof => clmp%profiles(iprof)
                read (20, *) prof%start_value, nnoise, noise0, (noisedat(i), i = 1, nnoise), &
                       prof%step_style, npoints, nppl

                 allocate (prof%points(npoints))

                 if (npoints .gt. 0) then
                do ird = 0, (npoints -1) / nppl
                    ntr = (npoints - nppl * ird)
                    if (ntr .gt. nppl) then
                        ntr = nppl
                    end if
                    read (20, *) (prof%points(nppl * ird + i), i = 1, ntr)
                end do
                end if

                if (nnoise .eq. 0) then
                    prof%noised = .false.
                else
                    prof%noised = .true.
                    prof%noiseseed = noise0
                    prof%noisemean = noisedat(1);
                    prof%noiseamp = noisedat(2);
                    prof%noisets = noisedat(3);
                end if

            end do
        end do

        if (mdef%recordClamps .eq. 1) then
            ! they will all be recorded anyway
            nclamprec = 0
        end if

        allocate (mdef%recorders(nrecorders + nclamprec))
        do i = 1, nrecorders
            read (20, *) mdef%recorders(i)%id
            read (20, *) itgt, mdef%recorders(i)%typecode, chanidx


            mdef%recorders(i)%tgt = itgt + 1
            mdef%recorders(i)%chan = chanidx + 1
!            print *, "read recorder ", i, mdef%recorders(i)%tgt, mdef%recorders(i)%typecode,  mdef%recorders(i)%chan
        end do


        if (nclamprec .gt. 0) then
            nclamprec = 0
            do iclmp = 1, nclamps
                 clmp => mdef%clamps(iclmp)
                 if (clmp%record .eq. 1) then
                    nclamprec = nclamprec + 1
                    i = nrecorders + nclamprec
                    mdef%recorders(i)%id = clmp%id
                    if (clmp%typecode .eq. 0) then
                        mdef%recorders(i)%typecode = 1   ! of current clamp, record voltage
                    else if (clmp%typecode .eq. 1) then
                        mdef%recorders(i)%typecode = 0   ! if voltage clamp, record current
                        mdef%recorders(i)%clmpidx = iclmp
                    else
                        mdef%recorders(i)%typecode = 1
                    end if
                    mdef%recorders(i)%tgt = clmp%tgt
                    mdef%recorders(i)%chan = 0
                 end if
            end do
        end if



        read (20, *) nevtgen
        allocate(mdef%evtgens(nevtgen))
        do ievg = 1, nevtgen
            evg => mdef%evtgens(ievg)
            read (20, *) evg%typ, evg%popid, evg%seed
            if (evg%typ .eq. 10) then
                read (20, *) nevt
                allocate(evg%times(nevt))
                allocate(evg%targets(nevt))
                do j = 1, nevt
                    read (20, *) evg%times(j), evg%targets(j)
                end do
            else
                read (20, *) evg%q1
            end if
            evg%popid = evg%popid + 1    ! 0 indexed arrays in the ppp file, 1 here
        end do


        read (20, '(a8)') text8
        close (20)
        if (text8(1:3) .ne. 'END') then
            print *, "ERROR line miscount reading input file - expecting 'END...' but got ", text8
            stop
        end if



        do i = 1, 127
            mdef%output_file(i:i+1) = " ";
        end do

  
        isub = index(fname(1:namelength), ".ppp", .true.)
        mdef%output_file(1:isub-1) = fname(1:isub-1)

        print *, "output will go to files with root name ", mdef%output_file

    end function mdef





    subroutine mdefclean(model)
        type(modeldef) model
        type(compartment), pointer :: cpt
        type(kschannel), pointer :: chandef
        type(gating_complex), pointer :: cplx
        type(clamp), pointer :: clmp
        type(evtgen), pointer :: evg
        integer :: icpt, ichan, icplx, iclmp, iprof, ievg


        do icpt = 1, size(model%compartments)
            cpt => model%compartments(icpt)
            deallocate(cpt%popids, cpt%popnos, cpt%con_ids, cpt%con_gs)
        end do
        deallocate(model%compartments)


        do ichan = 1, size(model%kschannels)
            chandef => model%kschannels(ichan)
            do icplx = 1, size(chandef%complexes)
                cplx => chandef%complexes(icplx)
                deallocate(cplx%states, cplx%transitions, cplx%rates)
            end do
            deallocate(chandef%complexes)
        end do
        deallocate(model%kschannels)


        do iclmp = 1, size(model%clamps)
            clmp => model%clamps(iclmp)
            do iprof = 1, size(clmp%profiles)
                deallocate(clmp%profiles(iprof)%points)
            end do
            deallocate(clmp%profiles)
        end do


        do ievg = 1, size(model%evtgens)
            evg => model%evtgens(ievg)
            if (evg%typ .eq. 10) then
                deallocate(evg%times, evg%targets)
            end if
        end do
        deallocate(model%clamps, model%recorders, model%synapses, model%evtgens)

    end subroutine mdefclean

end module model

