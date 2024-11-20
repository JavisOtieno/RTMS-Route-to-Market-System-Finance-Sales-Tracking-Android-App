package com.scg.rtmssf.util;

import android.content.Context;
import android.content.Intent;

import java.io.Serializable;

public class PreventDoubleClickOnLists {


        private static long lastIntentTime = 0;
        private static final long MIN_CLICK_INTERVAL = 1000; // 1 second

        public static <T> void showActivityWithoutDoubleClick(String objType,Context context,
                                                              T dataObject, Class<?> targetActivity) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastIntentTime > MIN_CLICK_INTERVAL) {
                lastIntentTime = currentTime;

                Intent intent = new Intent(context, targetActivity);
                intent.putExtra(objType, (Serializable) dataObject);
                context.startActivity(intent);
            } else {
                // Optional: Show a message to the user
                // Toast.makeText(context, "Please wait before clicking again", Toast.LENGTH_SHORT).show();
            }
        }


}
