package com.scg.sf.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Routeplan implements Serializable {
    public String name;
    public String date;
    public String scheduledDate;
    public ArrayList<RouteplanItem> routeplanItems;
    public String routeplanItemsPreview;


}
