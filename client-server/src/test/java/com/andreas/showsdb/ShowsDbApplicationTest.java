package com.andreas.showsdb;

import com.andreas.showsdb.controller.ShowsController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.WHEN_AVAILABLE)
class ShowsDbApplicationTest {

    @Autowired
    private ShowsController showsController;

    @Test
    void contextLoads() {
        assertNotNull(showsController);
    }
}