package com.andreas.showsdb.service;

import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.model.Show;

import java.util.List;
import java.util.Optional;

public interface MainCastService {
    List<MainCast> findAll();
    Optional<MainCast> findByActorAndShow(Actor actor, Show show);
    MainCast saveMainCast(MainCast mainCast);

}
