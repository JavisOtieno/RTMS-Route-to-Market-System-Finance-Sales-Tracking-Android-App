package com.scg.rtmssf.util;

import android.view.View;

public abstract class DebouncedOnClickListener implements View.OnClickListener {
    private static final long MIN_CLICK_INTERVAL = 2000; // Adjust as needed
    private long lastClickTime;

    public abstract void onDebouncedClick(View v);

    @Override
    public final void onClick(View v) {
        long currentClickTime = System.currentTimeMillis();
        if (currentClickTime - lastClickTime > MIN_CLICK_INTERVAL) {
            lastClickTime = currentClickTime;
            onDebouncedClick(v);
        }
    }
}