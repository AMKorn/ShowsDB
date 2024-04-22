package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Episode;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.model.dto.EpisodeOutputDto;
import com.andreas.showsdb.model.dto.EpisodeInputDto;
import com.andreas.showsdb.repository.EpisodesRepository;
import com.andreas.showsdb.repository.SeasonsRepository;
import com.andreas.showsdb.repository.ShowsRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EpisodesService {

    private final EpisodesRepository episodesRepository;
    private final SeasonsRepository seasonsRepository;
    private final ShowsRepository showsRepository;

    public EpisodesService(EpisodesRepository episodesRepository, SeasonsRepository seasonsRepository, ShowsRepository showsRepository) {
        this.episodesRepository = episodesRepository;
        this.seasonsRepository = seasonsRepository;
        this.showsRepository = showsRepository;
    }

    public List<@Valid EpisodeOutputDto> findBySeason(long showId, int seasonNumber) {
        return episodesRepository.findBySeasonShowIdAndSeasonNumber(showId, seasonNumber).stream()
                .map(Episode::getInfoDto).toList();
    }

    public @Valid EpisodeOutputDto findByShowAndSeasonAndEpisodeNumbers(long showId, int seasonNumber, int episodeNumber)
            throws NotFoundException {
        return episodesRepository.findBySeasonShowIdAndSeasonNumberAndNumber(showId, seasonNumber, episodeNumber)
                .orElseThrow(() -> new NotFoundException("Episode not found"))
                .getInfoDto();
    }

    public @Valid EpisodeOutputDto save(long showId, int seasonNumber, EpisodeInputDto episodeInputDto) throws NotFoundException {
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

    public @Valid EpisodeOutputDto modify(long showId, int seasonNumber, @Valid EpisodeInputDto episodeInputDto)
            throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Season season = seasonsRepository.findByShowAndNumber(show, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"));
        Episode episode = episodesRepository.findBySeasonAndNumber(season, episodeInputDto.getEpisodeNumber())
                .orElseThrow(() -> new NotFoundException("Episode not found or trying to modify episode number."));

        episode.setName(episodeInputDto.getName());
        episode.setReleaseDate(episodeInputDto.getReleaseDate());

        return episodesRepository.save(episode).getInfoDto();
    }

    public @Valid EpisodeOutputDto createInSeason(long showId, int seasonNumber) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Season season = seasonsRepository.findByShowAndNumber(show, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"));

        int episodeNumber;
        try {
            episodeNumber = episodesRepository.findBySeason(season).stream()
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

    public void deleteByShowAndSeasonAndEpisodeNumbers(long showId, int seasonNumber, int episodeNumber)
            throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Season season = seasonsRepository.findByShowAndNumber(show, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"));
        Episode episode = episodesRepository.findBySeasonAndNumber(season, episodeNumber)
                .orElseThrow(() -> new NotFoundException("Episode not found"));
        episodesRepository.deleteById(episode.getId());
    }

    public void deleteAllBySeason(long showId, int seasonNumber) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Season season = seasonsRepository.findByShowAndNumber(show, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"));

        episodesRepository.findBySeason(season)
                .stream().map(Episode::getId)
                .forEach(episodesRepository::deleteById);
    }
}
