package com.andreas.showsdb.repository;

import com.andreas.showsdb.model.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EpisodesRepository extends JpaRepository<Episode, Long> {
    List<Episode> findBySeasonShowIdAndSeasonNumber(long showId, int seasonNumber);

    Optional<Episode> findBySeasonShowIdAndSeasonNumberAndNumber(long showId, int seasonNumber, int episodeNumber);

    void deleteBySeasonShowIdAndSeasonNumberAndNumber(long showId, int seasonNumber, int episodeNumber);
}
