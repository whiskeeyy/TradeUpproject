package com.example.tradeupproject.models;

import java.util.List;

public class Listing {
    private String id;
    private String title;
    private String price;
    private String location;
    private List<String> images;
    // có thể thêm: description, category, condition, tags,...

    public Listing() { /* bắt buộc có constructor trống */ }

    // getters / setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}
