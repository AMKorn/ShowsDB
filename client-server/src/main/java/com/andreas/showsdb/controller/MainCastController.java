package com.andreas.showsdb.controller;


import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.exception.ShowsDatabaseException;
import com.andreas.showsdb.model.dto.MainCastDto;
import com.andreas.showsdb.model.dto.hateoas.MainCastHypermedia;
import com.andreas.showsdb.service.MainCastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/main-cast")
@RequiredArgsConstructor
public class MainCastController {

    private final MainCastService mainCastService;

    @SneakyThrows // This method cannot and will not throw an exception, but the compiler doesn't know that.
    private static MainCastHypermedia addLinks(MainCastDto mainCast) {
        Long showId = mainCast.getShowId();
        Long actorId = mainCast.getActorId();

        MainCastHypermedia mch = new MainCastHypermedia(mainCast);
        mch.add(linkTo(methodOn(ShowsController.class).get(showId)).withRel("show"));
        mch.add(linkTo(methodOn(ActorsController.class).get(actorId)).withRel("actor"));

        return mch;
    }

    @Operation(summary = "List all main casts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MainCastHypermedia.class))))})
    @GetMapping
    public List<MainCastHypermedia> getAll() {
        return mainCastService.findAll().stream()
                .map(MainCastController::addLinks)
                .toList();
    }

    @Operation(summary = "Create a main cast passed through body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Main cast created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MainCastHypermedia.class))),
            @ApiResponse(responseCode = "404", description = "Actor or show not found"),
            @ApiResponse(responseCode = "409", description = "Combination already present in database")})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MainCastHypermedia create(@RequestBody MainCastDto mainCastDto) throws ShowsDatabaseException {
        MainCastDto mainCast = mainCastService.save(mainCastDto);
        return addLinks(mainCast);
    }

    @Operation(summary = "Modify a main cast passed through body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Main cast modified",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MainCastHypermedia.class))),
            @ApiResponse(responseCode = "404", description = "Actor, show or main cast not found")})
    @PutMapping
    public MainCastHypermedia modify(@RequestBody MainCastDto mainCastDto) throws NotFoundException {
        MainCastDto modifiedMC = mainCastService.modify(mainCastDto);
        return addLinks(modifiedMC);
    }

    @Operation(summary = "Delete a main cast")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Main cast deleted")})
    @DeleteMapping
    public void delete(@Parameter(description = "Id of the actor")
                       @RequestParam("actor") Long actorId,
                       @Parameter(description = "Id of the show")
                       @RequestParam("show") Long showId) {
        mainCastService.delete(actorId, showId);
    }

    @Operation(summary = "Clear all the cache for main casts", description = """
            Should not be necessary, as any modifications to the relevant tables in the database will also clear cache, 
            but it's better to have it than not.""")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Cache cleared"))
    @DeleteMapping("/cache")
    public void clearCache() {
        mainCastService.clearCache();
    }
}
