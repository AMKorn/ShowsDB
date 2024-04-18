package com.andreas.showsdb.controller;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.service.ShowsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/shows/{showId}/seasons")
public class SeasonsController {
    @Autowired
    private ShowsService showsService;

    @GetMapping("")
    public ResponseEntity<?> findShowSeasons(@PathVariable("showId") long id) {
        Show show;
        try {
            show = findShow(id);
        } catch (NotFoundException e) {
            return e.getResponse();
        }

        List<Season> season = showsService.getShowSeasons(show);
        return ResponseEntity.ok(season);
    }

    @PostMapping("")
    public ResponseEntity<?> addShowSeason(@PathVariable("showId") long id, @RequestBody(required = false) Season season) {
        Show show;
        try {
            show = findShow(id);
        } catch (NotFoundException e) {
            return e.getResponse();
        }

        if (season == null || season.getSeasonNumber() == null) {
            Season savedSeason = showsService.addShowSeason(show);
            return new ResponseEntity<>(savedSeason, HttpStatus.CREATED);
        }

        season.setShow(show);
        try {
            Season savedSeason = showsService.saveSeason(season);
            return new ResponseEntity<>(savedSeason, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            Map<String, Object> response = new HashMap<>();
            Optional<Season> optionalSeason = showsService.getShowSeasons(show).stream()
                    .filter(s -> s.getSeasonNumber().equals(season.getSeasonNumber()))
                    .findFirst();
            response.put("message",
                    "Show '" + show.getName() + "' already has a Season " + season.getSeasonNumber());
            response.put("season", optionalSeason.orElseThrow());
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/{seasonNumber}")
    public ResponseEntity<?> getSeason(@PathVariable("showId") Long showId,
                                       @PathVariable("seasonNumber") int seasonNumber) {
        Show show;
        try {
            show = findShow(showId);
        } catch (NotFoundException e) {
            return e.getResponse();
        }

        Optional<Season> optionalSeason = showsService.getShowSeason(show, seasonNumber);
        try {
            Season season = optionalSeason.orElseThrow();
            return new ResponseEntity<>(season, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{seasonNumber}")
    public ResponseEntity<?> deleteSeason(@PathVariable("showId") Long showId,
                                          @PathVariable("seasonNumber") Integer seasonNumber) {
        Show show;
        try {
            show = findShow(showId);
        } catch (NotFoundException e) {
            return e.getResponse();
        }

        Optional<Season> optionalSeason = showsService.getShowSeason(show, seasonNumber);
        Season season;
        try {
            season = optionalSeason.orElseThrow(() -> new NotFoundException("Season does not exist"));

            showsService.deleteSeason(season);
            return ResponseEntity.ok().build();

        } catch (NotFoundException e) {
            return e.getResponse();
        }


    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteSeasons(@PathVariable("showId") long showId) {
        try {
            Show show = findShow(showId);

            showsService.deleteShowSeasons(show);

            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    private Show findShow(long showId) throws NotFoundException {
        return showsService.findById(showId)
                .orElseThrow(() -> new NotFoundException("Show does not exist"));
    }
}
