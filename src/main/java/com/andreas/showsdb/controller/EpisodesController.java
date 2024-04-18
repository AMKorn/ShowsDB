package com.andreas.showsdb.controller;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.dto.EpisodeInfo;
import com.andreas.showsdb.model.dto.EpisodeInput;
import com.andreas.showsdb.service.EpisodesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/shows/{showId}/seasons/{seasonNumber}/episodes")
public class EpisodesController {
    @Autowired
    private EpisodesService episodesService;

    @PostMapping("")
    public ResponseEntity<?> create(@PathVariable("showId") long showId,
                                    @PathVariable("seasonNumber") int seasonNumber,
                                    @RequestBody(required = false) EpisodeInput episodeInput) {
        try {
            if (episodeInput == null) {
                EpisodeInfo savedEpisode = episodesService.createInSeason(showId, seasonNumber);
                return new ResponseEntity<>(savedEpisode, HttpStatus.CREATED);
            }

            // In case no episode number was stated, we put it by default as the next episode in the season.
            Integer episodeNumber = episodeInput.getEpisodeNumber();
            if (episodeNumber == null) {
                try {
                    episodeNumber = episodesService.findBySeason(showId, seasonNumber).stream()
                            .max(Comparator
                                    .comparingLong(EpisodeInfo::getShowId)
                                    .thenComparingInt(EpisodeInfo::getSeasonNumber)
                                    .thenComparingInt(EpisodeInfo::getEpisodeNumber))
                            .orElseThrow()
                            .getEpisodeNumber() + 1;
                } catch (NoSuchElementException e) {
                    episodeNumber = 1;
                }
                EpisodeInput oldEpisodeInput = episodeInput;

                episodeInput = EpisodeInput.builder()
                        .episodeNumber(episodeNumber)
                        .name(oldEpisodeInput.getName())
                        .releaseDate(oldEpisodeInput.getReleaseDate())
                        .build();
            }

            EpisodeInfo savedEpisode = episodesService.save(showId, seasonNumber, episodeInput);
            return new ResponseEntity<>(savedEpisode, HttpStatus.CREATED);
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @GetMapping("/{episodeNumber}")
    public ResponseEntity<?> get(@PathVariable("showId") long showId,
                                 @PathVariable("seasonNumber") int seasonNumber,
                                 @PathVariable("episodeNumber") int episodeNumber) {
        try {
            EpisodeInfo episodeInfo =
                    episodesService.findByShowAndSeasonAndEpisodeNumbers(showId, seasonNumber, episodeNumber);
            return ResponseEntity.ok(episodeInfo);
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getAllFromShow(@PathVariable("showId") long showId,
                                            @PathVariable("seasonNumber") int seasonNumber) {
        try {
            List<EpisodeInfo> episodesInfo = episodesService.findBySeason(showId, seasonNumber);
            return ResponseEntity.ok(episodesInfo);
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @PutMapping("")
    public ResponseEntity<?> modify(@PathVariable("showId") long showId,
                                    @PathVariable("seasonNumber") int seasonNumber,
                                    @RequestBody EpisodeInput episodeInput) {
        try {
            EpisodeInfo modifiedEpisode = episodesService.modify(showId, seasonNumber, episodeInput);
            return ResponseEntity.ok(modifiedEpisode);
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @DeleteMapping("/{episodeNumber}")
    public ResponseEntity<?> delete(@PathVariable("showId") long showId,
                                    @PathVariable("seasonNumber") int seasonNumber,
                                    @PathVariable("episodeNumber") int episodeNumber) {
        try {
            episodesService.deleteByShowAndSeasonAndEpisodeNumbers(showId, seasonNumber, episodeNumber);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteAll(@PathVariable("showId") long showId,
                                       @PathVariable("seasonNumber") int seasonNumber) {
        try {
            episodesService.deleteAllBySeason(showId, seasonNumber);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }
}
