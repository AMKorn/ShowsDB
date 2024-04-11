package com.andreas.showsdb.controller;

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

        if (showsService.findById(show.getId()).isEmpty()){
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Show does not exist");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(showsService.save(show));
    }

    @DeleteMapping("/{id}")
    public void deleteShow(@PathVariable("id") long id) {
        showsService.deleteById(id);
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
        Optional<Show> optionalShow = showsService.findById(id);
        if (optionalShow.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Show does not exist");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        Show show = optionalShow.get();

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
        Optional<Show> optionalShow = showsService.findById(showId);
        if (optionalShow.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Show does not exist");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        Show show = optionalShow.get();

        Optional<Season> optionalSeason = showsService.getShowSeason(show, seasonNumber);
        try {
            Season season = optionalSeason.orElseThrow();
            return new ResponseEntity<>(season, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }


}
