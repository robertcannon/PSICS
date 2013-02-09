
module stimrec

    use mersenne

    implicit none

    integer, public, parameter  :: k8 = selected_real_kind( 15 )

    type stimcmd
        integer :: npoints
        integer :: step_style
        logical :: noised = .false.
        real :: zmean, zgamma, zsigma
        integer :: seed = -1
        real :: aux
        real(kind=k8), dimension(:), pointer :: times
        real(kind=k8), dimension(:), pointer :: values
        ! transtypes currently only codes repeats but could encode ramp transitions etc
        integer, dimension(:), pointer :: transtypes
    end type stimcmd




    type stimset
        logical :: recordClamps
        integer :: nout
        integer :: ncc
        integer :: nvc
        integer :: ngc
        integer :: nvcgr
        type(stimcmd), dimension(:), pointer :: ccs
        type(stimcmd), dimension(:), pointer :: vcs
        type(stimcmd), dimension(:), pointer :: gcs

        integer, dimension(:), pointer :: pcc
        integer, dimension(:), pointer :: pvc
        integer, dimension(:), pointer :: pgc
        integer, dimension(:,:), pointer :: pvcgr
        character (len=64), dimension(:), pointer :: outids
        ! cck, vck, gck are arrays of indexes into the profile vectors that
        ! keep track of the current segment of the profile
        integer, dimension(:), pointer :: pos, inoise
        real, dimension(:), pointer :: offset, ccvals, vcvals, gcvals, gcdrive, znoise

    end type stimset


