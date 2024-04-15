package com.andreas.showsdb.service.jpa;

import com.andreas.showsdb.model.Episode;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.repository.EpisodesRepository;
import com.andreas.showsdb.repository.SeasonsRepository;
import com.andreas.showsdb.repository.ShowsRepository;
import com.andreas.showsdb.service.ShowsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ShowsServiceJpa implements ShowsService {

    @Autowired
    private ShowsRepository showsRepository;

    @Autowired
    private SeasonsRepository seasonsRepository;

    @Autowired
    private EpisodesRepository episodesRepository;

    @Override
    public List<Show> findAll() {
        return showsRepository.findAll();
    }

    @Override
    public Optional<Show> findById(long id) {
        return showsRepository.findById(id);
    }

    @Override
    public Show save(Show show) {
        return showsRepository.save(show);
    }

    @Override
    public void deleteById(long id) {
        Optional<Show> show = showsRepository.findById(id);
        deleteShowSeasons(show.orElseThrow());

        showsRepository.deleteById(id);
    }

    @Override
    public List<Season> getShowSeasons(Show show) {
        return seasonsRepository.findByShow(show);
    }

    @Override
    public Optional<Season> getShowSeason(Show show, Integer seasonNumber) {
        return seasonsRepository.findByShowAndSeasonNumber(show, seasonNumber);
    }

    @Override
    public Season saveSeason(Season season) {
        return seasonsRepository.save(season);
    }

    @Override
    public Season addShowSeason(Show show) {
        //.size() + 1;
        int seasonNumber;
        try {
            seasonNumber = seasonsRepository.findByShow(show).stream()
                    .max(Season::compareTo)
                    .orElseThrow()
                    .getSeasonNumber() + 1;
        } catch (NoSuchElementException e) {
            seasonNumber = 1;
        }

        Season season = new Season(show, seasonNumber);
        return seasonsRepository.save(season);
    }

    @Override
    public void deleteSeason(Season season) {
        deleteSeasonEpisodes(season);

        seasonsRepository.deleteById(season.getId());
    }

    @Override
    public void deleteShowSeasons(Show show) {
        seasonsRepository.findByShow(show)
                .forEach(this::deleteSeason);
    }

    @Override
    public List<Episode> getSeasonEpisodes(Season season) {
        return episodesRepository.findBySeason(season);
    }

    @Override
    public Optional<Episode> getSeasonEpisode(Season season, Integer episodeNumber) {
        return episodesRepository.findBySeasonAndEpisodeNumber(season, episodeNumber);
    }

    @Override
    public Episode saveEpisode(Episode episode) {
        return episodesRepository.save(episode);
    }

    @Override
    public Episode addSeasonEpisode(Season season) {
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

    @Override
    public void deleteEpisodeById(Long episodeId) {
        episodesRepository.deleteById(episodeId);
    }

    @Override
    public void deleteSeasonEpisodes(Season season) {
        episodesRepository.findBySeason(season)
                .stream().map(Episode::getId)
                .forEach(this::deleteEpisodeById);
    }
}
