package com.cts.mrfp.carrygo.controller;

import com.cts.mrfp.carrygo.dto.FareEstimateRequest;
import com.cts.mrfp.carrygo.dto.FareEstimateResponse;
import com.cts.mrfp.carrygo.service.FareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Endpoint that calculates how much a delivery will cost before the user books it.
@RestController
@RequestMapping("/api/fare")
@CrossOrigin(origins = "*")
public class FareController {

    @Autowired private FareService fareService;

    // POST /api/fare/estimate — given pickup/drop and package details,
    // returns the estimated fare breakdown (base price, distance cost, service fee, total).
    @PostMapping("/estimate")
    public ResponseEntity<FareEstimateResponse> estimate(@RequestBody FareEstimateRequest req) {
        return ResponseEntity.ok(fareService.estimate(req));
    }
}
