package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Episode;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.service.ShowsService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class EpisodesController {

    @Autowired
    private ShowsService showsService;

    @PostMapping("/{showId}/seasons/{seasonNumber}/episodes")
    public ResponseEntity<?> addEpisode(@PathVariable("showId") long showId,
                                        @PathVariable("seasonNumber") int seasonNumber,
                                        @RequestBody Episode episode) {
        Season season;
        try {
            season = findSeason(showId, seasonNumber);
        } catch (NotFoundException e) {
            return e.getResponse();
        }

        episode.setSeason(season);

        return ResponseEntity.ok(showsService.saveEpisode(episode));

    }

    private Show findShow(long showId) throws NotFoundException {
        return showsService.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show does not exist"));
    }

    private Season findSeason(long showId, int seasonNumber) throws NotFoundException {
        return showsService.getShowSeason(findShow(showId), seasonNumber)
                .orElseThrow(() -> new NotFoundException("Season does not exist"));
    }

    @Getter
    private static class NotFoundException extends Exception {
        private final ResponseEntity<Map<?, ?>> response;

        public NotFoundException(String message) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", message); //"Show does not exist");
            this.response = new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

}
