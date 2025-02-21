package com.scg.rtmssf;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.scg.rtmssf.models.Followup;
import com.scg.rtmssf.util.EncryptedPrefsUtil;
import com.scg.rtmssf.util.Toasts;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class EditFollowupActivity extends AppCompatActivity {

    private EditText typeEditText;
    private EditText statusEditText;

    private String status;
    private String type;
    private String time;

    private Integer checkedStatus=-1;
    private String dateToBeSaved;
    private Integer checkedType=-1;
    private String mUserId;
    private String mAuthToken;
    private Button submitFollowupButton;
    private EditText descriptionEditText;
    private Boolean isTaskRunning = false;
    private String description;
    private Followup mFollowup;
    private EditText dateInput;
    private EditText timeInput;
    private String[] statusArray = new String[]{
            "Pending", "Complete", "Cancelled"
    };
    private String[] typeArray = new String[]{
            "Call", "Meeting","Visit" ,"Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_followup);

        initToolbar();
        mFollowup = (Followup) getIntent().getSerializableExtra("followup");



        mUserId = EncryptedPrefsUtil.getString("userId","0");
        mAuthToken = EncryptedPrefsUtil.getString("authToken", "");

        submitFollowupButton = (Button)findViewById(R.id.submitFollowupButton);
        descriptionEditText = (EditText)findViewById(R.id.descriptionEditText);
        statusEditText = (EditText)findViewById(R.id.statusEditText);
        typeEditText = (EditText)findViewById(R.id.typeEditText);

//        statusEditText = (EditText) findViewById(R.id.statusEditText);
//        typeEditText = (EditText) findViewById(R.id.typeEditText);
        dateInput = (EditText) findViewById(R.id.dateInput);
        timeInput = (EditText) findViewById(R.id.timeInput);

        statusEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStatusDialog(view);
            }
        });



        descriptionEditText.setText(mFollowup.description);
        statusEditText.setText(mFollowup.status.substring(0, 1).toUpperCase()
                + mFollowup.status.substring(1));
        typeEditText.setText(mFollowup.type.substring(0, 1).toUpperCase()
                + mFollowup.type.substring(1));
        timeInput.setText(mFollowup.time);
        dateInput.setText(mFollowup.date);
        dateToBeSaved = mFollowup.date;
        time = mFollowup.time;
        status = mFollowup.status.substring(0, 1).toUpperCase() +
                mFollowup.status.substring(1).toLowerCase();
        checkedStatus = Arrays.asList(statusArray).indexOf(status);
        checkedType = Arrays.asList(typeArray).indexOf(mFollowup.type.substring(0, 1).toUpperCase() +
                mFollowup.type.substring(1).toLowerCase());

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


//                    Intent i = getIntent();
//                    visitId = i.getStringExtra("visitId");
//                    userId = mUserId;
//                    outletId = i.getStringExtra("outletId");
//                    outletName = i.getStringExtra("outletName");

                    //long unixTime = System.currentTimeMillis() / 1000L;

//                    new JsonTask().execute(Constants.BASE_URL + "api/addfollowup", "POST",
//                            userId, outletId, visitId, followup);

                    new TokenManager(EditFollowupActivity.this,  (success, result) -> {
                        if (success) {
                            // Handle the JSON result, update UI, etc.
                            System.out.println("API Result: " + result);

                            handleResult(result);

                        } else {
                            // Handle errors (e.g., token refresh failed)
                            Toasts.toastIconError(EditFollowupActivity.this, result);
//                            Intent intent = new Intent(EditFollowupActivity.this, LoginActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
                        }
                    }).executeRequest(Constants.BASE_URL +"api/editfollowup/"+
                            mFollowup.id, "PUT",
                            "userId",mUserId,"description",
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

        Calendar calendar = Calendar.getInstance();

        timeInput.setText(mFollowup.time);

        timeInput.setOnClickListener(v -> {

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Display the current date in EditText
            timeInput.setText(mFollowup.time);

            LocalTime localTime = LocalTime.parse(mFollowup.time);
            int dbHour = localTime.getHour();  // 9
            int dbMinute = localTime.getMinute();

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, selectedHour, selectedMinute) -> {
                        String timeSelected = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                        timeInput.setText(timeSelected);
//                        mTime = timeSelected;
                    }, dbHour, dbMinute, true);

            timePickerDialog.show();
        });

        EditText dateInput = findViewById(R.id.dateInput);

        LocalDate localDate = LocalDate.parse(mFollowup.date);
        int day = localDate.getDayOfMonth();
        int month = localDate.getMonthValue();
        int year = localDate.getYear();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String formattedDate = localDate.format(formatter);

        dateInput.setText(formattedDate);

        dateToBeSaved = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                year, month , day);

        dateInput.setOnClickListener(v -> {
            LocalDate localDateUpdated = LocalDate.parse(dateToBeSaved);
            int dayUpdated = localDateUpdated.getDayOfMonth();
            int monthUpdated = localDateUpdated.getMonthValue();
            int yearUpdated = localDateUpdated.getYear();
//            Calendar calendar = Calendar.getInstance();
//            int year = calendar.get(Calendar.YEAR);
//            int month = calendar.get(Calendar.MONTH);
//            int day = calendar.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Set selected date to EditText
                        dateInput.setText(String.format(Locale.getDefault(), "%02d-%02d-%04d",
                                selectedDay, selectedMonth + 1, selectedYear));
                        dateToBeSaved = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                                selectedYear, selectedMonth + 1, selectedDay);

                    }, yearUpdated , monthUpdated-1, dayUpdated);

            datePickerDialog.show();
        });
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }
    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Followup");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
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
                    Toasts.toastIconError(EditFollowupActivity.this,message);
                    //Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
                }
                else if(result.contains("success")){
                    String message = jsonObject.getString("message");
                    Intent intent = new Intent(EditFollowupActivity.this, FollowupsActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("lastUpdatedActivity","followup");
                    Toasts.toastIconSuccess(EditFollowupActivity.this,message);
                    startActivity(intent);
                }else{
                    Toasts.toastIconError(EditFollowupActivity.this,
                            "Error Saving. Please Try Again");
                    FirebaseCrashlytics.getInstance().recordException(new Exception("Add Followup Error: "+result));

                }

            }else
            {
                Toasts.toastIconError(EditFollowupActivity.this,
                        "Submit Followup Failed. Please Try Again");
                FirebaseCrashlytics.getInstance().recordException(new Exception("Add Followup Error: "+result));

            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }




}