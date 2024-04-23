package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Episode;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.dto.EpisodeInputDto;
import com.andreas.showsdb.model.dto.EpisodeOutputDto;
import com.andreas.showsdb.repository.EpisodesRepository;
import com.andreas.showsdb.repository.SeasonsRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EpisodesService {

    private final EpisodesRepository episodesRepository;
    private final SeasonsRepository seasonsRepository;

    public EpisodesService(EpisodesRepository episodesRepository,
                           SeasonsRepository seasonsRepository) {
        this.episodesRepository = episodesRepository;
        this.seasonsRepository = seasonsRepository;
    }

    public List<EpisodeOutputDto> findBySeason(long showId, int seasonNumber) {
        return episodesRepository.findBySeasonShowIdAndSeasonNumber(showId, seasonNumber).stream()
                .map(Episode::getInfoDto).toList();
    }

    public EpisodeOutputDto findByShowAndSeasonAndEpisodeNumbers(long showId, int seasonNumber, int episodeNumber)
            throws NotFoundException {
        return episodesRepository.findBySeasonShowIdAndSeasonNumberAndNumber(showId, seasonNumber, episodeNumber)
                .orElseThrow(() -> new NotFoundException("Episode not found"))
                .getInfoDto();
    }

    public EpisodeOutputDto save(long showId, int seasonNumber, EpisodeInputDto episodeInputDto)
            throws NotFoundException {
        Season season = seasonsRepository.findByShowIdAndNumber(showId, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"));

        Episode episode = Episode.builder()
                .season(season)
                .name(episodeInputDto.getName())
                .number(episodeInputDto.getEpisodeNumber())
                .releaseDate(episodeInputDto.getReleaseDate())
                .build();

        return episodesRepository.save(episode).getInfoDto();
    }

    public EpisodeOutputDto modify(long showId, int seasonNumber, @Valid EpisodeInputDto episodeInputDto)
            throws NotFoundException {
        Episode episode = episodesRepository.findBySeasonShowIdAndSeasonNumberAndNumber(showId, seasonNumber,
                        episodeInputDto.getEpisodeNumber())
                .orElseThrow(() -> new NotFoundException("Episode not found or trying to modify episode number."));

        episode.setName(episodeInputDto.getName());
        episode.setReleaseDate(episodeInputDto.getReleaseDate());

        return episodesRepository.save(episode).getInfoDto();
    }

    public EpisodeOutputDto createInSeason(long showId, int seasonNumber) throws NotFoundException {
        Season season = seasonsRepository.findByShowIdAndNumber(showId, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"));

        int episodeNumber;
        try {
            episodeNumber = episodesRepository.findBySeasonShowIdAndSeasonNumber(showId, seasonNumber).stream()
                    .max(Episode::compareTo)
                    .orElseThrow()
                    .getNumber() + 1;
        } catch (NoSuchElementException e) {
            episodeNumber = 1;
        }

        Episode episode = new Episode();
        episode.setSeason(season);
        episode.setNumber(episodeNumber);
        return episodesRepository.save(episode).getInfoDto();
    }

    public void deleteByShowAndSeasonAndEpisodeNumbers(long showId, int seasonNumber, int episodeNumber) {
        episodesRepository.deleteBySeasonShowIdAndSeasonNumberAndNumber(showId, seasonNumber, episodeNumber);
    }

    public void deleteAllBySeason(long showId, int seasonNumber) {

        episodesRepository.findBySeasonShowIdAndSeasonNumber(showId, seasonNumber)
                .stream().map(Episode::getId)
                .forEach(episodesRepository::deleteById);
    }
}
