package com.linksnip.controller;

import com.linksnip.dto.UrlRequest;
import com.linksnip.dto.UrlResponse;
import com.linksnip.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
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

    // builds base url from the incoming request (works on localhost, render, any domain)
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto");
        if (scheme == null) scheme = request.getScheme();

        String host = request.getHeader("X-Forwarded-Host");
        if (host == null) host = request.getHeader("Host");

        return scheme + "://" + host;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<?> shortenUrl(@Valid @RequestBody UrlRequest request, HttpServletRequest httpRequest) {
        try {
            UrlResponse response = urlService.createShortUrl(request, getBaseUrl(httpRequest));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/urls")
    public ResponseEntity<List<UrlResponse>> getAllUrls(HttpServletRequest httpRequest) {
        return ResponseEntity.ok(urlService.getAllUrls(getBaseUrl(httpRequest)));
    }

    @GetMapping("/api/urls/{shortCode}/stats")
    public ResponseEntity<?> getStats(@PathVariable String shortCode, HttpServletRequest httpRequest) {
        try {
            UrlResponse response = urlService.getStats(shortCode, getBaseUrl(httpRequest));
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
