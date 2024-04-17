package com.andreas.showsdb.controller;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.model.dto.ActorDto;
import com.andreas.showsdb.model.dto.ActorDtoId;
import com.andreas.showsdb.service.ActorsService;
import com.andreas.showsdb.service.MainCastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/actors")
public class ActorsController {

    @Autowired
    private ActorsService actorsService;

    @Autowired
    MainCastService mainCastService;

    @GetMapping("")
    public List<ActorDtoId> getAll() {
        return actorsService.findAll();
    }

    @GetMapping("/{actorId}")
    public ResponseEntity<?> get(@PathVariable("actorId") long id) {
        try {
            return ResponseEntity.ok(actorsService.findById(id));
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody ActorDto actor) {
//        if (actor.getId() != null && actorsService.findById(actor.getId()).isPresent()) {
//            Map<String, Object> response = new HashMap<>();
//            response.put("message", "Actor already exists with that id");
//            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
//        }

        ActorDtoId saved = actorsService.save(actor);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("")
    public ResponseEntity<?> modify(@RequestBody ActorDtoId actor) {
//        if (actor.getId() == null || actorsService.findById(actor.getId()).isEmpty()) {
//            Map<String, Object> response = new HashMap<>();
//            response.put("message", "Actor does not exist");
//            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
//        }

        try {
            return ResponseEntity.ok(actorsService.modify(actor));
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @DeleteMapping("/{actorId}")
    public ResponseEntity<?> delete(@PathVariable("actorId") long id) {
        try {
            actorsService.findById(id);
        } catch (NotFoundException e) {
            return e.getResponse();
        }

//        if (actorsService.findById(id).isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
        actorsService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{actorId}/shows")
    public ResponseEntity<?> getShows(@PathVariable("actorId") long id) {
//        Actor actor;
//        try {
//            actor = actorsService.findById(id);
//        } catch (NotFoundException e) {
//            return e.getResponse();
//        }
//
//        List<MainCast> showsAsMainCast = mainCastService.findShowsAsMainCast(actor);
//        return ResponseEntity.ok(showsAsMainCast);
        return ResponseEntity.ok().build();
    }
}
