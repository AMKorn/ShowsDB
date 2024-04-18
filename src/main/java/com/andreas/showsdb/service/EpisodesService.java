package com.andreas.showsdb.service;

import com.andreas.showsdb.model.Episode;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.repository.EpisodesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class EpisodesService {

    @Autowired
    private EpisodesRepository episodesRepository;

    public List<Episode> findBySeason(Season season) {
        return episodesRepository.findBySeason(season);
    }

    public Optional<Episode> findBySeasonAndNumber(Season season, Integer episodeNumber) {
        return episodesRepository.findBySeasonAndEpisodeNumber(season, episodeNumber);
    }

    public Episode save(Episode episode) {
        return episodesRepository.save(episode);
    }

    public Episode createInSeason(Season season) {
        int episodeNumber;
        try {
            episodeNumber = episodesRepository.findBySeason(season).stream()
                    .max(Episode::compareTo)
                    .orElseThrow()
                    .getEpisodeNumber() + 1;
        } catch (NoSuchElementException e) {
            episodeNumber = 1;
        }

        Episode episode = new Episode();
        episode.setSeason(season);
        episode.setEpisodeNumber(episodeNumber);
        return episodesRepository.save(episode);
    }

    public void deleteById(Long episodeId) {
        episodesRepository.deleteById(episodeId);
    }

    public void deleteAllBySeason(Season season) {
        episodesRepository.findBySeason(season)
                .stream().map(Episode::getId)
                .forEach(this::deleteById);
    }
}
