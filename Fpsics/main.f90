

! For Absoft add compiler flag -lU77 to make getarg available


program fpsicsmain

   use fpsics


   character (len=32) :: versionname="psics-1.1.1"
   character (len=32) :: versiondate="18-May-2011"

   character (len=128) :: filename
   real(kind=k8)  :: cputime
   integer :: lfnm
   integer :: nargs

   print *, versionname, versiondate
   nargs = iargc();
   if (nargs .eq. 1) then
           call getarg(1, filename)
           lfnm = len_trim(filename)
           print *, "running file ", filename, " length is ", lfnm
           call runonce(trim(filename), lfnm, cputime)
   else
    print *, "fpsics requires one argument: the name of the ppp file containing the model specification"
   end if


  stop
end program fpsicsmain


