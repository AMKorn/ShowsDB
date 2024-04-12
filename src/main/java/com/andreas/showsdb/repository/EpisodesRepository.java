package com.andreas.showsdb.repository;

import com.andreas.showsdb.model.Episode;
import com.andreas.showsdb.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EpisodesRepository extends JpaRepository<Episode, Long> {
    List<Episode> findBySeason(Season season);

    Optional<Episode> findBySeasonAndEpisodeNumber(Season season, int episodeNumber);
}
