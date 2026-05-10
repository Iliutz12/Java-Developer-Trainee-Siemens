package com.example.trainapplication.controller;

import com.example.trainapplication.contracts.RouteSearchRequest;
import com.example.trainapplication.dtos.RouteDtos;
import com.example.trainapplication.services.RouteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchRoute(@RequestBody RouteSearchRequest request) {
        if (request.fromStation() == null || request.fromStation().isBlank() ||
                request.toStation()   == null || request.toStation().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Both fromStation and toStation are required."));
        }
        try {
            RouteDtos.RouteSearchResponse response =
                    routeService.findRoute(request.fromStation(), request.toStation());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
