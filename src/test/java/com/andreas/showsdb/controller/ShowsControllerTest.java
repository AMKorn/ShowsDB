package com.andreas.showsdb.controller;

import com.andreas.showsdb.model.Show;
import com.andreas.showsdb.model.dto.ShowInputDto;
import com.andreas.showsdb.model.dto.ShowOutputDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
        ResponseEntity<ShowOutputDto[]> response =
                client.getForEntity(createUri("/api/shows"), ShowOutputDto[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        List<ShowOutputDto> shows = Arrays.asList(Objects.requireNonNull(response.getBody()));

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
        ResponseEntity<ShowOutputDto> response = client.getForEntity(createUri("/api/shows/1"), ShowOutputDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ShowOutputDto show = response.getBody();
        assertNotNull(show);
        assertEquals(1L, show.getId());
        assertEquals("What We Do in the Shadows", show.getName());
        assertEquals("United States", show.getCountry());
    }

    @Test
    @Order(3)
    void testGetShowDoesNotExist() throws URISyntaxException, JsonProcessingException {
        ResponseEntity<String> response = client.getForEntity(createUri("/api/shows/10"), String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());
        assertNull(response.getBody());
    }

    @Test
    @Order(4)
    void testAddShow() throws URISyntaxException {
        ShowInputDto showInputDto = ShowInputDto.builder()
                .name("Bojack Horseman")
                .country("United States")
                .build();

        ResponseEntity<ShowOutputDto> response = client.postForEntity(createUri("/api/shows"), showInputDto, ShowOutputDto.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        ShowOutputDto showOutputDto = response.getBody();
        assertNotNull(showOutputDto);
        assertEquals(3, showOutputDto.getId());
        assertEquals("Bojack Horseman", showOutputDto.getName());
        assertEquals("United States", showOutputDto.getCountry());
    }

    @Test
    @Order(5)
    void testModifyShow() throws URISyntaxException {
        ResponseEntity<ShowOutputDto> response = client.getForEntity(createUri("/api/shows/2"), ShowOutputDto.class);
        ShowOutputDto showOutputDto = response.getBody();

        assertNotNull(showOutputDto);

        ShowOutputDto showOutputDto1 = ShowOutputDto.builder()
                .id(showOutputDto.getId())
                .name("The Good Place")
                .country("Canada")
                .build();


        client.put(createUri("/api/shows"), showOutputDto1);

        response = client.getForEntity(createUri("/api/shows/2"), ShowOutputDto.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());

        showOutputDto = response.getBody();
        assertNotNull(showOutputDto);
        assertEquals("The Good Place", showOutputDto.getName());
        assertEquals("Canada", showOutputDto.getCountry());
    }

    @Test
    @Order(6)
    void testModifyShowDoesNotExist() throws URISyntaxException, JsonProcessingException {
        Show show = Show.builder()
                .name("Nonexistent Show")
                .country("Nonexistent Country")
                .build();
        show.setId(99L);
        RequestEntity<Show> request = new RequestEntity<>(show, HttpMethod.PUT, createUri("/api/shows"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());
        assertNull(response.getBody());
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
    void testDeleteNonexistentShow() throws URISyntaxException, JsonProcessingException {
        RequestEntity<Void> request = new RequestEntity<>(HttpMethod.DELETE, createUri("/api/shows/99"));
        ResponseEntity<String> response = client.exchange(request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getHeaders().getContentType());
        assertNull(response.getBody());
    }

    private URI createUri(String uri) throws URISyntaxException {
        return new URI("http://localhost:" + port + uri);
    }

}
