package com.scg.rtmssf.models;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class CompetitiveAnalysis implements Serializable {

    public int image;
    public Drawable imageDrw;
    public String brandVisibility;
    public String pricing;
    public String created_at;
    public String created_outlet;
    public String id;
    public boolean section = false;

    public CompetitiveAnalysis() {
    }

    public CompetitiveAnalysis(String name, boolean section) {
        this.brandVisibility = name;
        this.section = section;
    }

}

