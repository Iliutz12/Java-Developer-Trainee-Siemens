package com.example.trainapplication.repositories;

import com.example.trainapplication.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IStationRepository extends JpaRepository<Station, Long> {

    Station findByName(String name);
}
