package com.example.trainapplication.services;

import com.example.trainapplication.model.User;

import java.util.List;

public interface IUserService {

    public List<User> getAllUsers();
    public User getUserById(Long id);
    public User getUserByUsername(String username);
    public User getUserByEmail(String email);
    public User addUser(User user);
    public User updateUser(Long id, User user);
    public void deleteUserById(Long id);
    public User authenticate(String username, String password);
}
