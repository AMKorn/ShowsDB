package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.util.Utils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MainCastControllerTest {

    @Autowired
    private TestRestTemplate client;
    @LocalServerPort
    private int port;

    @Test
    @Order(1)
    void testAddShowToActor() throws URISyntaxException {
        ResponseEntity<Show> showResponse = client.getForEntity(createUri("/api/shows/1"), Show.class);
        Show show = showResponse.getBody();
        assertNotNull(show);
        ResponseEntity<Actor> actorResponse = client.getForEntity(createUri("/api/actors/1"), Actor.class);
        Actor actor = actorResponse.getBody();
        // Depending on test order, actor may or may not exist. If not, we create it.
        if(actor == null) {
            actor = new Actor("Kayvan Novak", "United Kingdom", Utils.parseDate("23/11/1978"));
            actorResponse = client.postForEntity(createUri("/api/actors"), actor, Actor.class);
            actor = actorResponse.getBody();
        }
        assertNotNull(actor);

        MainCast newMainCast = new MainCast(actor, show, "Nandor the Relentless");

        ResponseEntity<MainCast> response =
                client.postForEntity(createUri("/api/main-cast"), newMainCast, MainCast.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        MainCast mainCast = response.getBody();
        assertNotNull(mainCast);
        assertEquals(actor.getName(), mainCast.getActor().getName());
        assertEquals(show.getName(), mainCast.getShow().getName());
    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }
}
