package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.service.ActorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/actors")
public class ActorsController {

    @Autowired
    private ActorsService actorsService;

    @GetMapping("")
    public List<Actor> getActors() {
        return actorsService.findAll();
    }

    @GetMapping("/{actorId}")
    public ResponseEntity<?> getActor(@PathVariable("actorId") long id) {
        try {
            return ResponseEntity.ok(actorsService.findById(id).orElseThrow());
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createActor(@RequestBody Actor actor) {
        if (actor.getId() != null && actorsService.findById(actor.getId()).isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Actor already exists with that id");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        Actor saved = actorsService.save(actor);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("")
    public ResponseEntity<?> modifyActor(@RequestBody Actor actor) {
        if (actor.getId() == null || actorsService.findById(actor.getId()).isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Actor does not exist");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(actorsService.save(actor));
    }

    @DeleteMapping("/{actorId}")
    public ResponseEntity<?> deleteActor(@PathVariable("actorId") long id) {
        if (actorsService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        actorsService.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
