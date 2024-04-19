package com.andreas.showsdb.controller;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.dto.SeasonInfo;
import com.andreas.showsdb.model.dto.SeasonInput;
import com.andreas.showsdb.service.SeasonsService;
import com.andreas.showsdb.service.ShowsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/shows/{showId}/seasons")
public class SeasonsController {
    @Autowired
    private SeasonsService seasonsService;
    @Autowired
    private ShowsService showsService;

    @GetMapping("")
    public ResponseEntity<?> getAllByShow(@PathVariable("showId") long showId) {
        try {
            List<SeasonInfo> season = seasonsService.findByShow(showId);
            return ResponseEntity.ok(season);
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @PostMapping("")
    public ResponseEntity<?> create(@PathVariable("showId") long showId,
                                    @RequestBody(required = false) SeasonInput seasonInput) {
        try {
            if (seasonInput == null || seasonInput.getSeasonNumber() == null) {
                SeasonInfo savedSeason = seasonsService.createInShow(showId);
                return new ResponseEntity<>(savedSeason, HttpStatus.CREATED);
            }

            try {
                SeasonInfo savedSeason = seasonsService.save(showId, seasonInput);
                return new ResponseEntity<>(savedSeason, HttpStatus.CREATED);
            } catch (DataIntegrityViolationException e) {
                Map<String, Object> response = new HashMap<>();
                Optional<@Valid SeasonInfo> optionalSeason = seasonsService.findByShow(showId).stream()
                        .filter(s -> s.getSeasonNumber().equals(seasonInput.getSeasonNumber()))
                        .findFirst();
                response.put("message",
                        "Show already has a Season " + seasonInput.getSeasonNumber());
                response.put("season", optionalSeason.orElseThrow());
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            }
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @GetMapping("/{seasonNumber}")
    public ResponseEntity<?> get(@PathVariable("showId") long showId,
                                 @PathVariable("seasonNumber") int seasonNumber) {
        try {
            SeasonInfo seasonInfo = seasonsService.findByShowAndNumber(showId, seasonNumber);
            return new ResponseEntity<>(seasonInfo, HttpStatus.OK);
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @DeleteMapping("/{seasonNumber}")
    public ResponseEntity<?> delete(@PathVariable("showId") long showId,
                                    @PathVariable("seasonNumber") Integer seasonNumber) {
        try {
            seasonsService.delete(showId, seasonNumber);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteSeasons(@PathVariable("showId") long showId) {
        try {
            seasonsService.deleteByShow(showId);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }
}
