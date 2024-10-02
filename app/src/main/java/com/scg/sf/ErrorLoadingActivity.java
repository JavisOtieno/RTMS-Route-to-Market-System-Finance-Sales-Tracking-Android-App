package com.scg.sf;



import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ErrorLoadingActivity extends AppCompatActivity {
//    private ProgressBar progress_bar;
    private LinearLayout lyt_no_connection;
    private String mCallingActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_loading);

        Intent intent = getIntent();
        mCallingActivity = intent.getStringExtra("callingActivity");

        initToolbar(mCallingActivity);
        initComponent(mCallingActivity);


    }

    private void initToolbar(String activityTitle) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Error");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //Tools.setSystemBarColor(this);
    }

    private void initComponent(String callingActivity) {
//        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
        lyt_no_connection = (LinearLayout) findViewById(R.id.lyt_no_connection);

//        progress_bar.setVisibility(View.GONE);
        lyt_no_connection.setVisibility(View.VISIBLE);

        lyt_no_connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                if(callingActivity.equals("Products")){
                    Intent intent = new Intent(ErrorLoadingActivity.this, ProductsListActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                }else if(callingActivity.equals("Customers")){
                    Intent intent = new Intent(ErrorLoadingActivity.this, CustomersActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }else if (callingActivity.equals("Visits")){
                    Intent intent = new Intent(ErrorLoadingActivity.this,VisitsActivity.class);
                    startActivity(intent);
                    finish();
                }else if(callingActivity.equals("POSAudits")){
                    Intent intent = new Intent(ErrorLoadingActivity.this,POSAuditsListActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if(callingActivity.equals("CompetitiveAnalysis")){
                    Intent intent = new Intent(ErrorLoadingActivity.this, CompetitiveAnalysisListActivity.class);
                    startActivity(intent);
                    finish();
                }else if(callingActivity.equals("Feedback")){
                    Intent intent = new Intent(ErrorLoadingActivity.this,FeedbackListActivity.class);
                    startActivity(intent);
                    finish();
                }else if(callingActivity.equals("Profile")){
                    Intent intent = new Intent(ErrorLoadingActivity.this,ProfileActivity.class);
                    startActivity(intent);
                    finish();
                }else if(callingActivity.equals("Clockin")){
                    Intent intent = new Intent(ErrorLoadingActivity.this,ClockinActivity.class);
                    startActivity(intent);
                    finish();
                }else if(callingActivity.equals("SelectExistingOutlet")){
                    Intent intent = new Intent(ErrorLoadingActivity.this,SelectExistingCustomerActivity.class);
                    startActivity(intent);
                    finish();
                }else if(callingActivity.equals("visitSales")){
                    Intent intent = new Intent(ErrorLoadingActivity.this,VisitSalesActivity.class);
                    startActivity(intent);
                    finish();
                }else if(callingActivity.equals("visitPOSAudits")){
                    Intent intent = new Intent(ErrorLoadingActivity.this,VisitPOSAuditsActivity.class);
                    startActivity(intent);
                    finish();
                }else if(callingActivity.equals("visitCompetitiveAnalysis")){
                    Intent intent = new Intent(ErrorLoadingActivity.this,VisitCompetitiveAnalysisActivity.class);
                    startActivity(intent);
                    finish();
                }else if(callingActivity.equals("visitFeedback")){
                    Intent intent = new Intent(ErrorLoadingActivity.this,VisitFeedbackActivity.class);
                    startActivity(intent);
                    finish();
                }else if(callingActivity.equals("routeplans")){
                    Intent intent = new Intent(ErrorLoadingActivity.this,RouteplansActivity.class);
                    startActivity(intent);
                    finish();
                }else if(callingActivity.equals("ProductsCheck")){
                    Intent intent = new Intent(ErrorLoadingActivity.this,ProductsCheckListActivity.class);
                    startActivity(intent);
                    finish();
                }




//                progress_bar.setVisibility(View.VISIBLE);
//                lyt_no_connection.setVisibility(View.GONE);
//
//
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        if (isNetworkConnected() == true) {
//                            Intent intent = new Intent(ErrorLoadingActivity.this, ProductsListActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
//                        }else {
//                            progress_bar.setVisibility(View.GONE);
//                            lyt_no_connection.setVisibility(View.VISIBLE);
//                        }
//
//
//                    }
//                }, 1000);


            }
        });
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_setting, menu);
        return true;
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
                onBackPressed();
//            Intent intent = new Intent(ErrorLoadingActivity.this, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
                return true;
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


//    private boolean isNetworkConnected() {
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
//    }
}