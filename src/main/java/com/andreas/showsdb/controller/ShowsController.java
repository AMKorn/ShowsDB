package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.repository.SeasonsRepository;
import com.andreas.showsdb.service.ShowsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ShowsController {

    @Autowired
    private ShowsService showsService;
    @Autowired
    private SeasonsRepository seasonsRepository;

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

        List<Season> season = seasonsRepository.findByShow(show.get());
        return ResponseEntity.ok(season);
    }

    @PostMapping("/shows/{id}/seasons")
    public ResponseEntity<?> addShowSeason(@PathVariable("id") long id, @RequestBody Season season){
        Optional<Show> show = showsService.findById(id);
        if (show.isEmpty())
            return ResponseEntity.notFound().build();

        season.setShow(show.orElseThrow());
        Season savedSeason = seasonsRepository.save(season);
        return new ResponseEntity<>(savedSeason, HttpStatus.CREATED);
    }
}
