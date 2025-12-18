package com.example.demo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionController {

    private final DatabaseVersionService databaseVersionService;

    public VersionController(DatabaseVersionService databaseVersionService) {
        this.databaseVersionService = databaseVersionService;
    }

    @GetMapping(value = "/version", produces = MediaType.TEXT_PLAIN_VALUE)
    public String version() {
        return databaseVersionService.getPostgresVersion();
    }
}
