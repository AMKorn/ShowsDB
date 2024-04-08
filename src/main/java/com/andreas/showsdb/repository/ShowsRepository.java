package com.andreas.showsdb.repository;

import com.andreas.showsdb.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowsRepository extends JpaRepository<Show, Long> {
    List<Show> findByName(String name);
}
