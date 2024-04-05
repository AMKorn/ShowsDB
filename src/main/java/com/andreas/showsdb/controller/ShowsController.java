package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.service.ShowsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Show getShow(@PathVariable("id") long id) {
        return showsService.findById(id).orElseThrow();
    }

    @PostMapping("/shows")
    public Show createShow(@RequestBody Show show) {
        showsService.save(show);
        return show;
    }

    @PutMapping("/shows")
    public Show modifyShow(@RequestBody Show show) {
        showsService.save(show);
        return show;
    }
}
