package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Episode;
import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.service.ShowsService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/shows")
public class ShowsController {

    @Autowired
    private ShowsService showsService;

    @GetMapping("")
    public List<Show> searchAll() {
        return showsService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getShow(@PathVariable("id") long id) {
        try {
            return ResponseEntity.ok(showsService.findById(id).orElseThrow());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public Show createShow(@RequestBody Show show) {
        return showsService.save(show);
    }

    @PutMapping("")
    public ResponseEntity<?> modifyShow(@RequestBody Show show) {
        try {
            findShow(show.getId());
        } catch (NotFoundException e) {
            return e.getResponse();
        }

        return ResponseEntity.ok(showsService.save(show));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShow(@PathVariable("id") long id) {
        if (showsService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        showsService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/seasons")
    public ResponseEntity<?> findShowSeasons(@PathVariable("id") long id) {
        Optional<Show> show = showsService.findById(id);
        if (show.isEmpty())
            return ResponseEntity.notFound().build();

        List<Season> season = showsService.getShowSeasons(show.get());
        return ResponseEntity.ok(season);
    }

    @PostMapping("/{id}/seasons")
    public ResponseEntity<?> addShowSeason(@PathVariable("id") long id, @RequestBody(required = false) Season season) {
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

    @GetMapping("/{showId}/seasons/{seasonNumber}")
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

    @DeleteMapping("/{showId}/seasons/{seasonNumber}")
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
        } catch (NotFoundException e) {
            return e.getResponse();
        }

        showsService.deleteSeason(season);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{showId}/seasons")
    public ResponseEntity<?> deleteSeasons(@PathVariable("showId") long showId) {
        Show show;
        try {
            show = findShow(showId);
        } catch (NotFoundException e) {
            return e.getResponse();
        }

        showsService.deleteShowSeasons(show);

        return ResponseEntity.ok().build();
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
