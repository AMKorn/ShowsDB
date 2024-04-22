package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.model.dto.SeasonOutputDto;
import com.andreas.showsdb.model.dto.SeasonInputDto;
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

    public List<@Valid SeasonOutputDto> findByShow(long showId) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));

        return seasonsRepository.findByShow(show).stream()
                .map(Season::getInfoDto).toList();
    }

    public @Valid SeasonOutputDto findByShowAndNumber(long showId, int seasonNumber) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        return seasonsRepository.findByShowAndNumber(show, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"))
                .getInfoDto();
    }

    public @Valid SeasonOutputDto save(long showId, @Valid SeasonInputDto seasonInputDto) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Season season = Season.builder()
                .show(show)
                .number(seasonInputDto.getSeasonNumber())
                .build();

        return seasonsRepository.save(season).getInfoDto();
    }

    public @Valid SeasonOutputDto createInShow(long showId) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        int seasonNumber;
        try {
            seasonNumber = seasonsRepository.findByShow(show).stream()
                    .max(Season::compareTo)
                    .orElseThrow()
                    .getNumber() + 1;
        } catch (NoSuchElementException e) {
            seasonNumber = 1;
        }

        Season season = Season.builder()
                .show(show)
                .number(seasonNumber)
                .build();

        return seasonsRepository.save(season).getInfoDto();
    }

    public void delete(long showId, int seasonNumber) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Season season = seasonsRepository.findByShowAndNumber(show, seasonNumber)
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
