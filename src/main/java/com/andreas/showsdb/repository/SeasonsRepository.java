package com.andreas.showsdb.repository;

import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SeasonsRepository extends JpaRepository<Season, Long> {
    List<Season> findByShow(Show show);

    Optional<Season> findByShowAndSeasonNumber(Show show, int seasonNumber);

    @Modifying
    @Transactional
    @Query("delete from Season s where s.show = ?1")
    void deleteAllByShow(Show show);
}
