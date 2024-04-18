package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.model.dto.SeasonInfo;
import com.andreas.showsdb.model.dto.SeasonInput;
import com.andreas.showsdb.repository.SeasonsRepository;
import com.andreas.showsdb.repository.ShowsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SeasonsService {
    @Autowired
    private ShowsRepository showsRepository;
    @Autowired
    private SeasonsRepository seasonsRepository;

    public List<@Valid SeasonInfo> findByShow(long showId) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));

        return seasonsRepository.findByShow(show).stream()
                .map(Season::getInfoDto).toList();
    }

    public @Valid SeasonInfo findByShowAndNumber(long showId, int seasonNumber) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        return seasonsRepository.findByShowAndSeasonNumber(show, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"))
                .getInfoDto();
    }

    public @Valid SeasonInfo save(long showId, @Valid SeasonInput seasonInput) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Season season = Season.builder()
                .show(show)
                .seasonNumber(seasonInput.getSeasonNumber())
                .build();

        return seasonsRepository.save(season).getInfoDto();
    }

    public @Valid SeasonInfo createInShow(long showId) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
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

        return seasonsRepository.save(season).getInfoDto();
    }

    public void delete(long showId, int seasonNumber) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Season season = seasonsRepository.findByShowAndSeasonNumber(show, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"));

        seasonsRepository.deleteById(season.getId());
    }

    public void deleteByShow(long showId) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));

        seasonsRepository.findByShow(show)
                .forEach(seasonsRepository::delete);
    }

}
