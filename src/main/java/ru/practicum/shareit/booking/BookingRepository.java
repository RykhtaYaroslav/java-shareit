package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.id = :itemId
            AND b.startDate < :end
            AND b.endDate > :start
            AND b.status IN :statuses
            """)
    List<Booking> findBookingInPeriod(@Param("itemId") Long itemId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      @Param("statuses") List<BookingStatus> statuses);

    List<Booking> findAllByBookerIdOrderByStartDateDesc(Long userId);

    List<Booking> findAllByBookerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(Long userId, LocalDateTime nowForStart, LocalDateTime nowForEnd);

    List<Booking> findAllByBookerIdAndEndDateBeforeOrderByStartDateDesc(Long userId, LocalDateTime now);

    List<Booking> findAllByBookerIdAndStartDateAfterOrderByStartDateDesc(Long userId, LocalDateTime now);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDateDesc(Long userId, BookingStatus status);

    List<Booking> findAllByItemOwnerIdOrderByStartDateDesc(Long ownerId);

    List<Booking> findAllByItemOwnerIdAndStartDateBeforeAndEndDateAfterOrderByStartDateDesc(Long userId, LocalDateTime nowForStart, LocalDateTime nowForEnd);

    List<Booking> findAllByItemOwnerIdAndEndDateBeforeOrderByStartDateDesc(Long userId, LocalDateTime now);

    List<Booking> findAllByItemOwnerIdAndStartDateAfterOrderByStartDateDesc(Long userId, LocalDateTime now);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDateDesc(Long userId, BookingStatus status);

    @Query("""
            SELECT b FROM Booking as b
            WHERE b.item.id IN :itemIds
            AND b.status = 'APPROVED'
            AND b.startDate <= :now
            ORDER BY b.startDate DESC
            """)
    List<Booking> findPreviousBookingsForItems(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);

    @Query("""
            SELECT b FROM Booking as b
            WHERE b.item.id IN :itemIds
            AND b.status = 'APPROVED'
            AND b.startDate > :now
            ORDER BY b.startDate ASC
            """)
    List<Booking> findFeatureBookingsForItems(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);

    boolean existsByBookerIdAndItemIdAndStatusAndEndDateBefore(Long bookerId, Long itemId, BookingStatus status, LocalDateTime now);
}
