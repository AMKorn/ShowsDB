package com.andreas.showsdb.service;

import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.repository.MainCastRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MainCastService {
    @Autowired
    MainCastRepository mainCastRepository;

    public List<MainCast> findAll() {
        return mainCastRepository.findAll();
    }

    public Optional<MainCast> findByActorAndShow(Actor actor, Show show) {
        return mainCastRepository.findDistinctByActorAndShow(actor, show);
    }

    public MainCast saveMainCast(MainCast mainCast) {
        return mainCastRepository.save(mainCast);
    }

    public List<MainCast> findShowsAsMainCast(Actor actor) {
        return mainCastRepository.findByActor(actor);
    }

    public List<MainCast> findMainCastByShow(Show show) {
        return mainCastRepository.findByShow(show);
    }
}
