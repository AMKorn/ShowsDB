package com.andreas.showsdb.repository;

import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SeasonsRepository extends JpaRepository<Season, Long> {
    List<Season> findByShow(Show show);
    List<Season> findByShowId(long showId);

    Optional<Season> findByShowAndNumber(Show show, int number);
    Optional<Season> findByShowIdAndNumber(long showId, int number);

    @Modifying
    @Transactional
    void deleteAllByShow(Show show);
    @Modifying
    @Transactional
    void deleteAllByShowId(long showId);
}
