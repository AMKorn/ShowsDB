package com.andreas.showsdb.service.jpa;

import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.repository.SeasonsRepository;
import com.andreas.showsdb.repository.ShowsRepository;
import com.andreas.showsdb.service.ShowsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShowsServiceJpa implements ShowsService {

    @Autowired
    private ShowsRepository showsRepository;

    @Autowired
    private SeasonsRepository seasonsRepository;

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
        seasonsRepository.save(season);
        return season;
    }

    @Override
    public Season addShowSeason(Show show) {
        int seasonNumber = seasonsRepository.findByShow(show).size() + 1;
        Season season = new Season(show, seasonNumber);
        return seasonsRepository.save(season);
    }

    @Override
    public void deleteSeason(Season season) {
        seasonsRepository.deleteById(season.getId());
    }
}
