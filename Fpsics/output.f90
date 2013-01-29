

module output
    implicit none


contains

    subroutine format_output(output_root, sepfiles, sf_extension)
        character (len=128) :: output_root
        character (len=10) :: sf_extension
        logical :: sepfiles
        integer :: ncommands, nsteps, nout, ncol, openstat, i
        character (len=64), dimension(:), allocatable :: outids
        character (len=128) :: prefix

        ! the unformatted source data
        open(unit=20, file=trim(output_root)//".dat", form="unformatted", status="old", iostat=openstat)
        if (openstat .ne. 0) then
            print *, "ERROR: cant open file ", output_root
            stop
        end if

!        print *, "formatted to ", trim(output_root)//".txt"
        open(unit=21, file=trim(output_root)//".txt", status="replace", iostat=openstat)

        read (20) ncommands
        read (20) nsteps, nout

        allocate (outids(nout))
        read (20) (outids(i), i = 1, nout)
        ncol = 1 + ncommands * nout

        if (ncol .le. 1001) then
            call format_transposed(ncommands, nsteps, nout, outids)
        else
            call format_insequence(ncommands, nsteps, nout, outids)
        end if

        if (sepfiles .and. ncol .le. 100) then
            open(unit=21, file=trim(output_root)//".txt", status="old", iostat=openstat)
            prefix = trim(output_root)//"-"
            if (output_root(1:9) .eq. "psics-out") then
                prefix = "";
            end if
            call split_columns(prefix, sf_extension, ncol, nsteps)
        end if

        deallocate (outids)
    end subroutine format_output



     subroutine format_insequence(ncommands, nsteps, nout, outids)
         integer, intent(in) :: ncommands, nsteps, nout
         character (len=64), dimension(nout), intent(in) :: outids
         character (len=64) :: uuid
         integer :: icmd, i, j, noutwk
         integer :: uunsteps
         real, dimension(:,:), allocatable :: vdat
         real, dimension(:), allocatable :: t



         write (21, *) "#FPSICS2 multiple runs in successive blocks. This file contains:"
         write (21, *) "# nrec, nsteps, nruns (recordings per run, steps per run, number of runs)"
         write (21, *) "# times(1,...,nstep)"
         write (21, *) "# data((i = 1,...,nrec), j=1,...,nstep) for each of the runs"

         write (21, *) nout, nsteps, ncommands

        allocate(t(nsteps))
        allocate(vdat(nout, nsteps))

        print *, "nrec = ", nout, " nsteps=", nsteps

         do icmd = 1, ncommands
            if (icmd .gt. 1) then
               read (20) uunsteps, noutwk
               read (20) (uuid, i = 1, noutwk)
            end if

            do i = 1, nsteps
               read (20) t(i), vdat(1:nout, i)
            end do
            write (21, *) (((vdat(i, j)), i = 1, nout), j = 1, nsteps)
         end do

         close(20)
         close(21)
         deallocate(t)
         deallocate(vdat)

     end subroutine format_insequence




     subroutine format_transposed(ncommands, nsteps, nout, outids)
         integer, intent(in) :: ncommands, nsteps, nout
         character (len=64), dimension(nout), intent(in) :: outids
         character (len=2000) hformat, dformat, wkformat
         real, dimension(:,:), allocatable :: tvdat
         integer :: i, icmd, irec, ncol, uunout, uunsteps, iout
         character (len=64) :: uuid


         real :: cput1, cput2
         ncol = 1 + ncommands * nout

        write (wkformat, '(i4)') ncommands
         if (ncommands .gt. 1) then
            hformat = '("#time ", '//trim(wkformat)//'('
            do i = 1, nout
                if (i .gt. 1) then
                    hformat = trim(hformat)//', '
                end if
                hformat = trim(hformat)//' " '//trim(outids(i))//'",i0'
            end do
            hformat = trim(hformat)//'))'
            write (21, hformat) ((icmd, iout = 1, nout),  icmd = 1, ncommands)
        else
 !            write (21, '(#time '//trim(wkformat)//'(a))') (outids(i), i = 1, nout)
            write (21, *) "#time ", ((trim(outids(i))//" "), i = 1, nout)
        end if


        write(wkformat, '(i4)') ncol
        dformat = '('//trim(wkformat)//'g16.7)'


        call cpu_time(cput1);

        allocate(tvdat(ncol, nsteps))
        if (ncommands .eq. 1) then
            do i = 1, nsteps
!            	print *, "reading ", i, " of ", nsteps, " with cols= ", ncol
               read(20) tvdat(:, i)
            end do

            close(20)

        else

            do icmd = 1, ncommands
                if (icmd .gt. 1) then
                    read (20) uunsteps, uunout
                    read (20) (uuid, i = 1, uunout)
                end if
                irec = 1 + (icmd - 1) * nout
                do i = 1, nsteps
                    read(20) tvdat(1,i), tvdat(irec+1:irec+nout, i)
                end do

            end do
        end if

        write(21, dformat) tvdat
        close(21)


        call cpu_time(cput2)
        deallocate(tvdat)
    end subroutine format_transposed


    subroutine split_columns(prefix, sf_extension, ncol, nlin)
        character (len=128) :: prefix
        character (len=10) :: sf_extension
        integer, intent(in) :: ncol, nlin
        character (len=64) :: colname
        integer :: i, j, openstat
        real, dimension(ncol, nlin) :: dat
        character (len=64), dimension(ncol) :: colnames

        read (21, *) colnames
        do i = 1, nlin
             read(21, *) dat(:, i)
        end do
        close (21)


        do i = 1, ncol
                colname = colnames(i)
                if (colname(1:1) .eq. "#") then
                    colname = colname(2:len(colname))
                end if

                 open(unit=22, file=trim(prefix)//trim(colname)//trim(sf_extension), status="replace", iostat=openstat)
                 if (openstat .ne. 0) then
                    print *, "ERROR: cant open file ", prefix//trim(colname)
                       stop
                end if
                write (22, '(f13.4)') (dat(i,j), j = 1, nlin)
                close (22)

         end do
   end subroutine split_columns



end module output
