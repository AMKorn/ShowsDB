package com.andreas.showsdb.controller;


import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.exception.ShowsDatabaseException;
import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.model.dto.MainCastInfo;
import com.andreas.showsdb.service.ActorsService;
import com.andreas.showsdb.service.MainCastService;
import com.andreas.showsdb.service.ShowsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    public List<MainCastInfo> getMainCasts(){
        return mainCastService.findAll();
    }

    @PostMapping("")
    public ResponseEntity<?> addMainCast(@RequestBody MainCastInfo mainCastInfo) {
        try {
            MainCastInfo savedMainCast = mainCastService.save(mainCastInfo);
            return new ResponseEntity<>(savedMainCast, HttpStatus.CREATED);
        } catch (ShowsDatabaseException e) {
            return e.getResponse();
        }
    }

    @PutMapping("")
    public ResponseEntity<?> modifyMainCast(@RequestBody MainCastInfo mainCastInfo) {
        try {
            MainCastInfo modifiedMainCast = mainCastService.modify(mainCastInfo);
            return ResponseEntity.ok(modifiedMainCast);
        } catch (NotFoundException e) {
            return e.getResponse();
        }
//        Optional<Actor> optionalActor = actorsService.findById(mainCastDto.getActorId());
//        Optional<Show> optionalShow = showsService.findById(mainCastDto.getShowId());
//
//        Show show;
//        Actor actor;
//        try {
//            show = optionalShow.orElseThrow(() -> new NotFoundException("Show not found"));
//            actor = optionalActor.orElseThrow(() -> new NotFoundException("Actor not found"));
//
//            Optional<MainCast> optionalMainCast = mainCastService.findByActorAndShow(actor, show);
//            if(optionalMainCast.isEmpty())
//                throw new NotFoundException("That actor is not in that show: use POST to insert");
//
//            MainCast mainCast = MainCast.builder()
//                    .actor(actor)
//                    .show(show)
//                    .character(mainCastDto.getCharacter())
//                    .id(new MainCast.MainCastKey(actor.getId(), show.getId()))
//                    .build();
//
//            MainCast savedMainCast = mainCastService.saveMainCast(mainCast);
//            return ResponseEntity.ok(savedMainCast);
//        } catch (NotFoundException e) {
//            return e.getResponse();
//        }
    }
}
