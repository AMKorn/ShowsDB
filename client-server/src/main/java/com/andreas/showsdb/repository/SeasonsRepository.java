package com.andreas.showsdb.repository;

import com.andreas.showsdb.model.Season;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface SeasonsRepository extends JpaRepository<Season, Long> {
    List<Season> findByShowId(long showId);

    Optional<Season> findByShowIdAndNumber(long showId, int number);

    @Modifying
    @Transactional
    void deleteAllByShowId(long showId);

    @Modifying
    @Transactional
    void deleteByShowIdAndNumber(long showId, int seasonNumber);
}
