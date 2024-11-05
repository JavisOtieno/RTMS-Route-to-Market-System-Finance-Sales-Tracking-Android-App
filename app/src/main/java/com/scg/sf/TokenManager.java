package com.scg.sf;

import android.content.Context;

import com.scg.sf.util.EncryptedPrefsUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class TokenManager {
    public interface TokenResultListener {
        void onRequestComplete(boolean success, String result);
    }

    private Context context;
    private String mEmail, mPassword;
    private TokenResultListener listener;

    public TokenManager(Context context, TokenResultListener listener) {
        this.context = context;
        EncryptedPrefsUtil.init(context);

        this.mEmail = EncryptedPrefsUtil.getString("email", "");
        this.mPassword = EncryptedPrefsUtil.getString("password", "");
        this.listener = listener;
    }

    public void executeRequest(String apiUrl, String httpMethod, String... params) {
        long currentTimeMillis = System.currentTimeMillis();
        long tokenExpiry = Long.parseLong(EncryptedPrefsUtil.getString("tokenExpiry", "0"));

        String[] authParams = {"email", mEmail,"password",mPassword};
        if (currentTimeMillis > tokenExpiry) {
            refreshToken(apiUrl, httpMethod, authParams, params);
        } else {
            makeApiRequest(apiUrl, httpMethod, params);
        }
    }

    private void refreshToken(String apiUrl, String httpMethod, String[]... paramGroups) {

            String[] authParams = paramGroups[0];
            String[] params = paramGroups[1];

        new JsonTaskInline(result -> {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    System.out.println("Result: Refreshtoken "+result);
                    if (result.contains("errors")) {
                        listener.onRequestComplete(false, jsonObject.getString("message"));
                    } else if (jsonObject.has("status") && "success".equals(jsonObject.getString("status"))) {
                        System.out.println("refreshState: Token before"+EncryptedPrefsUtil.getString("authToken", ""));

                        EncryptedPrefsUtil.saveString("authToken", jsonObject.getString("authToken"));
                        EncryptedPrefsUtil.saveString("userId", jsonObject.getString("userId"));
                        long expiryMillis = System.currentTimeMillis() + Constants.EXPIRY_MILLIS;
                        EncryptedPrefsUtil.saveString("tokenExpiry", String.valueOf(expiryMillis));


                        System.out.println("refreshState: Token after "+EncryptedPrefsUtil.getString("authToken", ""));
                        System.out.println("refreshState: Token expiry "+EncryptedPrefsUtil.getString("tokenExpiry", ""));


                        System.out.println("refreshState: token Manager" + jsonObject.getString("authToken"));
                        // After refreshing token, proceed with the original request
                        makeApiRequest(apiUrl, httpMethod, params);

                    } else {
                        listener.onRequestComplete(false, "Failed to refresh token.");
                    }
                } catch (JSONException e) {
                    listener.onRequestComplete(false, e.getMessage());
                }
            } else {
                listener.onRequestComplete(false, "Null response from server.");
            }
        }).execute(Constants.BASE_URL + "api/login", "POST",authParams);
    }

    private void makeApiRequest(String apiUrl, String httpMethod, String... params) {

        new JsonTaskInline(result -> {
            System.out.println("Result: Apirequest "+result);
            System.out.println("Result: Apirequest "+httpMethod+apiUrl+" - "+params);

            if (result != null) {
                listener.onRequestComplete(true, result);

            } else {
                listener.onRequestComplete(false, "Failed to fetch data.");
            }
        }).execute(apiUrl, httpMethod, params);
    }

}
