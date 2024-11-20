package com.scg.rtmssf.util;

import android.content.Context;
import com.google.android.gms.location.GeofenceStatusCodes;

public class GeofenceErrorMessages {

    public static String getErrorString(Context context, int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Geofence service is not available now.";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Your app has registered too many geofences.";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "You have provided too many PendingIntents to the addGeofences() call.";
            default:
                return "Unknown error.";
        }
    }
}
