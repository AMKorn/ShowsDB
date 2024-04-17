package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.service.MainCastService;
import com.andreas.showsdb.service.ShowsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/shows")
public class ShowsController {

    @Autowired
    private ShowsService showsService;

    @Autowired
    private MainCastService mainCastService;

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
    public ResponseEntity<?> createShow(@RequestBody Show show) {
        if (show.getId() != null && showsService.findById(show.getId()).isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Show already exists with that id");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        Show saved = showsService.save(show);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("")
    public ResponseEntity<?> modifyShow(@RequestBody Show show) {
        if (show.getId() == null || showsService.findById(show.getId()).isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Show does not exist");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
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

    @GetMapping("/{id}/main-cast")
    public ResponseEntity<?> getShowMainCast(@PathVariable("id") long id) {
        Show show;
        try {
            show = showsService.findById(id).orElseThrow(() -> new ShowsDatabaseException("Show not found"));
        } catch (ShowsDatabaseException e) {
            return e.getResponse();
        }

        List<MainCast> mainCasts = mainCastService.findMainCastByShow(show);
        return ResponseEntity.ok(mainCasts);
    }
}
