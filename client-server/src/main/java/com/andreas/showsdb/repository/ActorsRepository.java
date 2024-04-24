package com.andreas.showsdb.repository;

import com.andreas.showsdb.model.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActorsRepository extends JpaRepository<Actor, Long> {
}
