package com.garygz.dspdemoproject.controller;

import com.garygz.dspdemoproject.entity.Advertiser;
import com.garygz.dspdemoproject.service.DashBoardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/advertisers")
public class AdvertiserController {

    private static final Logger logger = LoggerFactory.getLogger(AdvertiserController.class);

    private final DashBoardService dashBoardService;

    public AdvertiserController(DashBoardService dashBoardService) {
        this.dashBoardService = dashBoardService;
    }

    @GetMapping
    public ResponseEntity<List<Advertiser>> listAll() {
        return ResponseEntity.ok(dashBoardService.getAllAdvertisers());
    }

    @PostMapping
    public ResponseEntity<Void> add(@RequestBody Advertiser newAdv) {
        logger.debug("Creating advertiser {}", newAdv.getName());
        dashBoardService.addAdvertiser(newAdv);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
