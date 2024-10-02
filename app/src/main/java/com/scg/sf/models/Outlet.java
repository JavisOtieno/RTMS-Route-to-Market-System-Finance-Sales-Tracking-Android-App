package com.scg.sf.models;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class Outlet implements Serializable {

    public String image;
    public Drawable imageDrw;
    public String name;
    public String lat;
    public String lon;
    public String contact_person_name;
    public String phone;
    public String id;
    public boolean section = false;
    public String date;

    public Outlet() {
    }

    public Outlet(String name, boolean section) {
        this.name = name;
        this.section = section;
    }

}