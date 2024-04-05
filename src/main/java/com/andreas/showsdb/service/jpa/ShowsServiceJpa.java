package com.andreas.showsdb.service.jpa;

import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.repository.ShowsRepository;
import com.andreas.showsdb.service.ShowsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShowsServiceJpa implements ShowsService {

    @Autowired
    private ShowsRepository showsRepository;

    @Override
    public List<Show> findAll() {
        return showsRepository.findAll();
    }

    @Override
    public Optional<Show> findById(long id) {
        return showsRepository.findById(id);
    }

    @Override
    public void save(Show show) {
        showsRepository.save(show);
    }

    @Override
    public void deleteById(long id) {
        showsRepository.deleteById(id);
    }
}
