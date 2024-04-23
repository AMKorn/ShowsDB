package com.andreas.showsdb.repository;

import com.andreas.showsdb.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowsRepository extends JpaRepository<Show, Long> {
}
