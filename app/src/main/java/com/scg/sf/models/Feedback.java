package com.scg.sf.models;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class Feedback implements Serializable {

    public int image;
    public Drawable imageDrw;
    public String feedback;
    public String created_at;
    public String created_outlet;
    public String id;
    public boolean section = false;

    public Feedback() {
    }

    public Feedback(String name, boolean section) {
        this.feedback = name;
        this.section = section;
    }

}
