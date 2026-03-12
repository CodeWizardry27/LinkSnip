package com.linksnip.dto;

import jakarta.validation.constraints.NotBlank;

public class UrlRequest {

    @NotBlank(message = "URL cannot be empty")
    private String url;

    private String customAlias;

    private Integer expiryDays;

    // ----- Getters & Setters -----

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCustomAlias() {
        return customAlias;
    }

    public void setCustomAlias(String customAlias) {
        this.customAlias = customAlias;
    }

    public Integer getExpiryDays() {
        return expiryDays;
    }

    public void setExpiryDays(Integer expiryDays) {
        this.expiryDays = expiryDays;
    }
}
