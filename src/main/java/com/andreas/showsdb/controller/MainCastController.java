package com.andreas.showsdb.controller;


import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.exception.ShowsDatabaseException;
import com.andreas.showsdb.model.dto.MainCastInfo;
import com.andreas.showsdb.service.ActorsService;
import com.andreas.showsdb.service.MainCastService;
import com.andreas.showsdb.service.ShowsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<MainCastInfo> getAll() {
        return mainCastService.findAll();
    }

    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody MainCastInfo mainCastInfo) {
        try {
            MainCastInfo savedMainCast = mainCastService.save(mainCastInfo);
            return new ResponseEntity<>(savedMainCast, HttpStatus.CREATED);
        } catch (ShowsDatabaseException e) {
            return e.getResponse();
        }
    }

    @PutMapping("")
    public ResponseEntity<?> modify(@RequestBody MainCastInfo mainCastInfo) {
        try {
            MainCastInfo modifiedMainCast = mainCastService.modify(mainCastInfo);
            return ResponseEntity.ok(modifiedMainCast);
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @DeleteMapping("")
    public ResponseEntity<?> delete(@RequestParam("actor") Long actorId,
                                    @RequestParam("show") Long showId) {
        try {
            mainCastService.delete(actorId, showId);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }
}
