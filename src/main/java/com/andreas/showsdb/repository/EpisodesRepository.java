package com.andreas.showsdb.repository;

import com.andreas.showsdb.model.Episode;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface EpisodesRepository extends JpaRepository<Episode, Long> {
    List<Episode> findBySeasonShowIdAndSeasonNumber(long showId, int seasonNumber);

    Optional<Episode> findBySeasonShowIdAndSeasonNumberAndNumber(long showId, int seasonNumber, int episodeNumber);

    @Transactional
    void deleteBySeasonShowIdAndSeasonNumberAndNumber(long showId, int seasonNumber, int episodeNumber);
}
