package com.andreas.showsdb.service;

import com.andreas.showsdb.model.Show;

import java.util.List;
import java.util.Optional;

public interface ShowsService {
    List<Show> findAll();

    Optional<Show> findById(long id);

    Show save(Show show);

    void deleteById(long id);
}
