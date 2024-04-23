package com.andreas.showsdb.repository;

import com.andreas.showsdb.model.MainCast;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface MainCastRepository extends JpaRepository<MainCast, MainCast.MainCastKey> {

    List<MainCast> findByShowId(long showId);


    List<MainCast> findByActorId(long actorId);


    Optional<MainCast> findDistinctByActorIdAndShowId(long actorId, long showId);

    @Modifying
    @Transactional
    void deleteDistinctByActorIdAndShowId(long actorId, long showId);
}
