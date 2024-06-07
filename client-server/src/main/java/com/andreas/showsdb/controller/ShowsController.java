package com.andreas.showsdb.controller;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.exception.ShowsDatabaseException;
import com.andreas.showsdb.messaging.Messenger;
import com.andreas.showsdb.model.dto.ShowInputDto;
import com.andreas.showsdb.model.dto.ShowOutputDto;
import com.andreas.showsdb.model.dto.hateoas.MainCastHypermedia;
import com.andreas.showsdb.model.dto.hateoas.ShowHypermedia;
import com.andreas.showsdb.service.MainCastService;
import com.andreas.showsdb.service.ShowsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowsController {

    private final ShowsService showsService;
    private final MainCastService mainCastService;
    private final Messenger messenger;

    @SneakyThrows // This method cannot and will not throw an exception, but the compiler doesn't know that.
    private static ShowHypermedia addLinks(ShowOutputDto show) {
        ShowHypermedia sh = new ShowHypermedia(show);
        Long id = show.getId();
        sh.add(linkTo(methodOn(ShowsController.class).get(id)).withSelfRel());
        sh.add(linkTo(methodOn(ShowsController.class).updateState(id)).withRel("update state"));
        sh.add(linkTo(methodOn(SeasonsController.class).getAllByShow(id)).withRel("seasons"));
        return sh;
    }

    @Operation(summary = "List all shows")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ShowHypermedia.class))))})
    @GetMapping
    public List<ShowHypermedia> searchAll() {
        return showsService.findAll().stream()
                .map(ShowsController::addLinks)
                .toList();
    }

    @Operation(summary = "Find a show given the specified id by parameter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Show found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShowHypermedia.class))),
            @ApiResponse(responseCode = "404", description = "Show not found")})
    @GetMapping("/{id}")
    public ShowHypermedia get(@Parameter(description = "Id of the show")
                              @PathVariable("id") long id) throws NotFoundException {
        ShowOutputDto show = showsService.findById(id);
        return addLinks(show);
    }

    @Operation(summary = "Create a show passed through body. Does not check for duplicates")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Show created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShowHypermedia.class)))})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShowHypermedia create(@RequestBody @Valid ShowInputDto show) {
        ShowOutputDto savedShow = showsService.save(show);
        messenger.newShow(savedShow);
        return addLinks(savedShow);
    }

    @Operation(summary = "Modify a show passed through body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Show found and modified",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShowHypermedia.class))),
            @ApiResponse(responseCode = "404", description = "Show not found")})
    @PutMapping
    public ShowHypermedia modify(@RequestBody ShowOutputDto show) throws NotFoundException {
        ShowOutputDto modifiedShow = showsService.modify(show);
        return addLinks(modifiedShow);
    }

    @Operation(summary = "Delete a show given the specified id by parameter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Show deleted")})
    @DeleteMapping("/{id}")
    public void delete(@Parameter(description = "Id of the show")
                       @PathVariable("id") long id) {
        showsService.deleteById(id);
    }

    @Operation(summary = "Find all the main cast of a show and the characters they play")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shows and characters found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MainCastHypermedia.class))))})
    @GetMapping("/{id}/main-cast")
    public List<MainCastHypermedia> getMainCast(@Parameter(description = "Id of the show")
                                                @PathVariable("id") long id) {
        return mainCastService.findByShow(id).stream()
                .map(MainCastHypermedia::new)
                .peek(mainCast -> {
                    try {
                        mainCast.add(linkTo(methodOn(ShowsController.class).get(mainCast.getContent().getShowId()))
                                .withRel("show"));
                        mainCast.add(linkTo(methodOn(ActorsController.class).get(mainCast.getContent().getActorId()))
                                .withRel("actor"));
                    } catch (NotFoundException e) {
                        // This line cannot and will not throw an exception, but the compiler doesn't know that.
                    }
                })
                .toList();
    }

    @Operation(summary = "Update the state of a show with internal logic")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shows state updated",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ShowHypermedia.class)))),
            @ApiResponse(responseCode = "404", description = "Show not found")})
    @PatchMapping("/{id}")
    public ShowHypermedia updateState(@Parameter(description = "Id of the show")
                                      @PathVariable Long id) throws ShowsDatabaseException {
        return addLinks(showsService.updateState(id));
    }

    @Operation(summary = "Clear all the cache for shows", description = """
            Should not be necessary, as any modifications to the relevant tables in the database will also clear cache,
            but it's better to have it than not.""")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Cache cleared"))
    @DeleteMapping("/cache")
    public void clearCache() {
        showsService.clearCache();
    }
}
