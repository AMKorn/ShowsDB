package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.repository.SeasonsRepository;
import com.andreas.showsdb.repository.ShowsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ShowsService {

    @Autowired
    private ShowsRepository showsRepository;

    @Autowired
    private SeasonsRepository seasonsRepository;

    @Autowired
    private EpisodesService episodesService;

    public List<Show> findAll() {
        return showsRepository.findAll();
    }

    public Optional<Show> findById(long id) {
        return showsRepository.findById(id);
    }

    public Show save(Show show) {
        return showsRepository.save(show);
    }

    public void deleteById(long id) {
        Optional<Show> show = showsRepository.findById(id);
        deleteShowSeasons(show.orElseThrow());
        showsRepository.deleteById(id);
    }

    public List<Season> getShowSeasons(Show show) {
        return seasonsRepository.findByShow(show);
    }

    public Optional<Season> getShowSeason(Show show, Integer seasonNumber) {
        return seasonsRepository.findByShowAndSeasonNumber(show, seasonNumber);
    }

    public Season saveSeason(Season season) {
        return seasonsRepository.save(season);
    }

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

        Season season = Season.builder()
                .show(show)
                .seasonNumber(seasonNumber)
                .build();
        return seasonsRepository.save(season);
    }

    public void deleteSeason(Season season) throws NotFoundException {
        episodesService.deleteAllBySeason(season.getShow().getId(), season.getSeasonNumber());

        seasonsRepository.deleteById(season.getId());
    }

    public void deleteShowSeasons(Show show) {
        try {
            for (Season season : seasonsRepository.findByShow(show)) {
                deleteSeason(season);
            }
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
