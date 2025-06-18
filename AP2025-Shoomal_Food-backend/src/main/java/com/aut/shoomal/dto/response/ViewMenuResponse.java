package com.aut.shoomal.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ViewMenuResponse
{
    @JsonProperty("menu_titles")
    private List<String> menuTitles;
    @JsonProperty("menu_title")
    private List<ListItemResponse> foods;

    public ViewMenuResponse() {}
    public ViewMenuResponse(List<String> menuTitles, List<ListItemResponse> foods)
    {
        this.menuTitles = menuTitles;
        this.foods = foods;
    }

    public List<String> getMenuTitles()
    {
        return menuTitles;
    }

    public void setMenuTitles(List<String> menuTitles)
    {
        this.menuTitles = menuTitles;
    }

    public List<ListItemResponse> getFoods()
    {
        return foods;
    }

    public void setFoods(List<ListItemResponse> foods)
    {
        this.foods = foods;
    }
}