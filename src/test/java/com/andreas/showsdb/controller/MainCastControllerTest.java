package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.model.dto.MainCastDto;
import com.andreas.showsdb.util.Utils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
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
        Actor actor = Actor.builder()
                .name("Kayvan Novak")
                .country("United Kingdom")
                .birthDate(Utils.parseDate("23/11/1978"))
                .build();

        ResponseEntity<Actor> actorResponse = client.postForEntity(createUri("/api/actors"), actor, Actor.class);
        actor = actorResponse.getBody();

        assertNotNull(actor);

        MainCastDto mainCastDto = MainCastDto.builder()
                .actorId(actor.getId())
                .showId(show.getId())
                .character("Nandor the Relentless")
                .build();

        ResponseEntity<MainCast> response =
                client.postForEntity(createUri("/api/main-cast"), mainCastDto, MainCast.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        MainCast mainCast = response.getBody();
        assertNotNull(mainCast);
        assertEquals(actor.getName(), mainCast.getActor().getName());
        assertEquals(show.getName(), mainCast.getShow().getName());
    }

    @Test
    @Order(2)
    void testGetActorShowsAsMainCast() throws URISyntaxException {
        ResponseEntity<MainCast[]> response = client.getForEntity(createUri("/api/actors/1/shows"), MainCast[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        MainCast[] mainCasts = response.getBody();
        assertNotNull(mainCasts);
        assertEquals(1, mainCasts.length);
        assertEquals("What We Do in the Shadows", mainCasts[0].getShow().getName());
        assertEquals("Nandor the Relentless", mainCasts[0].getCharacter());
    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }
}
