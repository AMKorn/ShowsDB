package com.andreas.showsdb.controller;

import com.andreas.showsdb.service.ShowsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(ShowsController.class)
class ShowsControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ShowsService showsService;


}