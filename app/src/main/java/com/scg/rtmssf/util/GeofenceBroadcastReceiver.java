package com.scg.rtmssf.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(context, geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // Handle the geofence transition
            Log.d(TAG, "Geofence transition enter");

            Toast.makeText(context,"We are within geofence",Toast.LENGTH_SHORT).show();

            // Trigger the intent you want to allow within the geofence
//            Intent allowedIntent = new Intent(context, AllowedActivity.class);
//            allowedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(allowedIntent);
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.d(TAG, "Geofence transition exit");
            Toast.makeText(context,"We are outside the geofence",Toast.LENGTH_SHORT).show();

        } else {
            Log.e(TAG, "Invalid geofence transition: " + geofenceTransition);
            Toast.makeText(context,"Invalid geofence transition",Toast.LENGTH_SHORT).show();

        }
    }
}

