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
@RequestMapping("/api")
public class ShowsController {

    @Autowired
    private ShowsService showsService;

    @GetMapping("/shows")
    public List<Show> searchAll() {
        return showsService.findAll();
    }

    @GetMapping("/shows/{id}")
    public ResponseEntity<?> getShow(@PathVariable("id") long id) {
        try {
            return ResponseEntity.ok(showsService.findById(id).orElseThrow());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/shows")
    @ResponseStatus(HttpStatus.CREATED)
    public Show createShow(@RequestBody Show show) {
        return showsService.save(show);
    }

    @PutMapping("/shows")
    public Show modifyShow(@RequestBody Show show) {
        return showsService.save(show);
    }

    @DeleteMapping("/shows/{id}")
    public void deleteShow(@PathVariable("id") long id) {
        showsService.deleteById(id);
    }

    @GetMapping("/shows/{id}/seasons")
    public ResponseEntity<?> findShowSeasons(@PathVariable("id") long id) {
        Optional<Show> show = showsService.findById(id);
        if (show.isEmpty())
            return ResponseEntity.notFound().build();

        List<Season> season = showsService.getShowSeasons(show.get());
        return ResponseEntity.ok(season);
    }

    @PostMapping("/shows/{id}/seasons")
    public ResponseEntity<?> addShowSeason(@PathVariable("id") long id, @RequestBody Season season) {
        Optional<Show> show = showsService.findById(id);
        if (show.isEmpty())
            return ResponseEntity.notFound().build();

        season.setShow(show.orElseThrow());
        try {
            Season savedSeason = showsService.saveSeason(season);
            return new ResponseEntity<>(savedSeason, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            Map<String, Object> response = new HashMap<>();
            Optional<Season> optionalSeason = showsService.getShowSeasons(show.get()).stream()
                    .filter(s -> s.getSeasonNumber().equals(season.getSeasonNumber()))
                    .findFirst();
            response.put("message",
                    "Show '" + show.get().getName() + "' already has a Season " + season.getSeasonNumber());
            response.put("season", optionalSeason.orElseThrow());
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }
}
