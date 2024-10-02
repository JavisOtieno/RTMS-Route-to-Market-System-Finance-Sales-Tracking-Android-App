package com.scg.sf;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.scg.sf.models.Visit;

public class IndividualVisitActivity extends AppCompatActivity {
    private Visit mVisit;
    private EditText mOutletNameText;
    private EditText mCheckinTimeText;
    private EditText mCheckoutTimeText;
    private EditText mDurationText;

    private Button viewSalesButton;
    private Button viewPOSAuditsButton;
    private Button viewCompetitiveAnalysisButton;
    private Button viewFeedbackButton;
    private Button viewOnMapsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_visit);

        initToolbar();

        mVisit=(Visit)getIntent().getSerializableExtra("visit");
        viewOnMapsButton = (Button) findViewById(R.id.viewOnMapsButton);
        mOutletNameText = (EditText) findViewById(R.id.outletNameText);
        mCheckinTimeText=(EditText) findViewById(R.id.checkinText);
        mCheckoutTimeText=(EditText) findViewById(R.id.checkoutText);
        mDurationText=(EditText) findViewById(R.id.durationText);
        viewFeedbackButton = (Button) findViewById(R.id.viewFeedbackButton);
        viewCompetitiveAnalysisButton = (Button) findViewById(R.id.viewCompetitiveAnalysisButton);
        viewPOSAuditsButton = (Button) findViewById(R.id.viewPOSAuditsButton);
        viewSalesButton = (Button) findViewById(R.id.viewSalesButton);

        mOutletNameText.setText(mVisit.outletName);
        mCheckinTimeText.setText(mVisit.date);
        mCheckoutTimeText.setText(mVisit.checkoutTime);
        mDurationText.setText(mVisit.duration);

        mOutletNameText.setEnabled(false);
        mCheckinTimeText.setEnabled(false);
        mCheckoutTimeText.setEnabled(false);
        mDurationText.setEnabled(false);

        viewSalesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IndividualVisitActivity.this, VisitSalesActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("visitId", mVisit.id);
                intent.putExtra("outletName", mVisit.outletName);
                intent.putExtra("outletId", "IndividualVisitNotRequired");
                startActivity(intent);
            }
        });

//        viewPOSAuditsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(IndividualVisitActivity.this, VisitPOSAuditsActivity.class);
//                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra("visitId", mVisit.id);
//                intent.putExtra("outletName", mVisit.outletName);
//                intent.putExtra("outletId", "IndividualVisitNotRequired");
//                startActivity(intent);
//            }
//        });

        viewCompetitiveAnalysisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IndividualVisitActivity.this, VisitCompetitiveAnalysisActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("visitId", mVisit.id);
                intent.putExtra("outletName", mVisit.outletName);
                intent.putExtra("outletId", "IndividualVisitNotRequired");
                startActivity(intent);

            }
        });

        viewFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IndividualVisitActivity.this, VisitFeedbackActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("visitId", mVisit.id);
                intent.putExtra("outletName", mVisit.outletName);
                intent.putExtra("outletId", "IndividualVisitNotRequired");
                startActivity(intent);

            }
        });

        viewOnMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(IndividualVisitActivity.this, MarkerOnMapsActivity.class);
                intent.putExtra("lat",mVisit.outletLat);
                intent.putExtra("long",mVisit.outletLong);
                //add other outlet fields later
                intent.putExtra("name",mVisit.outletName);
                startActivity(intent);


            }
        });




    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Visit");
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
}