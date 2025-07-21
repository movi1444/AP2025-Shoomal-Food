package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ListItemRequest {
    @JsonProperty("search")
    private String search;

    @JsonProperty("price")
    private Integer price;

    @JsonProperty("keywords")
    private List<String> keywords;

    public ListItemRequest() {
    }

    public ListItemRequest(String search, Integer price, List<String> keywords) {
        this.search = search;
        this.price = price;
        this.keywords = keywords;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}