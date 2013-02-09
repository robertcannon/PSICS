module fpsics

    use model
    use cell
    use output
    use mersenne

contains


    subroutine runtimings()
         call mersenne_timings()
     end subroutine runtimings


    subroutine runonce(fname, namelength, cputime)
         character (len=256), intent(in) :: fname
         integer, intent(in) :: namelength
        real(kind=k8), intent(out) :: cputime
        real :: cput1, cput2
        integer :: iseed
        integer, dimension(8) :: timevals
        integer :: nstep
        integer :: openstat
        type(modeldef) :: model

        model = mdef(fname, namelength)
 
        ! Seed the mersenne twister from the system generator
        call date_and_time(values=timevals)
        iseed = timevals(8) + 1000 * timevals(7)
        call sgrnd(iseed)

        ! print *, "seed: ", iseed, " first rnos: ", grnd(), grnd()

        call cpu_time(cput1)
         call cell_init(model)
        call cell_check()

        nstep = nint(model%runtime / model%timestep)
        call cell_run(model%ncommands, nstep, model%v0, model%timestep, model%ftimediff, model%output_file)
     
        call mdefclean(model)
        call cell_clean()
        call cpu_time(cput2)

        call format_output(model%output_file, model%sepfiles, model%sf_extension)
   	
        cputime = cput2 - cput1

         open(unit=22, file="log.txt", status="old", iostat=openstat, form="formatted", &
               position="append", action="readwrite")
         if (openstat .ne. 0) then
             print *, "error opening log file ", openstat
         else
             write(22, *) "ppp: ", fname(1:namelength-4), " ", cputime
         end if
         close(22)

 end subroutine runonce






end module fpsics

