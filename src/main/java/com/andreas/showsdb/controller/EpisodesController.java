package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Episode;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.service.ShowsService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/shows/{showId}/seasons/{seasonNumber}/episodes")
public class EpisodesController {

    @Autowired
    private ShowsService showsService;

    @PostMapping("")
    public ResponseEntity<?> addEpisode(@PathVariable("showId") long showId,
                                        @PathVariable("seasonNumber") int seasonNumber,
                                        @RequestBody Episode episode) {
        Season season;
        try {
            season = findSeason(showId, seasonNumber);
        } catch (NotFoundException e) {
            return e.getResponse();
        }

        if (episode == null || episode.getEpisodeNumber() == null) {
            Episode savedEpisode = showsService.addSeasonEpisode(season);
            return new ResponseEntity<>(savedEpisode, HttpStatus.CREATED);
        }

        episode.setSeason(season);

        Episode savedEpisode = showsService.saveEpisode(episode);
        return new ResponseEntity<>(savedEpisode, HttpStatus.CREATED);

    }

    @GetMapping("")
    public ResponseEntity<?> getShowEpisodes(@PathVariable("showId") long showId,
                                             @PathVariable("seasonNumber") int seasonNumber) {
        Season season;
        try {
            season = findSeason(showId, seasonNumber);
        } catch (NotFoundException e) {
            return e.getResponse();
        }

        List<Episode> episodes = season.getEpisodes();
        return ResponseEntity.ok(episodes);
    }

    @GetMapping("/{episodeNumber}")
    public ResponseEntity<?> getEpisode(@PathVariable("showId") long showId,
                                        @PathVariable("seasonNumber") int seasonNumber,
                                        @PathVariable("episodeNumber") int episodeNumber) {
        Season season;
        try {
            season = findSeason(showId, seasonNumber);
        } catch (NotFoundException e) {
            return e.getResponse();
        }

        Optional<Episode> optionalEpisode = showsService.getSeasonEpisode(season, episodeNumber);
        try {
            Episode episode = optionalEpisode.orElseThrow();
            return ResponseEntity.ok(episode);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Show findShow(long showId) throws NotFoundException {
        return showsService.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show does not exist"));
    }

    Season findSeason(long showId, int seasonNumber) throws NotFoundException {
        return showsService.getShowSeason(findShow(showId), seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season does not exist"));
    }


}
