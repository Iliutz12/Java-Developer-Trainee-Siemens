package com.example.trainapplication.repositoires;

import com.example.trainapplication.model.Booking;
import com.example.trainapplication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IBookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT COALESCE(SUM(b.numberOfTickets), 0) FROM Booking b WHERE b.train.id = :trainId")
    Integer sumBookedTicketsByTrainId(@Param("trainId") Long trainId);

    Booking getBookingByUser(User user);
    List<Booking> findByTrainId(Long trainId);
    @Modifying
    @Query("DELETE FROM Booking b WHERE b.train.id = :trainId")
    void deleteByTrainId(Long trainId);
}
