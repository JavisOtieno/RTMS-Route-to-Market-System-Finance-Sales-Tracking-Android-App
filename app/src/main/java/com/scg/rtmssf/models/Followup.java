package com.scg.rtmssf.models;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class Followup implements Serializable {

    public int image;
    public Drawable imageDrw;
    public String type;
    public String description;
    public String date;
    public String time;
    public String status;
    public String created_at;
    public String created_outlet;
    public String id;
    public boolean section = false;

    public Followup() {
    }

//    public Feedback(String name, boolean section) {
//        this.feedback = name;
//        this.section = section;
//    }

}
