package com.andreas.showsdb.controller;

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
        if (showsService.findById(show.getId()).isEmpty()) {
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


}
