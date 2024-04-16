package com.andreas.showsdb.service;

import com.andreas.showsdb.model.MainCast;

import java.util.List;

public interface MainCastService {
    List<MainCast> findAll();
    MainCast saveMainCast(MainCast mainCast);

}
