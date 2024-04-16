package com.andreas.showsdb.service.jpa;

import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.repository.MainCastRepository;
import com.andreas.showsdb.service.MainCastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MainCastServiceJpa implements MainCastService {
    @Autowired
    MainCastRepository mainCastRepository;

    @Override
    public List<MainCast> findAll() {
        return mainCastRepository.findAll();
    }

    @Override
    public MainCast saveMainCast(MainCast mainCast) {
        return mainCastRepository.save(mainCast);
    }
}
