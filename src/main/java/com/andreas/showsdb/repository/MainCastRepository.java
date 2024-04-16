package com.andreas.showsdb.repository;

import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MainCastRepository extends JpaRepository<MainCast, MainCast.MainCastKey> {
    List<Actor> findByShow(Show show);
    List<Show> findByActor(Actor actor);
    Optional<MainCast> findDistinctByActorAndShow(Actor actor, Show show);
}
