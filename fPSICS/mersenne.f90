!____________________________________________________________________________
! A C-program for MT19937: Real number version
!   genrand() generates one pseudorandom real number (double)
! which is uniformly distributed on [0,1]-interval, for each
! call. sgenrand(seed) set initial values to the working area
! of 624 words. Before genrand(), sgenrand(seed) must be
! called once. (seed is any 32-bit integer except for 0).
! Integer generator is obtained by modifying two lines.
!   Coded by Takuji Nishimura, considering the suggestions by
! Topher Cooper and Marc Rieffel in July-Aug. 1997.
!
! GPL etc...
! Copyright (C) 1997 Makoto Matsumoto and Takuji Nishimura.
! When you use this, send an email to: matumoto@math.keio.ac.jp
! with an appropriate reference to your work.
!
!
! This program uses the following non-standard intrinsics.
!   ishft(i,n): If n>0, shifts bits in i by n positions to left.
!               If n<0, shifts bits in i by n positions to right.
!   iand (i,j): Performs logical AND on corresponding bits of i and j.
!   ior  (i,j): Performs inclusive OR on corresponding bits of i and j.
!   ieor (i,j): Performs exclusive OR on corresponding bits of i and j.
!
!***********************************************************************
! Fortran version rewritten as an F90 module and mt state saving and getting
! subroutines added by Richard Woloshyn. (rwww@triumf.ca). June 30, 1999



 module mersenne
! Default seed
    integer, parameter :: defaultsd = 4357
! Period parameters
    integer, parameter :: N = 624, N1 = N + 1

! the array for the state vector
    integer, save, dimension(0:N-1) :: mt
    integer, save                   :: mti = N1


 contains

!Initialization subroutine
  subroutine sgrnd(seed)
    implicit none
!
!      setting initial seeds to mt[N] using
!      the generator Line 25 of Table 1 in
!      [KNUTH 1981, The Art of Computer Programming
!         Vol. 2 (2nd Ed.), pp102]
!
    integer, intent(in) :: seed

    mt(0) = iand(seed,-1)
    do mti=1,N-1
      mt(mti) = iand(69069 * mt(mti-1),-1)
    enddo
    return
  end subroutine sgrnd



!Random number generator
    real(8) function grnd()
    implicit integer(a-z)

! Period parameters
    integer, parameter :: M = 397, MATA  = -1727483681
!                                    constant vector a
    integer, parameter :: LMASK =  2147483647
!                                    least significant r bits
    integer, parameter :: UMASK = -LMASK - 1
!                                    most significant w-r bits
! Tempering parameters
    integer, parameter :: TMASKB= -1658038656, TMASKC= -272236544

    dimension mag01(0:1)
    data mag01/0, MATA/
    save mag01
!                        mag01(x) = x * MATA for x=0,1

    TSHFTU(y)=ishft(y,-11)
    TSHFTS(y)=ishft(y,7)
    TSHFTT(y)=ishft(y,15)
    TSHFTL(y)=ishft(y,-18)

    if(mti.ge.N) then
!                       generate N words at one time
      if(mti.eq.N+1) then
!                            if sgrnd() has not been called,
        call sgrnd( defaultsd )
!                              a default initial seed is used
      endif

      do kk=0,N-M-1
          y=ior(iand(mt(kk),UMASK),iand(mt(kk+1),LMASK))
          mt(kk)=ieor(ieor(mt(kk+M),ishft(y,-1)),mag01(iand(y,1)))
      enddo
      do kk=N-M,N-2
          y=ior(iand(mt(kk),UMASK),iand(mt(kk+1),LMASK))
          mt(kk)=ieor(ieor(mt(kk+(M-N)),ishft(y,-1)),mag01(iand(y,1)))
      enddo
      y=ior(iand(mt(N-1),UMASK),iand(mt(0),LMASK))
      mt(N-1)=ieor(ieor(mt(M-1),ishft(y,-1)),mag01(iand(y,1)))
      mti = 0
    endif

    y=mt(mti)
    mti = mti + 1
    y=ieor(y,TSHFTU(y))
    y=ieor(y,iand(TSHFTS(y),TMASKB))
    y=ieor(y,iand(TSHFTT(y),TMASKC))
    y=ieor(y,TSHFTL(y))

    if(y .lt. 0) then
      grnd=(dble(y)+2.0d0**32)/(2.0d0**32-1.0d0)
    else
      grnd=dble(y)/(2.0d0**32-1.0d0)
    endif

    return
  end function grnd




    subroutine array_grnd(n, vals)
        integer, intent(in) :: n
        real, dimension(n), intent(out) :: vals
        integer i
        do i = 1, n
            vals(i) = grnd()
        end do
    end subroutine array_grnd



