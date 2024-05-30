package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Episode;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.dto.EpisodeInputDto;
import com.andreas.showsdb.model.dto.EpisodeOutputDto;
import com.andreas.showsdb.repository.EpisodesRepository;
import com.andreas.showsdb.repository.SeasonsRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class EpisodesService {
    private static final Logger logger = LoggerFactory.getLogger(EpisodesService.class);

    private final EpisodesRepository episodesRepository;
    private final SeasonsRepository seasonsRepository;

    @Cacheable("findEpisodesBySeason")
    public List<EpisodeOutputDto> findBySeason(long showId, int seasonNumber) {
        return episodesRepository.findBySeasonShowIdAndSeasonNumber(showId, seasonNumber).stream()
                .map(Episode::getInfoDto).toList();
    }

    @Cacheable("findEpisode")
    public EpisodeOutputDto findByShowAndSeasonAndEpisodeNumbers(long showId, int seasonNumber, int episodeNumber)
            throws NotFoundException {
        return episodesRepository.findBySeasonShowIdAndSeasonNumberAndNumber(showId, seasonNumber, episodeNumber)
                .orElseThrow(NotFoundException::new)
                .getInfoDto();
    }

    @CacheEvict(cacheNames = {"findEpisodesBySeason", "findEpisode"}, allEntries = true)
    public EpisodeOutputDto save(long showId, int seasonNumber, EpisodeInputDto episodeInputDto)
            throws NotFoundException {
        Season season = seasonsRepository.findByShowIdAndNumber(showId, seasonNumber)
                .orElseThrow(NotFoundException::new);

        Episode episode = Episode.builder()
                .season(season)
                .name(episodeInputDto.getName())
                .number(episodeInputDto.getEpisodeNumber())
                .releaseDate(episodeInputDto.getReleaseDate())
                .build();

        return episodesRepository.save(episode).getInfoDto();
    }

    @CacheEvict(cacheNames = {"findEpisodesBySeason", "findEpisode"}, allEntries = true)
    public EpisodeOutputDto modify(long showId, int seasonNumber, @Valid EpisodeInputDto episodeInputDto)
            throws NotFoundException {
        Episode episode = episodesRepository.findBySeasonShowIdAndSeasonNumberAndNumber(showId, seasonNumber,
                        episodeInputDto.getEpisodeNumber())
                .orElseThrow(() -> new NotFoundException("Episode not found or trying to modify episode number."));

        episode.setName(episodeInputDto.getName());
        episode.setReleaseDate(episodeInputDto.getReleaseDate());

        return episodesRepository.save(episode).getInfoDto();
    }

    @CacheEvict(cacheNames = {"findEpisodesBySeason", "findEpisode"}, allEntries = true)
    public EpisodeOutputDto createInSeason(long showId, int seasonNumber) throws NotFoundException {
        Season season = seasonsRepository.findByShowIdAndNumber(showId, seasonNumber)
                .orElseThrow(NotFoundException::new);

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

    @CacheEvict(cacheNames = {"findEpisodesBySeason", "findEpisode"}, allEntries = true)
    public void deleteByShowAndSeasonAndEpisodeNumbers(long showId, int seasonNumber, int episodeNumber) {
        episodesRepository.deleteBySeasonShowIdAndSeasonNumberAndNumber(showId, seasonNumber, episodeNumber);
    }

    @CacheEvict(cacheNames = {"findEpisodesBySeason", "findEpisode"}, allEntries = true)
    public void deleteAllBySeason(long showId, int seasonNumber) {
        try {
            episodesRepository.findBySeasonShowIdAndSeasonNumber(showId, seasonNumber)
                    .stream().map(Episode::getId)
                    .forEach(episodesRepository::deleteById);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
