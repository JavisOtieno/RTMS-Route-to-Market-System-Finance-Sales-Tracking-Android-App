package com.scg.sf.models;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class Product implements Serializable {

    public String image;
    public Drawable imageDrw;
    public String name;

    public String sku;
    public String currentSalesQuantity;
    public String currentSalesTotal;
    public String productType;
    public String targetType;
    public String target;
    public String price;
    public String id;
    public String description;
    public Integer stock;
    public Long currentNumberOfProducts;
    public boolean section = false;
    public boolean checked = false;

    public String date;

    public Product() {
    }

    public Product(String name, boolean section) {
        this.name = name;
        this.section = section;
    }

}