!Initialization subroutine
  subroutine inst_sgrnd(seed, wk)
    implicit none

    integer, intent(in) :: seed
    integer, intent(inout), dimension(0:N) :: wk
    integer :: imti

    wk(0) = iand(seed,-1)

    do imti=1,N-1
      wk(imti) = iand(69069 * wk(imti-1),-1)
    enddo
    wk(N) = imti
    return
  end subroutine inst_sgrnd



!Random number generator
    real(8) function inst_grnd(wk)
    implicit integer(a-z)
    integer, intent(inout), dimension(0:N) :: wk
    integer :: imti

! Period parameters
    integer, parameter :: M = 397, MATA  = -1727483681
!                                    constant vector a
    integer, parameter :: LMASK =  2147483647
!                                    least significant r bits
    integer, parameter :: UMASK = -LMASK - 1
!                                    most significant w-r bits
! Tempering parameters
    integer, parameter :: TMASKB= -1658038656, TMASKC= -272236544

    dimension mag01(0:1)
    data mag01/0, MATA/
    save mag01
!                        mag01(x) = x * MATA for x=0,1

    TSHFTU(y)=ishft(y,-11)
    TSHFTS(y)=ishft(y,7)
    TSHFTT(y)=ishft(y,15)
    TSHFTL(y)=ishft(y,-18)

    imti = wk(N)

    if(imti.ge.N) then
!                       generate N words at one time
      if(imti.eq.N+1) then
!                            if sgrnd() has not been called,
        call inst_sgrnd( defaultsd, wk)
!                              a default initial seed is used
      endif

      do kk=0,N-M-1
          y=ior(iand(wk(kk),UMASK),iand(wk(kk+1),LMASK))
          wk(kk)=ieor(ieor(wk(kk+M),ishft(y,-1)),mag01(iand(y,1)))
      enddo
      do kk=N-M,N-2
          y=ior(iand(wk(kk),UMASK),iand(wk(kk+1),LMASK))
          wk(kk)=ieor(ieor(wk(kk+(M-N)),ishft(y,-1)),mag01(iand(y,1)))
      enddo
      y=ior(iand(wk(N-1),UMASK),iand(wk(0),LMASK))
      wk(N-1)=ieor(ieor(wk(M-1),ishft(y,-1)),mag01(iand(y,1)))
      imti = 0
    endif

    y=wk(imti)
    imti = imti + 1
    y=ieor(y,TSHFTU(y))
    y=ieor(y,iand(TSHFTS(y),TMASKB))
    y=ieor(y,iand(TSHFTT(y),TMASKC))
    y=ieor(y,TSHFTL(y))

    if(y .lt. 0) then
      inst_grnd=(dble(y)+2.0d0**32)/(2.0d0**32-1.0d0)
    else
      inst_grnd=dble(y)/(2.0d0**32-1.0d0)
    endif
    wk(N) = imti

    return
  end function inst_grnd


  subroutine mersenne_first_n

  integer, parameter      :: no=1000
    real(8), dimension(0:7) :: r
    integer j,k
!    real(8) grnd
!
!      call sgrnd(4357)!                         any nonzero integer can be used as a seed
    do j=0,no-1
      r(mod(j,8))=grnd()
      if(mod(j,8).eq.7) then
        write(*,'(8(f9.6,'' ''))') (r(k),k=0,7)
      else if(j.eq.no-1) then
        write(*,'(8(f9.6,'' ''))') (r(k),k=0,mod(no-1,8))
      endif
    enddo
  end subroutine mersenne_first_n



    subroutine mersenne_timings()
        real, dimension(100000) :: a
        real :: t1, t2
        integer ::  i, j
        call cpu_time(t1)

            do i = 1, 1000
                do j = 1, 100000
                    a(j) = grnd()
                end do
            end do

        call cpu_time(t2)
        print *, "first block took ", (t2 - t1)

!		do i = 1, 1000
!				do j = 1, 100000
!					call random_number(a(j))
!				end do
!			end do


        call cpu_time(t1)
        print *, " secnd block took ", (t1 - t2)

    end subroutine mersenne_timings


 end module mersenne


