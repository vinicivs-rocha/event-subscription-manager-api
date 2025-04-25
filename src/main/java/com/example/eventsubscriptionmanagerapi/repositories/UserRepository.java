package com.example.eventsubscriptionmanagerapi.repositories;

import com.example.eventsubscriptionmanagerapi.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {
    public Optional<User> findByEmail(String email);
}
