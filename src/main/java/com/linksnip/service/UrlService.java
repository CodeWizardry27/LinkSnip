package com.linksnip.service;

import com.linksnip.dto.UrlRequest;
import com.linksnip.dto.UrlResponse;
import com.linksnip.model.Url;
import com.linksnip.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UrlService {

    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final UrlRepository urlRepository;

    @Value("${app.default-expiry-days}")
    private int defaultExpiryDays;

    public UrlService(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public UrlResponse createShortUrl(UrlRequest request, String baseUrl) {
        String originalUrl = normalizeUrl(request.getUrl());

        // check if custom alias is already taken
        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            String alias = request.getCustomAlias().trim();
            if (urlRepository.existsByShortCode(alias) || urlRepository.existsByCustomAlias(alias)) {
                throw new IllegalArgumentException("Custom alias '" + alias + "' is already taken.");
            }
        }

        Url url = new Url();
        url.setOriginalUrl(originalUrl);
        url.setClickCount(0L);

        int expiryDays = (request.getExpiryDays() != null && request.getExpiryDays() > 0)
                ? request.getExpiryDays()
                : defaultExpiryDays;
        url.setExpiresAt(LocalDateTime.now().plusDays(expiryDays));

        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            String alias = request.getCustomAlias().trim();
            url.setCustomAlias(alias);
            url.setShortCode(alias);
        } else {
            // save first to get auto-generated ID, then encode
            url.setShortCode("temp");
            url = urlRepository.save(url);
            url.setShortCode(encodeBase62(url.getId()));
        }

        url = urlRepository.save(url);
        return toResponse(url, baseUrl);
    }

    public String resolveAndTrack(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode).orElse(null);
        if (url == null) return null;

        // expired check
        if (url.getExpiresAt() != null && url.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }

        url.setClickCount(url.getClickCount() + 1);
        url.setLastAccessedAt(LocalDateTime.now());
        urlRepository.save(url);

        return url.getOriginalUrl();
    }

    public UrlResponse getStats(String shortCode, String baseUrl) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Short URL not found: " + shortCode));
        return toResponse(url, baseUrl);
    }

    public List<UrlResponse> getAllUrls(String baseUrl) {
        return urlRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(u -> toResponse(u, baseUrl))
                .collect(Collectors.toList());
    }

    public void deleteUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Short URL not found: " + shortCode));
        urlRepository.delete(url);
    }

    // converts numeric ID to base62 string
    private String encodeBase62(long id) {
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(BASE62.charAt((int) (id % 62)));
            id /= 62;
        }
        while (sb.length() < 4) {
            sb.append('a');
        }
        return sb.reverse().toString();
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }
        url = url.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        return url;
    }

    private UrlResponse toResponse(Url url, String baseUrl) {
        UrlResponse res = new UrlResponse();
        res.setId(url.getId());
        res.setOriginalUrl(url.getOriginalUrl());
        res.setShortCode(url.getShortCode());
        res.setShortUrl(baseUrl + "/s/" + url.getShortCode());
        res.setCustomAlias(url.getCustomAlias());
        res.setClickCount(url.getClickCount());
        res.setCreatedAt(url.getCreatedAt());
        res.setExpiresAt(url.getExpiresAt());
        res.setLastAccessedAt(url.getLastAccessedAt());
        return res;
    }
}
