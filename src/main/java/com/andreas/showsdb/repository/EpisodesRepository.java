package com.andreas.showsdb.repository;

import com.andreas.showsdb.model.Episode;
import com.andreas.showsdb.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EpisodesRepository extends JpaRepository<Episode, Long> {
    List<Episode> findBySeason(Season season);
    List<Episode> findBySeasonShowIdAndSeasonNumber(long showId, int seasonNumber);

    Optional<Episode> findBySeasonAndNumber(Season season, int episodeNumber);
    Optional<Episode> findBySeasonShowIdAndSeasonNumberAndNumber(long showId, int seasonNumber, int episodeNumber);
}
