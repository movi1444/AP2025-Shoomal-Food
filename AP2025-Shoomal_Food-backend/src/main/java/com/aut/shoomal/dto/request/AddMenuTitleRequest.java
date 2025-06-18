package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddMenuTitleRequest {
    @JsonProperty(value = "title", required = true)
    private String title;

    public AddMenuTitleRequest(){}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
