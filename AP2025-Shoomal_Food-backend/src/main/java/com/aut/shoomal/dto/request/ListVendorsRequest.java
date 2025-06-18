package com.aut.shoomal.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ListVendorsRequest
{
    private String search;
    @JsonProperty("categories")
    private List<String> keywords;
    public ListVendorsRequest() {}

    public ListVendorsRequest(String search, List<String> keywords)
    {
        this.search = search;
        this.keywords = keywords;
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
}