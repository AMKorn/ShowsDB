package com.andreas.showsdb.repository;

import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeasonsRepository extends JpaRepository<Season, Long> {
    List<Season> findByShow(Show show);

    Optional<Season> findByShowAndSeasonNumber(Show show, int seasonNumber);
}
