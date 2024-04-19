package com.andreas.showsdb.service;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Episode;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.model.dto.EpisodeInfo;
import com.andreas.showsdb.model.dto.EpisodeInput;
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

    @Autowired
    private EpisodesRepository episodesRepository;
    @Autowired
    private SeasonsRepository seasonsRepository;
    @Autowired
    private ShowsRepository showsRepository;

    public List<@Valid EpisodeInfo> findBySeason(long showId, int seasonNumber) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Season season = seasonsRepository.findByShowAndSeasonNumber(show, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"));
        return episodesRepository.findBySeason(season).stream()
                .map(Episode::getInfoDto).toList();
    }

    public @Valid EpisodeInfo findByShowAndSeasonAndEpisodeNumbers(long showId, int seasonNumber, int episodeNumber)
            throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Season season = seasonsRepository.findByShowAndSeasonNumber(show, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"));
        return episodesRepository.findBySeasonAndEpisodeNumber(season, episodeNumber)
                .orElseThrow(() -> new NotFoundException("Episode not found"))
                .getInfoDto();
    }

    public @Valid EpisodeInfo save(long showId, int seasonNumber, EpisodeInput episodeInput) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Season season = seasonsRepository.findByShowAndSeasonNumber(show, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"));

        Episode episode = Episode.builder()
                .season(season)
                .name(episodeInput.getName())
                .episodeNumber(episodeInput.getEpisodeNumber())
                .releaseDate(episodeInput.getReleaseDate())
                .build();

        return episodesRepository.save(episode).getInfoDto();
    }

    public @Valid EpisodeInfo modify(long showId, int seasonNumber, @Valid EpisodeInput episodeInput)
            throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Season season = seasonsRepository.findByShowAndSeasonNumber(show, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"));
        Episode episode = episodesRepository.findBySeasonAndEpisodeNumber(season, episodeInput.getEpisodeNumber())
                .orElseThrow(() -> new NotFoundException("Episode not found or trying to modify episode number."));

        episode.setName(episodeInput.getName());
        episode.setReleaseDate(episodeInput.getReleaseDate());

        return episodesRepository.save(episode).getInfoDto();
    }

    public @Valid EpisodeInfo createInSeason(long showId, int seasonNumber) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Season season = seasonsRepository.findByShowAndSeasonNumber(show, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"));

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
        return episodesRepository.save(episode).getInfoDto();
    }

    public void deleteByShowAndSeasonAndEpisodeNumbers(long showId, int seasonNumber, int episodeNumber)
            throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Season season = seasonsRepository.findByShowAndSeasonNumber(show, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"));
        Episode episode = episodesRepository.findBySeasonAndEpisodeNumber(season, episodeNumber)
                .orElseThrow(() -> new NotFoundException("Episode not found"));
        episodesRepository.deleteById(episode.getId());
    }

    public void deleteAllBySeason(long showId, int seasonNumber) throws NotFoundException {
        Show show = showsRepository.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show not found"));
        Season season = seasonsRepository.findByShowAndSeasonNumber(show, seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season not found"));

        episodesRepository.findBySeason(season)
                .stream().map(Episode::getId)
                .forEach(episodesRepository::deleteById);
    }
}
