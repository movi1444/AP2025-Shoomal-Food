package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MenuTitleResponse {
    @JsonProperty("title")
    private String title;

    public MenuTitleResponse() {
    }

    public MenuTitleResponse(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}