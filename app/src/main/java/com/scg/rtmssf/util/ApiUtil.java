package com.scg.rtmssf.util;

import android.content.Context;
import android.content.Intent;

import com.scg.rtmssf.ApiCallback;
import com.scg.rtmssf.Constants;
import com.scg.rtmssf.LoginActivity;
import com.scg.rtmssf.TokenManager;

public class ApiUtil {


    public static void makeApiRequest(Context context, String endpointUrl, ApiCallback callback) {

        new TokenManager(context, (success, result) -> {
            if (success) {
                // Pass the result to the callback
                callback.onSuccess(result);
            } else {
                // Handle token refresh failure
                Toasts.toastIconError(context, result);

                // Redirect to LoginActivity
                Intent intent = new Intent(context, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                callback.onError(result); // Notify callback of the error
            }
        }).executeRequest(Constants.BASE_URL + endpointUrl, "GET");
    }


}
