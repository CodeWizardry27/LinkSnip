package com.linksnip.controller;

import com.linksnip.dto.UrlRequest;
import com.linksnip.dto.UrlResponse;
import com.linksnip.service.UrlService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<?> shortenUrl(@Valid @RequestBody UrlRequest request) {
        try {
            UrlResponse response = urlService.createShortUrl(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/urls")
    public ResponseEntity<List<UrlResponse>> getAllUrls() {
        return ResponseEntity.ok(urlService.getAllUrls());
    }

    @GetMapping("/api/urls/{shortCode}/stats")
    public ResponseEntity<?> getStats(@PathVariable String shortCode) {
        try {
            UrlResponse response = urlService.getStats(shortCode);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/urls/{shortCode}")
    public ResponseEntity<?> deleteUrl(@PathVariable String shortCode) {
        try {
            urlService.deleteUrl(shortCode);
            return ResponseEntity.ok(Map.of("message", "URL deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // redirect short url to original
    @GetMapping("/s/{shortCode}")
    public void redirect(@PathVariable String shortCode, HttpServletResponse response) throws IOException {
        String originalUrl = urlService.resolveAndTrack(shortCode);

        if (originalUrl == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Short URL not found or has expired.");
            return;
        }

        response.sendRedirect(originalUrl);
    }
}
