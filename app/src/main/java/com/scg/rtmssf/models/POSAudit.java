package com.scg.rtmssf.models;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.ArrayList;

public class POSAudit implements Serializable {

    public String image;
    public Drawable imageDrw;
    public String name;
    public String time;
    public String price;
    public ArrayList<POSAuditItem> posAuditItems;
    public boolean section = false;

    public POSAudit() {
    }

    public POSAudit(String name, boolean section) {
        this.name = name;
        this.section = section;
    }

}
