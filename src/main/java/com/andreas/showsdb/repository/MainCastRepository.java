package com.andreas.showsdb.repository;

import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.model.Show;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface MainCastRepository extends JpaRepository<MainCast, MainCast.MainCastKey> {
    List<MainCast> findByShow(Show show);

    List<MainCast> findByActor(Actor actor);

    Optional<MainCast> findDistinctByActorAndShow(Actor actor, Show show);

    @Modifying
    @Transactional
    void deleteDistinctByActorAndShow(Actor actor, Show show);
}
