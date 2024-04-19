package com.andreas.showsdb.controller;


import com.andreas.showsdb.exception.NotFoundException;
import com.andreas.showsdb.exception.ShowsDatabaseException;
import com.andreas.showsdb.model.dto.ActorInfo;
import com.andreas.showsdb.model.dto.MainCastInfo;
import com.andreas.showsdb.service.MainCastService;
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
@RequestMapping("/api/main-cast")
public class MainCastController {
    @Autowired
    private MainCastService mainCastService;

    @Operation(summary = "List all main casts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = MainCastInfo.class)
                            )
                    )
            )
    })
    @GetMapping("")
    public List<MainCastInfo> getAll() {
        return mainCastService.findAll();
    }

    @Operation(summary = "Create a main cast passed through body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Main cast created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActorInfo.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Actor or show not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema,
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "string"
                                    }""")
                    )
            )
    })
    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody MainCastInfo mainCastInfo) {
        try {
            MainCastInfo savedMainCast = mainCastService.save(mainCastInfo);
            return new ResponseEntity<>(savedMainCast, HttpStatus.CREATED);
        } catch (ShowsDatabaseException e) {
            return e.getResponse();
        }
    }

    @Operation(summary = "Modify a main cast passed through body")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Main cast modified",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ActorInfo.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Actor, show or main cast not found",
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
    public ResponseEntity<?> modify(@RequestBody MainCastInfo mainCastInfo) {
        try {
            MainCastInfo modifiedMainCast = mainCastService.modify(mainCastInfo);
            return ResponseEntity.ok(modifiedMainCast);
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }

    @Operation(summary = "Delete a main cast")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Main cast deleted"
            ),
            @ApiResponse(responseCode = "404",
                    description = "Actor, show or main cast not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema,
                            examples = @ExampleObject(value = """
                                    {
                                      "message": "string"
                                    }""")
                    )
            )
    })
    @DeleteMapping("")
    public ResponseEntity<?> delete(@Parameter(description = "Id of the actor")
                                    @RequestParam("actor") Long actorId,
                                    @Parameter(description = "Id of the show")
                                    @RequestParam("show") Long showId) {
        try {
            mainCastService.delete(actorId, showId);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            return e.getResponse();
        }
    }
}
