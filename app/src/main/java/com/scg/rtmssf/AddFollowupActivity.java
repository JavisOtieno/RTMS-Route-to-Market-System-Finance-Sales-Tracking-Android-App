package com.scg.rtmssf;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.rtmssf.util.EncryptedPrefsUtil;
import com.scg.rtmssf.util.Toasts;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

public class AddFollowupActivity extends AppCompatActivity {


    private EditText typeEditText;
    private EditText statusEditText;
    private SharedPreferences mPrefs;

    private String status;
    private String type;
    private String time;

    private Integer checkedStatus=-1;
    private Integer checkedAccount=-1;
    private Integer checkedContact=-1;
    private String dateToBeSaved;
    private Integer checkedType=-1;
    private String mUserId;
    private String mAuthToken;
    private Button submitFollowupButton;
    private EditText descriptionEditText;
    private Boolean isTaskRunning = false;
    private String description;
    private String visitId;
    private String outletId;
    private String outletName;
    private String userId;
    private EditText timeInput;
    private String followupWithoutCheckin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_followup);
        mPrefs = getSharedPreferences("label", 0);


        statusEditText = (EditText) findViewById(R.id.statusEditText);

        statusEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStatusDialog(view);
            }
        });

        mUserId = EncryptedPrefsUtil.getString("userId","0");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");

        submitFollowupButton = (Button)findViewById(R.id.submitFollowupButton);
        descriptionEditText = (EditText)findViewById(R.id.descriptionEditText);

        submitFollowupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //   longitude = longEditText.getText().toString();
                //   latitude = latEditText.getText().toString();
                if (!isTaskRunning) {
                    isTaskRunning = true;

                    description = descriptionEditText.getText().toString();
                    time = timeInput.getText().toString();
                    status = statusEditText.getText().toString().toLowerCase();
                    type = typeEditText.getText().toString().toLowerCase();


                    Intent i = getIntent();
                    visitId = i.getStringExtra("visitId");
//                    visitId = null;
                    if(visitId != null && visitId.equals("")){
                        visitId=null;
                    }
                    userId = mUserId;
                    outletId = i.getStringExtra("outletId");
                    outletName = i.getStringExtra("outletName");

                    //long unixTime = System.currentTimeMillis() / 1000L;

//                    new JsonTask().execute(Constants.BASE_URL + "api/addfollowup", "POST",
//                            userId, outletId, visitId, followup);

                    new TokenManager(AddFollowupActivity.this,  (success, result) -> {
                        if (success) {
                            // Handle the JSON result, update UI, etc.
                            System.out.println("API Result: " + result);

                            handleResult(result);

                        } else {
                            // Handle errors (e.g., token refresh failed)
                            Toasts.toastIconError(AddFollowupActivity.this, result);
//                            Intent intent = new Intent(AddFollowupActivity.this, LoginActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
                        }
                    }).executeRequest(Constants.BASE_URL +"api/addfollowup", "POST",
                            "userId",mUserId,"outletId",outletId,"visitId",null,"description",
                            description,"date",dateToBeSaved,"time",time,"status",status,"type",type);
                }


            }
        });

        typeEditText = (EditText) findViewById(R.id.typeEditText);

        typeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTypeDialog(view);
            }
        });

        initToolbar();
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        timeInput = findViewById(R.id.timeInput);
        Calendar calendar = Calendar.getInstance();
        int hourNow = calendar.get(Calendar.HOUR_OF_DAY);
        int minuteNow = calendar.get(Calendar.MINUTE);
        int yearNow = calendar.get(Calendar.YEAR);
        int monthNow = calendar.get(Calendar.MONTH);
        int dayNow = calendar.get(Calendar.DAY_OF_MONTH);

        // Display the current date in EditText
        timeInput.setText(String.format(Locale.getDefault(), "%02d:%02d",
                hourNow, minuteNow));

        timeInput.setOnClickListener(v -> {

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Display the current date in EditText
            timeInput.setText(String.format(Locale.getDefault(), "%02d:%02d",
                    hour, minute));


            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, selectedHour, selectedMinute) -> {
                        timeInput.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
                    }, hour, minute, true);

            timePickerDialog.show();
        });

        EditText dateInput = findViewById(R.id.dateInput);

        dateInput.setText(String.format(Locale.getDefault(), "%02d-%02d-%04d",
                dayNow, monthNow + 1, yearNow));
        dateToBeSaved=String.format(Locale.getDefault(), "%04d-%02d-%02d",
                yearNow, monthNow + 1,dayNow);

        dateInput.setOnClickListener(v -> {
//            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Set selected date to EditText
                        dateInput.setText(String.format(Locale.getDefault(), "%02d-%02d-%04d",
                                selectedDay, selectedMonth + 1, selectedYear));
                        dateToBeSaved=String.format(Locale.getDefault(), "%04d-%02d-%02d",
                                selectedYear, selectedMonth + 1, selectedDay);
                    }, year, month, day);

            datePickerDialog.show();
        });

    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Follow up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }


    private void handleResult(String result){
        isTaskRunning = false;

        System.out.println("Result: " + result);
        try {

            //Submit Failed means server returns nothing
            //Error saving means server responds with something other than success
            if (result != null) {
                JSONObject jsonObject = new JSONObject(result);
                if(result.contains("errors")){
                    String message = jsonObject.getString("message");
                    Toasts.toastIconError(AddFollowupActivity.this,message);
                    //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                }
                else if(result.contains("success")){


                    Intent intent = new Intent(AddFollowupActivity.this, SelectActionActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    followupWithoutCheckin = mPrefs.getString("callingActivityFollowup","");

//                        Toast.makeText(NewCustomerActivity.this,followupWithoutCheckin,Toast.LENGTH_SHORT).show();

                    if(followupWithoutCheckin.equals("FollowupActivity")){
                        intent = new Intent(AddFollowupActivity.this,
                                FollowupsActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
// Optional: Finish E to ensure it's removed if needed (handled by CLEAR_TOP)
//                        finish();
//                        finishAffinity();
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    }
                    intent.putExtra("lastUpdatedActivity","followup");
                    intent.putExtra("visitId",visitId);
                    intent.putExtra("outletId",outletId);
                    intent.putExtra("outletName",outletName);
                    startActivity(intent);
                }else{
                    Toasts.toastIconError(AddFollowupActivity.this,
                            "Error Saving. Please Try Again");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Add Followup Error: "+result));

                }

            }else
            {
                Toasts.toastIconError(AddFollowupActivity.this,
                        "Submit Followup Failed. Please Try Again");
                FirebaseCrashlytics.getInstance().recordException(new Exception("Add Followup Error: "+result));

            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void showStatusDialog(final View v) {
        final String[] array = new String[]{
                "Pending", "Complete", "Cancelled"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Status");


        builder.setSingleChoiceItems(array, checkedStatus, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((EditText) v).setText(array[i]);
                dialogInterface.dismiss();
                checkedStatus = i;
            }
        });
        builder.show();
    }
    private void showTypeDialog(final View v) {
        final String[] array = new String[]{
                "Call", "Meeting","Visit" ,"Other"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type");


        builder.setSingleChoiceItems(array, checkedType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((EditText) v).setText(array[i]);
                dialogInterface.dismiss();
                checkedType = i;
            }
        });
        builder.show();
    }
}