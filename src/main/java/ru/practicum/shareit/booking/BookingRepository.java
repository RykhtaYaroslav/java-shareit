package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
