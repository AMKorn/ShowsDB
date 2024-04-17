package com.andreas.showsdb.controller;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.dto.ActorInput;
import com.andreas.showsdb.model.dto.ActorInfo;
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
    public List<ActorInfo> getAll() {
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
    public ResponseEntity<?> create(@RequestBody ActorInput actor) {
        ActorInfo saved = actorsService.save(actor);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("")
    public ResponseEntity<?> modify(@RequestBody ActorInfo actor) {
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
