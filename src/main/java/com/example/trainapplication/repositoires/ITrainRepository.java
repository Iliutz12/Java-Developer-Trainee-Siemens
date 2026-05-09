package com.example.trainapplication.repositoires;

import com.example.trainapplication.model.Train;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ITrainRepository extends JpaRepository<Train, Long> {

    Train findByName(String name);

    Optional<Train> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Train t WHERE t.id = :id")
    Optional<Train> findByIdForUpdate(@Param("id") Long id);
}