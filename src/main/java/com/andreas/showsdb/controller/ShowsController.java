package com.andreas.showsdb.controller;

import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.model.dto.MainCastInfo;
import com.andreas.showsdb.model.dto.ShowInfo;
import com.andreas.showsdb.model.dto.ShowInput;
import com.andreas.showsdb.service.MainCastService;
import com.andreas.showsdb.service.ShowsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shows")
public class ShowsController {

    @Autowired
    private ShowsService showsService;

    @Autowired
    private MainCastService mainCastService;

    @Operation(summary = "List all shows")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ShowInfo.class)
                            )
                    )
            )
    })
    @GetMapping("")
    public List<ShowInfo> searchAll() {
        return showsService.findAll();
    }

    @Operation(summary = "Find a show given the specified id by parameter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Show found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShowInfo.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Show not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema,
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "string"
                                    }""")
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@Parameter(description = "Id of the show")
                                 @PathVariable("id") long id) {
        try {
            return ResponseEntity.ok(showsService.findById(id));
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @Operation(summary = "Create a show passed through body. Does not check for duplicates")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Show created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShowInfo.class)
                    )
            )
    })
    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody ShowInput show) {
        ShowInfo saved = showsService.save(show);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @Operation(summary = "Modify a show passed through body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Show found and modified",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShowInfo.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Show not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema,
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "string"
                                    }""")
                    )
            )
    })
    @PutMapping("")
    public ResponseEntity<?> modify(@RequestBody ShowInfo show) {
        try {
            return ResponseEntity.ok(showsService.modify(show));
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @Operation(summary = "Delete a show given the specified id by parameter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Show deleted"
            ),
            @ApiResponse(responseCode = "404",
                    description = "Show not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema,
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "string"
                                    }""")
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@Parameter(description = "Id of the show")
                                    @PathVariable("id") long id) {
        try {
            showsService.findById(id);
        } catch (NotFoundException e) {
            return e.getResponse();
        }

        showsService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Find all the main cast of a show and the characters they play")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Shows and characters found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = MainCastInfo.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Show not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema,
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "string"
                                    }""")
                    )
            )
    })
    @GetMapping("/{id}/main-cast")
    public ResponseEntity<?> getMainCast(@Parameter(description = "Id of the show")
                                         @PathVariable("id") long id) {
        try {
            List<MainCastInfo> mainCasts = mainCastService.findByShow(id);
            return ResponseEntity.ok(mainCasts);
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }
}
