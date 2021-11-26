package com.example.api.user;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByLogin (String login);
    Optional<User> findByEmail (String email);
    Optional<User> findById (long id);
}
