package com.scg.sf.models;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.ArrayList;

public class Sale implements Serializable {

    public int image;
    public Drawable imageDrw;
    public String name;
    public String time;
    public String price;
    public String allproductsname;
    public String outletName;
    public String sellerName;
    public String pieces;
    public ArrayList<SaleItem> saleItems;
    public String status;

    public boolean section = false;

    public Sale() {
    }

    public Sale(String name, boolean section) {
        this.name = name;
        this.section = section;
    }

}