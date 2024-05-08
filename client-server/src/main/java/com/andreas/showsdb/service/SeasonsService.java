package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.model.dto.SeasonInputDto;
import com.andreas.showsdb.model.dto.SeasonOutputDto;
import com.andreas.showsdb.repository.SeasonsRepository;
import com.andreas.showsdb.repository.ShowsRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SeasonsService {
    private final ShowsRepository showsRepository;
    private final SeasonsRepository seasonsRepository;

    public List<SeasonOutputDto> findByShow(long showId) {
        return seasonsRepository.findByShowId(showId).stream()
                .map(Season::getInfoDto).toList();
    }

    public SeasonOutputDto findByShowAndNumber(long showId, int seasonNumber) throws NotFoundException {
        return seasonsRepository.findByShowIdAndNumber(showId, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"))
                .getInfoDto();
    }

    public SeasonOutputDto save(long showId, @Valid SeasonInputDto seasonInputDto) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Season season = Season.builder()
                .show(show)
                .number(seasonInputDto.getSeasonNumber())
                .build();

        return seasonsRepository.save(season).getInfoDto();
    }

    public SeasonOutputDto createInShow(long showId) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        int seasonNumber;
        try {
            seasonNumber = seasonsRepository.findByShowId(showId).stream()
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

    public void delete(long showId, int seasonNumber) {
        seasonsRepository.deleteByShowIdAndNumber(showId, seasonNumber);
    }

    public void deleteByShow(long showId) {
        seasonsRepository.deleteAllByShowId(showId);
    }

}
