package com.example.trainapplication.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "train_id")
    private Train train;

    private Integer numberOfTickets;

    @Column(name = "departure_station")
    private String departureStation;

    @Column(name = "arrival_station")
    private String arrivalStation;

    @Column(name = "departure_time")
    private LocalTime departureTime;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    public Booking(User user, Train train, Integer numberOfTickets) {
        this.user = user;
        this.train = train;
        this.numberOfTickets = numberOfTickets;
    }
}