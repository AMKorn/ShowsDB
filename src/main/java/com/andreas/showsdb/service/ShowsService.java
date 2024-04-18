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
//        Optional<Show> show = showsRepository.findById(id);
//        deleteShowSeasons(show.orElseThrow());
        showsRepository.deleteById(id);
    }
}
