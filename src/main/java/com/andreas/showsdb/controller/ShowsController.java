package com.andreas.showsdb.controller;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.model.dto.ShowInfo;
import com.andreas.showsdb.model.dto.ShowInput;
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
    public List<ShowInfo> searchAll() {
        return showsService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") long id) {
        try {
            return ResponseEntity.ok(showsService.findById(id));
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody ShowInput show) {
        ShowInfo saved = showsService.save(show);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("")
    public ResponseEntity<?> modify(@RequestBody ShowInfo show) {
        try {
            return ResponseEntity.ok(showsService.modify(show));
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") long id) {
        try {
            showsService.findById(id);
        } catch (NotFoundException e) {
            return e.getResponse();
        }

        showsService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/main-cast")
    public ResponseEntity<?> getShowMainCast(@PathVariable("id") long id) {
//        Show show;
//        try {
//            show = showsService.findById(id).orElseThrow(() -> new NotFoundException("Show not found"));
//        } catch (NotFoundException e) {
//            return e.getResponse();
//        }
//
//        List<MainCast> mainCasts = mainCastService.findMainCastByShow(show);
//        return ResponseEntity.ok(mainCasts);
        return ResponseEntity.ok().build();
    }
}
