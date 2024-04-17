package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Actor;
import com.andreas.showsdb.model.MainCast;
import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.model.dto.MainCastDto;
import com.andreas.showsdb.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
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
    void testAddRepeatedShowToActor() throws URISyntaxException, JsonProcessingException {
        MainCastDto mainCastDto = MainCastDto.builder()
                .actorId(1L)
                .showId(1L)
                .character("Nandor the Relentless")
                .build();

        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/main-cast"), mainCastDto, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("That actor is already in that show: use PUT to modify",
                json.path("message").asText());
    }

    @Test
    @Order(3)
    void testGetMainCasts() throws URISyntaxException {
        ResponseEntity<MainCast[]> response = client.getForEntity(createUri("/api/main-cast"), MainCast[].class);
        MainCast[] mainCasts = response.getBody();
        assertNotNull(mainCasts);
        assertEquals(1, mainCasts.length);
        assertEquals("What We Do in the Shadows", mainCasts[0].getShow().getName());
        assertEquals("Nandor the Relentless", mainCasts[0].getCharacter());
    }

    @Test
    @Order(4)
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

    @Test
    @Order(5)
    void testGetShowMainCast() throws URISyntaxException {
        ResponseEntity<MainCast[]> response = client.getForEntity(createUri("/api/shows/1/main-cast"), MainCast[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        MainCast[] mainCasts = response.getBody();
        assertNotNull(mainCasts);
        assertEquals(1, mainCasts.length);
        assertEquals("What We Do in the Shadows", mainCasts[0].getShow().getName());
        assertEquals("Nandor the Relentless", mainCasts[0].getCharacter());
    }

    @Test
    @Order(6)
    void testCreateMainCastShowDoesNotExist() throws URISyntaxException, JsonProcessingException {
        MainCastDto mainCastDto = MainCastDto.builder()
                .actorId(1L)
                .showId(99L)
                .character("Nonexistent Character")
                .build();

        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/main-cast"), mainCastDto, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Show not found", json.path("message").asText());
    }

    @Test
    @Order(7)
    void testCreateMainCastActorDoesNotExist() throws URISyntaxException, JsonProcessingException {
        MainCastDto mainCastDto = MainCastDto.builder()
                .actorId(99L)
                .showId(1L)
                .character("Nonexistent Character")
                .build();

        ResponseEntity<String> response =
                client.postForEntity(createUri("/api/main-cast"), mainCastDto, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Actor not found", json.path("message").asText());
    }

    @Test
    @Order(8)
    void testModifyMainCast() throws URISyntaxException {
        MainCastDto mainCastDto = MainCastDto.builder()
                .showId(1L)
                .actorId(1L)
                .character("Nandor, the Relentless")
                .build();

        RequestEntity<MainCastDto> request =
                new RequestEntity<>(mainCastDto, HttpMethod.PUT, createUri("/api/main-cast"));
        ResponseEntity<MainCast> response = client.exchange(request, MainCast.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        MainCast mainCast = response.getBody();
        assertNotNull(mainCast);
        assertEquals("What We Do in the Shadows", mainCast.getShow().getName());
        assertEquals("Kayvan Novak", mainCast.getActor().getName());
        assertEquals("Nandor, the Relentless", mainCast.getCharacter());
    }

    @Test
    @Order(9)
    void testAddSecondMainCast() throws URISyntaxException {
        Actor actor = Actor.builder()
                .name("Kristen Bell")
                .country("United States")
                .build();
        ResponseEntity<Actor> actorResponseEntity = client.postForEntity(createUri("/api/actors"), actor, Actor.class);
        actor = actorResponseEntity.getBody();
        assertNotNull(actor);
        Show show = Show.builder()
                .name("The Good Place")
                .build();
        ResponseEntity<Show> showResponseEntity = client.postForEntity(createUri("/api/shows"), show, Show.class);
        show = showResponseEntity.getBody();
        assertNotNull(show);
        MainCastDto mainCastDto = MainCastDto.builder()
                .actorId(actor.getId())
                .showId(show.getId())
                .character("Eleanor Shellstrop")
                .build();
        client.postForEntity(createUri("/api/main-cast"), mainCastDto, Void.class);


        ResponseEntity<MainCast[]> response = client.getForEntity(createUri("/api/main-cast"), MainCast[].class);

        MainCast[] mainCasts = response.getBody();
        assertNotNull(mainCasts);
        assertEquals(2, mainCasts.length);
        assertEquals("What We Do in the Shadows", mainCasts[0].getShow().getName());
        assertEquals("Kayvan Novak", mainCasts[0].getActor().getName());
        assertEquals("Nandor, the Relentless", mainCasts[0].getCharacter());
        assertEquals("The Good Place", mainCasts[1].getShow().getName());
        assertEquals("Kristen Bell", mainCasts[1].getActor().getName());
        assertEquals("Eleanor Shellstrop", mainCasts[1].getCharacter());
    }

    @Test
    @Order(10)
    void deleteMainCast() {

    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }
}
