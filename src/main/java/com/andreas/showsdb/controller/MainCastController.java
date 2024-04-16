package com.andreas.showsdb.controller;


import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.service.ActorsService;
import com.andreas.showsdb.service.MainCastService;
import com.andreas.showsdb.service.ShowsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/main-cast")
public class MainCastController {

    @Autowired
    private ShowsService showsService;
    @Autowired
    private ActorsService actorsService;
    @Autowired
    private MainCastService mainCastService;

    @GetMapping("")
    public List<MainCast> getMainCasts(){
        return mainCastService.findAll();
    }

    @PostMapping("")
    public ResponseEntity<?> addMainCast(@RequestBody MainCast mainCast) {
        Show show = mainCast.getShow();
        Map<String, Object> response = new HashMap<>();
        if (show == null || show.getId() == null) {
            response.put("message", "Show id must not be empty");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        Actor actor = mainCast.getActor();
        if (actor == null || actor.getId() == null) {
            response.put("message", "Actor id must not be empty");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            MainCast savedMainCast = mainCastService.saveMainCast(mainCast);
            return new ResponseEntity<>(savedMainCast, HttpStatus.CREATED);
        } catch (DataIntegrityViolationException e) {
            response.put("message", "That actor is already in that show. Use PUT to modify.");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }
}