contains


    subroutine stiminit(stim)
         type(stimset), intent(inout) :: stim

        integer :: i, sd, ncc, nvc, ngc, ntot
        real :: rno

          ncc = stim%ncc
        nvc = stim%nvc
        ngc = stim%ngc
        ntot = ncc + nvc + ngc

        allocate(stim%znoise(ntot), stim%inoise(ntot), stim%pos(ntot), stim%offset(ntot))
        stim%znoise = 0

          allocate(stim%ccvals(ncc), stim%vcvals(nvc), stim%gcvals(ngc), stim%gcdrive(ngc))
        stim%pos = 1
        stim%offset = 0.


        do i = 1, ntot
            call random_number(rno)
            stim%inoise(i) = floor(1e6 * rno)
        end do

        do i = 1, ncc
            sd = stim%ccs(i)%seed
            if (sd .ge. 0) stim%inoise(i) = sd
        end do

        do i = 1, nvc
            sd = stim%vcs(i)%seed
             if (sd .ge. 0) stim%inoise(ncc + i) = sd
        end do

        do i = 1, ngc
            sd = stim%gcs(i)%seed
            stim%gcdrive(i) = stim%gcs(i)%aux
            if (sd .ge. 0) stim%inoise(ncc + nvc + i) = sd
        end do


    end subroutine stiminit





    subroutine stimclean(stim)
        type(stimset), intent(inout) :: stim
        deallocate(stim%pos, stim%offset, stim%ccvals, stim%gcvals, stim%vcvals)
        deallocate(stim%gcdrive, stim%inoise, stim%znoise)
    end subroutine stimclean




    subroutine stimvalues(stim, time, dt)
        type(stimset), intent(inout) :: stim
        real(kind=k8), intent(in) :: time
        real, intent(in) :: dt
        integer :: i, ioff, k

        ioff = 0

        do i = 1, stim%ncc
            call cmdvalue(stim%ccs(i), time, dt, stim%pos(i), stim%offset(i), stim%inoise(i), stim%znoise(i), stim%ccvals(i))
        end do

        ioff = ioff + stim%ncc
        do i = 1, stim%nvc
            k = ioff + i
            call cmdvalue(stim%vcs(i), time, dt, stim%pos(k), stim%offset(k), stim%inoise(k), stim%znoise(k), stim%vcvals(i))
        end do

        ioff = ioff + stim%nvc
        do i = 1, stim%ngc
             k = ioff + i
            call cmdvalue(stim%gcs(i), time, dt, stim%pos(k), stim%offset(k), stim%inoise(k), stim%znoise(k), stim%gcvals(i))
        end do
    end subroutine stimvalues



    subroutine cmdvalue(cmd, time, dt, ipos, rptoffset, inoise, zn, val)
        type(stimcmd), intent(in) :: cmd
        real(kind=k8), intent(in) :: time
        real, intent(in) :: dt
        integer, intent(inout) :: ipos, inoise
           real, intent(inout) :: rptoffset
        real, intent(inout) :: zn
        real, intent(out) :: val
        real :: tpr, tnx, rtime, f
        integer :: nrep


        rtime = time + rptoffset;

        if (cmd%step_Style .eq. 2) then
            rtime = rtime + 0.5 * dt
        end if


        do
            if (ipos .eq. cmd%npoints) exit
            if (cmd%times(ipos + 1) .ge. rtime) exit
            ! NB need .ge. here since rtime is single precision, and we don't want
            ! to step into a repeat (type 10) step as though it was a normal one -
            ! leave it to get caught below.
            ipos = ipos + 1
        end do
        val = cmd%values(ipos)
         tpr = cmd%times(ipos)

		! print *, time, rtime, ipos, val

        if (cmd%step_Style .eq. 2) then
            ! interpolating in a sampled trace
             if (ipos .lt. cmd%npoints) then
               tnx = cmd%times(ipos + 1)
                 f = (rtime - tpr) / (tnx - tpr)
                 val = f * cmd%values(ipos+1) + (1. - f) * cmd%values(ipos);
            end if

        else
         if (ipos .lt. cmd%npoints) then
           if (rtime + dt .gt. cmd%times(ipos + 1) .and. cmd%transtypes(ipos + 1) .eq. 10) then
             ! signals start of a repeat

            nrep = nint(cmd%values(ipos + 1))

			! print *, "doing jumpback ", (rtime + dt), cmd%times(ipos + 1), cmd%times(ipos + 1 - nrep)


            ! the values elt actually encodes how many steps to go back and repeat, using nint upsets Absoft for some reason
            rptoffset = rptoffset - (cmd%times(ipos+1) - cmd%times(ipos + 1 - nrep))
            ipos = ipos - nrep
            rtime = time + rptoffset
            val = cmd%values(ipos)
            ! print *, "xxx done jumpback ", time, rtime, ipos, val
            end if
        end if

        if (ipos .lt. cmd%npoints) then
            tnx = cmd%times(ipos + 1)
            if (tnx .lt. rtime + dt) then
                f = (tnx - rtime) / dt
                if (cmd%transtypes(ipos + 1) .eq. 10) then
                    print *, "ERROR - stepped into repeat block as a normal step"
                end if

                if (cmd%step_style .eq. 0) then
                    ! MDPOINT
                    if (f .gt. 0.5) then
                        val = cmd%values(ipos)
                    else
                        val = cmd%values(ipos + 1)
                    end if
                else
                    ! AVERAGE
                    val = f * cmd%values(ipos) + (1 - f) * cmd%values(ipos + 1)
                end if


                if (cmd%step_style .ne. 2) then
                if (cmd%transtypes(ipos+1) .eq. 10) then
                    print *, "messed up repeat count - erroneous val ", val
                end if
                end if

            end if
        end if

        end if


        if (cmd%noised) then
            ! print *, "advancing noise ", zn
            zn = cmd%zgamma * zn + cmd%zsigma * randomn(inoise)
            val = val + cmd%zmean + zn
        end if

    end subroutine cmdvalue




    function randomn(inoise)
        real :: randomn
        integer, intent(inout) :: inoise
         integer, parameter :: im = 714025
        integer, parameter :: ia = 1366
           integer, parameter :: ic = 150889
          real :: r, ran1, ran2, fac;
          r = 2;
          do while (r .gt. 1.)
             inoise = mod(inoise * ia + ic, im)
           ran1 = (2. * inoise) / im - 1;

           inoise = mod(inoise * ia + ic, im)
           ran2 = (2. * inoise) / im - 1;
           r = ran1**2 + ran2**2;
          end do

       fac = sqrt(-2. * log(r) / r);
       randomn = ran1 * fac;
      !      g2 = ran2 * fac;  ! TODO should use this guy too, to save every other call
    end function randomn



end module stimrec
