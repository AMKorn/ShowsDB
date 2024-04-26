package com.andreas.showsdb.repository;

import com.andreas.showsdb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
