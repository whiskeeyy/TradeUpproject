package com.example.tradeupproject.models;  // hoặc package bạn đang dùng

import com.google.firebase.Timestamp;
import java.util.List;

public class Listing {
    private String id;
    private String title;
    private long price;                 // numeric
    private String category;
    private String condition;
    private String description;
    private String location;
    private String status;              // thay cho "behavior"
    private List<String> tags;
    private String sellerId;
    private List<String> images;
    private int views;
    private Timestamp createdAt;

    // Firestore đòi constructor không-arg
    public Listing() { }

    // Optional: constructor đầy đủ nếu bạn khởi tạo thủ công
    public Listing(String title, long price, String category, String condition,
                   String description, String location, String status,
                   List<String> tags, String sellerId, List<String> images,
                   int views, Timestamp createdAt) {
        this.title = title;
        this.price = price;
        this.category = category;
        this.condition = condition;
        this.description = description;
        this.location = location;
        this.status = status;
        this.tags = tags;
        this.sellerId = sellerId;
        this.images = images;
        this.views = views;
        this.createdAt = createdAt;
    }

    // ─── Getters ─────────────────────────────────────────────
    public String getTitle()          { return title; }
    public long getPrice()            { return price; }
    public String getCategory()       { return category; }
    public String getCondition()      { return condition; }
    public String getDescription()    { return description; }
    public String getLocation()       { return location; }
    public String getStatus()         { return status; }
    public List<String> getTags()     { return tags; }
    public String getSellerId()       { return sellerId; }
    public List<String> getImages()   { return images; }
    public int getViews()             { return views; }
    public Timestamp getCreatedAt()   { return createdAt; }

    // ─── (Optional) Setters nếu bạn cần cập nhật từ client ──
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setTitle(String t)         { title = t; }
    public void setPrice(long p)           { price = p; }
    public void setCategory(String c)      { category = c; }
    public void setCondition(String c)     { condition = c; }
    public void setDescription(String d)   { description = d; }
    public void setLocation(String l)      { location = l; }
    public void setStatus(String s)        { status = s; }
    public void setTags(List<String> t)    { tags = t; }
    public void setSellerId(String s)      { sellerId = s; }
    public void setImages(List<String> i)  { images = i; }
    public void setViews(int v)            { views = v; }
    public void setCreatedAt(Timestamp t)  { createdAt = t; }
}
