
module activity

    use mersenne
    use memchan


    implicit none
   integer, public, parameter  :: kl8 = selected_real_kind( 15 )

    private
    public :: generator
    public:: alloc_generator, alloc_explicit_generator, init_generator, advance_generator, clear_generator

    type generator
        integer :: typ
        integer :: destpop
        integer :: seed
        integer :: popsize
        real :: threshold
        real :: delta_ctr
        real :: ctr
        real :: tany
        real :: pv0
        integer :: position
        integer :: nexplicit
        real, dimension(:), pointer :: times
        integer, dimension(:), pointer :: targets
        integer, dimension(0:624) :: ranwk
    end type generator


contains


    subroutine alloc_generator(gen, typ, popid, popsize, seed, q1)
        type(generator), intent(inout) :: gen
        integer, intent(in) :: typ, popid, popsize, seed
        real, intent(in) :: q1

        gen%typ = typ
        gen%destpop = popid
        gen%popsize = popsize
        gen%seed = seed

        ! could allocate gen%ranwk here only as needed
        if (typ .eq. 1) then
            ! uniform
            ! q1 is the frequency
            gen%delta_ctr = 1. / q1
            gen%ctr = 0.

        else if (typ .eq. 2) then
            ! poisson
            ! q1 is the frequency
            gen%tany = 1. / (q1 * popsize)


        else if (typ .eq. 10) then
            print *, "ERROR - wrong call for explicit generators"

        else if (typ .eq. 20) then
            ! q1 is the threshold
            gen%threshold = q1
            gen%pv0 = q1 - 1

        else
             print *, "ERROR - unrecognized generator type ", typ
        end if
    end subroutine alloc_generator


subroutine alloc_explicit_generator(gen, typ, popid, popsize, times, targets)
        type(generator), intent(inout) :: gen
        integer, intent(in) :: typ, popid, popsize
        real, dimension(:), pointer :: times
        integer, dimension(:), pointer :: targets

        gen%typ = typ
        gen%destpop = popid
        gen%popsize = popsize
        gen%seed = 0


        if (typ .eq. 10) then
            ! explicit
            gen%times => times
            gen%targets => targets
            gen%position = 0
            gen%nexplicit = size(times)
            print *, "Explicit event sequence with ", gen%nexplicit, " events "
        else
             print *, "ERROR - unrecognized generator type ", typ
        end if
    end subroutine alloc_explicit_generator



    subroutine init_generator(gen)
        type(generator), intent(inout) :: gen

         gen%position = 0
         if (gen%seed .gt. 0) then
            call inst_sgrnd(gen%seed, gen%ranwk)
        else
            call inst_sgrnd(ceiling(100000 * grnd()), gen%ranwk)
        end if


         if (gen%typ .eq. 1) then
            gen%ctr =  0.5 * gen%delta_ctr

         else if (gen%typ .eq. 2) then
            gen%ctr = -1 * gen%tany * log(inst_grnd(gen%ranwk))

         else if (gen%typ .eq. 20) then
            gen%pv0 = gen%threshold - 1
         else
            ! others ok as they are?
         end if
    end subroutine init_generator




    subroutine clear_generator(gen)
        type(generator), intent(inout) :: gen
        gen%ctr = 0.
        ! should deallocate any random work space here
    end subroutine clear_generator




    subroutine advance_generator(gen, sp, dt, v0, time)
        type(generator), intent(inout) :: gen
        type (synpop), intent(inout) :: sp
        real, intent(in) :: dt
        real, intent(in) :: v0
        real (kind=kl8), intent(in) :: time
        integer :: isy



        if (gen%typ .eq. 1) then
            gen%ctr = gen%ctr - dt
            do while (gen%ctr .lt. 0.)
                do isy = 1, gen%popsize
                    call memsyn_event(sp, isy)
                end do
                gen%ctr = gen%ctr + gen%delta_ctr
            end do

        else if (gen%typ .eq. 2) then
            gen%ctr = gen%ctr - dt
            do while (gen%ctr .lt. 0)
                 isy = ceiling(inst_grnd(gen%ranwk) * gen%popsize)
                 call memsyn_event(sp, isy)
                 gen%ctr = gen%ctr + (-1 * gen%tany * log(inst_grnd(gen%ranwk)))
            end do

         else if (gen%typ .eq. 10) then
             do while (gen%position + 1 .lt. gen%nexplicit .and. gen%times(gen%position+1) .lt. time)
                 gen%position = gen%position + 1
                 isy = gen%targets(gen%position)
                 call memsyn_event(sp, isy)
             end do


         else if (gen%typ .eq. 20) then
            if (gen%pv0 .lt. gen%threshold .and. v0 .ge. gen%threshold) then
                do isy = 1, gen%popsize
                    call memsyn_event(sp, isy)
                end do
            end if
            gen%pv0 = v0

         else
     !       print *, "unrecognized ", gen%typ
        end if


    end subroutine advance_generator





end module activity
