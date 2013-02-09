

module passprop

    use stimrec
    implicit none


contains



    subroutine check_connections(ncompartments, nconnections, con_from, con_to)
        integer, intent(in) :: ncompartments, nconnections
        integer, dimension(nconnections), intent(in) :: con_from, con_to
        integer :: i
        if (nconnections .ge. 1) then
            if (maxval(con_to) .ne. ncompartments) then
                print *, "ERROR: size mismatch, ncpts=", ncompartments, " ncons=", nconnections, &
                    " but max connection is to ", maxval(con_to)
                stop
            end if
        end if

        do i = 2, nconnections
            if (con_to(i) .lt. con_to(i-1)) then
                print *, "ERROR: the con_to connections array must be in increasing ", &
                 "order, but elt ", i, " is out of order: ", con_from(i-1), "-->", con_to(i-1), con_from(i), "-->", con_to(i)
                stop
            end if
        end do
    end subroutine check_connections




    subroutine vupdate(ncompartments, gchan, echan, cpt_capacitance,                &
             nconnections, con_from, con_to, con_conductance, con_capacitance,      &
             nfixed, con_fixed, stim,  dt, ftimediff,    v)

        integer, intent(in) :: ncompartments, nconnections, nfixed
        real, dimension(ncompartments), intent(in)  :: gchan, echan, cpt_capacitance

        type(stimset), intent(in) :: stim

        integer, dimension(nconnections), intent(in) :: con_from, con_to
        real, dimension(nconnections), intent(in) :: con_conductance, con_capacitance

        integer, dimension(nfixed), intent(in) :: con_fixed

        real, intent(in) :: dt, ftimediff
        real, dimension(ncompartments), intent(inout) :: v


        ! automatic work arrays POSERR could make these doubles
        real, dimension(ncompartments) :: diag, rhs
        real, dimension(nconnections) :: offdiag

        real :: fcn, a, b, g

        integer :: i, i_from, i_to, icpt, p
        real :: f

        ! time difference weighting: if less than 0 means unspecified so use the default
        fcn = ftimediff
        if (fcn .lt. 0.) then
            fcn = 0.51
        end if

        v(stim%pvc) = stim%vcvals

        ! PERFORMANCE - the data parallel equivalents of these were significantly slower on
        ! casual tests but this could be revisited for other architectures
        do i = 1, ncompartments
            a = dt * gchan(i)
           rhs(i) = a * (echan(i) - v(i))
           diag(i) = cpt_capacitance(i) + fcn * a
        end do

        do i = 1, nconnections
           a = dt * con_conductance(i)
           offdiag(i) = -fcn * (con_capacitance(i) + a)
        end do

        ! do loop since there may be multiple current injections on a single site
    !	rhs(pcc) = rhs(pcc) + dt * ccvals;
        do i = 1, stim%ncc
            rhs(stim%pcc(i)) = rhs(stim%pcc(i)) + dt * stim%ccvals(i);
        end do

        do i = 1, stim%ngc
            g = stim%gcvals(i)
            p = stim%pgc(i)
            rhs(p) = rhs(p) + dt * g * (stim%gcdrive(i) - v(p))
            diag(p) = diag(p) + fcn * dt * g
        end do

        do i = 1, nconnections
           b = dt * con_conductance(i) * (v(con_from(i)) - v(con_to(i)))
           rhs(con_to(i)) = rhs(con_to(i)) + b
           rhs(con_from(i)) = rhs(con_from(i)) - b
           diag(con_to(i)) = diag(con_to(i)) - offdiag(i)
           diag(con_from(i)) = diag(con_from(i)) - offdiag(i)
        end do

		! for voltage clamped compartments, the corresponding row and colum of the matrix
		! get replaced with a trivial equation
        diag(stim%pvc) = 1
        rhs(stim%pvc) = 0
        offdiag(con_fixed) = 0




        ! forward sweep eliminating points to the right of the leading diagonal
        do i = nconnections, 1, -1
            i_from = con_from(i)
            i_to = con_to(i)
            f = offdiag(i) / diag(i_to)
            rhs(i_from) = rhs(i_from) - f * rhs(i_to)
            diag(i_from) = diag(i_from) - f * offdiag(i)
        end do

    !   diag(stim%pvc) = 1
    !   rhs(stim%pvc) = 0

        ! backsubstitute, working by non-zero element, not by row. The con_tos are in order
        icpt = 1
        do i = 1, nconnections
            if (con_to(i) .ne. icpt) then
                rhs(icpt) = rhs(icpt) / diag(icpt)
                icpt = con_to(icpt)
            end if
            rhs(con_to(i)) = rhs(con_to(i)) - offdiag(i) * rhs(con_from(i))
        end do
        rhs(ncompartments) = rhs(ncompartments) / diag(ncompartments)

        ! thats it: now the deltaVs are stored in rhs
        v = v + rhs
    end subroutine vupdate


end module passprop
