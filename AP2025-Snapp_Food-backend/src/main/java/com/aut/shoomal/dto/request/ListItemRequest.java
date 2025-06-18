package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ListItemRequest
{
    private String search;
    @JsonProperty("categories")
    private List<String> keywords;
    private String price;
    public ListItemRequest() {}

    public ListItemRequest(String search, List<String> keywords, String price)
    {
        this.search = search;
        this.keywords = keywords;
        this.price = price;
    }

    public String getSearch()
    {
        return search;
    }

    public void setSearch(String search)
    {
        this.search = search;
    }

    public List<String> getKeywords()
    {
        return keywords;
    }

    public void setKeywords(List<String> keywords)
    {
        this.keywords = keywords;
    }

    public String getPrice()
    {
        return price;
    }

    public void setPrice(String price)
    {
        this.price = price;
    }
}