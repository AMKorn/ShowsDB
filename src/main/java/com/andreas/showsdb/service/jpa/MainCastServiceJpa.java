package com.andreas.showsdb.service.jpa;

import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.repository.MainCastRepository;
import com.andreas.showsdb.service.MainCastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MainCastServiceJpa implements MainCastService {
    @Autowired
    MainCastRepository mainCastRepository;

    @Override
    public List<MainCast> findAll() {
        return mainCastRepository.findAll();
    }

    @Override
    public Optional<MainCast> findByActorAndShow(Actor actor, Show show) {
        return mainCastRepository.findDistinctByActorAndShow(actor, show);
    }

    @Override
    public MainCast saveMainCast(MainCast mainCast) {
        return mainCastRepository.save(mainCast);
    }

    @Override
    public List<MainCast> findShowsAsMainCast(Actor actor) {
        return mainCastRepository.findByActor(actor);
    }

    @Override
    public List<MainCast> findMainCastByShow(Show show) {
        return mainCastRepository.findByShow(show);
    }
}
