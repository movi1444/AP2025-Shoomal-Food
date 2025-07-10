package com.aut.shoomal.dto.request;

import java.util.List;

public class ListItemRequest
{
    private String search;
    private List<String> keywords;
    private Integer price;
    public ListItemRequest() {}

    public ListItemRequest(String search, List<String> keywords, Integer price)
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

    public Integer getPrice()
    {
        return price;
    }

    public void setPrice(Integer price)
    {
        this.price = price;
    }
}