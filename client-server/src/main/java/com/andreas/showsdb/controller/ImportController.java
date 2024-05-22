package com.andreas.showsdb.controller;

import com.andreas.showsdb.batch.BatchOrderListener;
import com.andreas.showsdb.exception.ShowsDatabaseException;
import com.andreas.showsdb.messaging.Messenger;
import com.andreas.showsdb.util.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImportController {
    private final Messenger messenger;

    @Value("${showsdb.files}")
    private String filePath;

    @Operation(summary = "Upload a file of shows to be exported in batch",
            description = """
                    Upload a csv file, with headers Name, Country and Seasons. The file will be uploaded, used by the
                    batch import system and later deleted. Shows with names already in the database will be ignored, and
                    any seasons up to the number of seasons will be created.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "File uploaded"
            )
    })
    @PostMapping("/imports/shows")
    public void importShows(@RequestPart("file") MultipartFile file) throws ShowsDatabaseException {
        String savedFile = Utils.saveFile(file, filePath);
        messenger.sendBatchOrder(BatchOrderListener.SHOWS, savedFile);
    }

    @Operation(summary = "Upload a file of episodes to be exported in batch",
            description = """
                    Upload a csv file, with headers Show, Season, Episode and Name. The file will be uploaded, used by
                    the batch import system and then deleted. A batch episode operation CAN NOT create new shows nor
                    seasons, so any episodes of a show whose name was not found or of a season stated does not exist
                    will be ignored.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "File uploaded"
            )
    })
    @PostMapping("/imports/episodes")
    public void importEpisodes(@RequestPart("file") MultipartFile file) throws ShowsDatabaseException {
        String savedFile = Utils.saveFile(file, filePath);
        messenger.sendBatchOrder(BatchOrderListener.EPISODES, savedFile);
    }
}
