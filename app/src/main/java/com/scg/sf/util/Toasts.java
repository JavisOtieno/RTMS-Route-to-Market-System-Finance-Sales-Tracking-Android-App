package com.scg.sf.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.scg.sf.R;

public class Toasts {

    public static void toastIconError(Context context, String error) {
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);

        // Inflate view
        View custom_view = LayoutInflater.from(context).inflate(R.layout.toast_icon_text, null);
        ((TextView) custom_view.findViewById(R.id.message)).setText(error);
        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(context.getResources().getColor(R.color.red_600));

        toast.setView(custom_view);
        toast.show();
    }
    public static void toastIconDownload(Context context,String successMessage) {
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);

        //inflate view
        View custom_view = LayoutInflater.from(context).inflate(R.layout.toast_icon_text, null);
        ((TextView) custom_view.findViewById(R.id.message)).setText(successMessage);
        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_file_download);
        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(context.getResources().getColor(R.color.blue_500));

        toast.setView(custom_view);
        toast.show();
    }
    public static void toastIconSuccess(Context context,String successMessage) {
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);

        //inflate view
        View custom_view = LayoutInflater.from(context).inflate(R.layout.toast_icon_text, null);
        ((TextView) custom_view.findViewById(R.id.message)).setText(successMessage);
        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_done);
        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(context.getResources().getColor(R.color.green_500));

        toast.setView(custom_view);
        toast.show();
    }

}
