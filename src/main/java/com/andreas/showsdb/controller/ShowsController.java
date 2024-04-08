package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.service.ShowsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

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
}
