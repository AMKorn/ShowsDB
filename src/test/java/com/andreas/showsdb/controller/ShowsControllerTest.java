package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Season;
import com.andreas.showsdb.model.Show;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShowsControllerTest {
    @Autowired
    private TestRestTemplate client;
    @LocalServerPort
    private int port;

    @Test
    @Order(1)
    void testSearchAll() throws URISyntaxException {
        ResponseEntity<Show[]> response =
                client.getForEntity(createUri("/api/shows"), Show[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        List<Show> shows = Arrays.asList(Objects.requireNonNull(response.getBody()));

        assertEquals(2, shows.size());
        assertEquals(1L, shows.get(0).getId());
        assertEquals(2L, shows.get(1).getId());
        assertEquals("What We Do in the Shadows", shows.get(0).getName());
        assertEquals("The Good Place", shows.get(1).getName());
        assertEquals("United States", shows.get(0).getCountry());
        assertEquals("United States", shows.get(1).getCountry());

    }

    @Test
    @Order(2)
    void testGetShowExists() throws URISyntaxException {
        ResponseEntity<Show> response = client.getForEntity(createUri("/api/shows/1"), Show.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Show show = response.getBody();
        assertNotNull(show);
        assertEquals(1L, show.getId());
        assertEquals("What We Do in the Shadows", show.getName());
        assertEquals("United States", show.getCountry());
    }

    @Test
    @Order(3)
    void testGetShowDoesNotExist() throws URISyntaxException {
        ResponseEntity<Show> response = client.getForEntity(createUri("/api/shows/10"), Show.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());

        assertNull(response.getBody());
    }

    @Test
    @Order(4)
    void testAddShow() throws URISyntaxException {
        Show show = new Show("Bojack Horseman", "United States");

        ResponseEntity<Show> response = client.postForEntity(createUri("/api/shows"), show, Show.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Show show1 = response.getBody();
        assertNotNull(show1);
        assertEquals(3, show1.getId());
        assertEquals("Bojack Horseman", show1.getName());
        assertEquals("United States", show1.getCountry());
    }

    @Test
    @Order(5)
    void testModifyShow() throws URISyntaxException {
        ResponseEntity<Show> response = client.getForEntity(createUri("/api/shows/2"), Show.class);
        Show show = response.getBody();

        assertNotNull(show);
        show.setCountry("Canada");

        client.put(createUri("/api/shows"), show);

        response = client.getForEntity(createUri("/api/shows/2"), Show.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        show = response.getBody();
        assertNotNull(show);
        assertEquals("Canada", show.getCountry());
    }

    @Test
    @Order(6)
    void testModifyShowDoesNotExist() throws URISyntaxException, JsonProcessingException {
        Show show = new Show("Nonexistent Show", "Nonexistent Country");
        show.setId(99L);
        RequestEntity<Show> request = new RequestEntity<>(show, HttpMethod.PUT, createUri("/api/shows"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Show does not exist",
                json.path("message").asText());
    }

    @Test
    @Order(7)
    void testDeleteShow() throws URISyntaxException {
        ResponseEntity<Show[]> response = client.getForEntity(createUri("/api/shows"), Show[].class);
        List<Show> shows = Arrays.asList(Objects.requireNonNull(response.getBody()));

        assertEquals(3, shows.size());

        client.delete(createUri("/api/shows/3"));

        response = client.getForEntity(createUri("/api/shows"), Show[].class);
        shows = Arrays.asList(Objects.requireNonNull(response.getBody()));

        assertEquals(2, shows.size());
        Optional<Show> show = shows.stream().filter(s -> s.getId() == 3).findFirst();
        assertTrue(show.isEmpty());
    }

    @Test
    @Order(8)
    void testDeleteNonexistentShow() throws URISyntaxException {
        RequestEntity<Void> request = new RequestEntity<>(HttpMethod.DELETE, createUri("/api/shows/99"));
        ResponseEntity<Void> response = client.exchange(request, Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());

        assertNull(response.getBody());
    }

    @Test
    @Order(9)
    void testAddSeasonToShow() throws URISyntaxException {
        Season season = new Season();
        season.setSeasonNumber(1);
        ResponseEntity<Season> response = client.postForEntity(createUri("/api/shows/1/seasons"), season, Season.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Season seasonResponse = response.getBody();
        assertNotNull(seasonResponse);
        assertNotNull(seasonResponse.getShow());
        assertEquals(1, seasonResponse.getShow().getId());
        assertEquals(1, seasonResponse.getSeasonNumber());
    }

    @Test
    @Order(10)
    void testAddSecondSeasonToShow() throws URISyntaxException {
        Season season = new Season();
        season.setSeasonNumber(2);
        ResponseEntity<Season> response = client.postForEntity(createUri("/api/shows/1/seasons"), season, Season.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Season seasonResponse = response.getBody();
        assertNotNull(seasonResponse);
        assertNotNull(seasonResponse.getShow());
        assertEquals(1, seasonResponse.getShow().getId());
        assertEquals(2, seasonResponse.getSeasonNumber());
    }

    @Test
    @Order(11)
    void testAddUnnumberedSeasonToShow() throws URISyntaxException {
        ResponseEntity<Season> response = client.postForEntity(createUri("/api/shows/1/seasons"), new Season(), Season.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Season seasonResponse = response.getBody();
        assertNotNull(seasonResponse);
        assertNotNull(seasonResponse.getShow());
        assertEquals(1, seasonResponse.getShow().getId());
        assertEquals(3, seasonResponse.getSeasonNumber());

    }

    @Test
    @Order(12)
    void testAddSeasonToShowAlreadyExists() throws JsonProcessingException, URISyntaxException {
        Season season = new Season();
        season.setSeasonNumber(1);
        ResponseEntity<String> response = client.postForEntity(createUri("/api/shows/1/seasons"), season, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Show 'What We Do in the Shadows' already has a Season 1",
                json.path("message").asText());

    }

    @Test
    @Order(13)
    void testAddSeasonToShowThatDoesNotExist() throws JsonProcessingException, URISyntaxException {
        Season season = new Season();
        ResponseEntity<String> response = client.postForEntity(createUri("/api/shows/99/seasons"), season, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        assertEquals("Show does not exist",
                json.path("message").asText());
    }


    @Test
    @Order(14)
    void testGetShowSeasons() throws URISyntaxException {
        ResponseEntity<Season[]> response = client.getForEntity(createUri("/api/shows/1/seasons"), Season[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        List<Season> seasons = Arrays.asList(Objects.requireNonNull(response.getBody()));
        assertEquals(3, seasons.size());
    }

    @Test
    @Order(15)
    void testGetShowSeasonByNumber() throws URISyntaxException {
        ResponseEntity<Season> response = client.getForEntity(createUri("/api/shows/1/seasons/1"), Season.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        Season season = response.getBody();
        assertNotNull(season);
        assertEquals(1, season.getShow().getId());
        assertEquals(1, season.getSeasonNumber());
    }

    @Test
    @Order(16)
    void testGetNonexistentSeason() throws URISyntaxException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/1/seasons/15"), String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());

        assertNull(response.getBody());
    }

//    @Test
//    @Order(17)
//    void testDeleteShowWithSeasons() throws URISyntaxException {
////        ResponseEntity<Show> showResponse = client.getForEntity(createUri("/api/shows/1"), Show.class);
//        ResponseEntity<Season[]> seasonsResponse = client.getForEntity(createUri("/api/shows/1/seasons"),
//                Season[].class);
//
////        Show show = showResponse.getBody();
////        assertNotNull(show);
//        List<Season> seasons = Arrays.asList(Objects.requireNonNull(seasonsResponse.getBody()));
//        assertEquals(3, seasons.size());
//
////        client.delete(createUri("/api/shows/1"));
//
//    }
//
//    @Test
//    void testDeleteSeason() {
//        assertEquals(1, 2);
//    }
//
//    @Test
//    void testDeleteNonexistentSeason() {
//        assertEquals(1, 2);
//    }
//
//    @Test
//    void testDeleteAllSeasons() {
//        assertEquals(1, 2);
//    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }

}
