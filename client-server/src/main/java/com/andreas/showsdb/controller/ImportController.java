package com.andreas.showsdb.controller;

import com.andreas.showsdb.exception.ShowsDatabaseException;
import com.andreas.showsdb.messaging.Messenger;
import com.andreas.showsdb.util.Utils;
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

    @PostMapping("/imports/shows")
    public void importShows(@RequestPart("file") MultipartFile file) throws ShowsDatabaseException {
        String savedFile = Utils.saveFile(file, filePath);
        messenger.sendBatchOrder("shows", savedFile);
    }

    @PostMapping("/imports/episodes")
    public void importEpisodes(@RequestPart("file") MultipartFile file) throws ShowsDatabaseException {
        String savedFile = Utils.saveFile(file, filePath);
        messenger.sendBatchOrder("episodes", savedFile);
    }
}
