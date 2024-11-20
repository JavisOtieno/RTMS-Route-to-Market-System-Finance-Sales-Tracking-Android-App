package com.scg.rtmssf.models;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class Visit implements Serializable {

    public int image;
    public Drawable imageDrw;
    public String outletName;
    public String outletLat;
    public String outletLong;
    public String date;
    public String duration;
    public String id;
    public String checkoutTime;
    public boolean section = false;

    public Visit() {
    }

    public Visit(String outletName, boolean section) {
        this.outletName = outletName;
        this.section = section;
    }

}
