package com.scg.rtmssf;



import android.content.Context;
import android.content.res.TypedArray;


import com.scg.rtmssf.models.Sale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ResourceType")
public class DataGenerator {


    public static List<Sale> getPeopleData(Context ctx) {
        List<Sale> items = new ArrayList<>();
        TypedArray drw_arr = ctx.getResources().obtainTypedArray(R.array.people_images);
        String name_arr[] = ctx.getResources().getStringArray(R.array.people_names);

        for (int i = 0; i < drw_arr.length(); i++) {
            Sale obj = new Sale();
            obj.image = drw_arr.getResourceId(i, -1);
            obj.name = name_arr[i];
            //obj.email = Tools.getEmailFromName(obj.name);
            obj.imageDrw = ctx.getResources().getDrawable(obj.image);
            items.add(obj);
        }
        Collections.shuffle(items);
        return items;
    }

}
