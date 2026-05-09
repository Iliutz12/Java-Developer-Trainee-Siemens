package com.example.trainapplication.repositoires;

import com.example.trainapplication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);
    User findByUsername(String username);
}
