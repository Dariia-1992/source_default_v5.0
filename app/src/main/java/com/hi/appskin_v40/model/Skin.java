package com.hi.appskin_v40.model;

import com.hi.appskin_v40.utils.MD5;

import android.text.format.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Skin {
    private final String id = UUID.randomUUID().toString();
    private String category;
    private Date date;
    private String description;
    private String mod;
    private ArrayList<String> screenShots;
    private String thumbnail;
    private String title;

    public String getId() { return id; }
    public String getCategory() { return category; }
    public Date getDate() { return date; }
    public String getDescription() { return description; }
    public String getMod() { return mod; }
    public ArrayList<String> getScreenShots() { return screenShots; }
    public String getThumbnail() { return thumbnail; }
    public String getTitle() { return title; }

    public String generateKey() {
        return MD5.generate(title + mod);
    }
}
