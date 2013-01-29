
module matrix



contains

    subroutine sort_indexes(col, isrt)
        real, dimension(:), intent(in) :: col
        integer, dimension(:), intent(out) :: isrt

        integer :: i, k, iin

        isrt(1) = 1

        do i = 2, size(col)
            iin = 1
            do
                if (iin .ge. i .or. col(i) .lt. col(isrt(iin))) exit
                iin = iin + 1
            end do

            do k = i-1, iin, -1
                isrt(k+1) = isrt(k)
            end do
            isrt(iin) = i
        end do

    end subroutine sort_indexes





    function expmatrix(n, m)
        integer, intent(in) :: n
        real, dimension(n, n), intent(in) :: m
        real, dimension(n, n) :: expmatrix, w
        real, parameter :: eps = 1.0e-12;
        real :: d, f
        integer p, i

        d = maxval(abs(m))
        p = 0
        f = 1
        do
            if (d * f < eps) exit
            f = f * 0.5
            p = p + 1
        end do

        w = f * m
        do i = 1, p
            w = w + w + matmul(w, w)
        end do
        do i = 1, n
            w(i,i) = w(i,i) + 1
        end do
        expmatrix = w
    end function expmatrix



    function ev1vec(n, m, pow)
        ! find the vector with eigenvalue 1., assuming it exists... or equivalently
        ! the null space of M-I, which is assumed to have dimension 1; actually just
        ! take a large power of the Matrix ***

        integer, intent(in) :: n, pow
        real, dimension(n, n), intent(in) :: m
        real, dimension(n, n) :: w
        real, dimension(n) :: ev1vec

        integer :: i

        w = m
        do i = 1, pow
            w = matmul(w, w)
        end do
        ev1vec = 1. / n
        ev1vec = matmul(w, ev1vec)


        if (abs(sum(ev1vec) - 1) .gt. 0.01) then
            print *, "WARNING: eigenvector calculation should sum to 1 but got ", sum(ev1vec)
        end if

        ev1vec = ev1vec / sum(ev1vec)
     end function ev1vec


    function random_index(n, v)
        integer :: n
        real, dimension(n) :: v
        integer :: random_index
        real :: r
        random_index = 1
        call random_number(r)
        do
            r = r - v(random_index)
            if (r .le. 0) exit
            random_index = random_index + 1
        end do
    end function random_index





end module matrix
