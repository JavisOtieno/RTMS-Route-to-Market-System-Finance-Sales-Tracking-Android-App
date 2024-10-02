package com.scg.sf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class ViewActivitiesActivity extends AppCompatActivity {

    private Button viewPOSAuditsButton;
    private Button viewCompetitiveAnalysisButton;
    private Button viewFeedbackButton;
    private String visitId;
    private String outletName;
    private String outletId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_activities);
        initToolbar();

        Intent intent = getIntent();
        visitId = intent.getStringExtra("visitId");
        outletName = intent.getStringExtra("outletName");
        outletId = intent.getStringExtra("outletId");

        viewFeedbackButton = (Button) findViewById(R.id.viewFeedbackButton);
        viewPOSAuditsButton = (Button) findViewById(R.id.viewPOSAuditsButton);
        viewCompetitiveAnalysisButton = (Button) findViewById(R.id.viewCompetitiveAnalysisButton);

        viewFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ViewActivitiesActivity.this,VisitFeedbackActivity.class);
                intent.putExtra("visitId",visitId);
                intent.putExtra("outletName",outletName);
                intent.putExtra("outletId",outletId);
                startActivity(intent);
            }
        });

        viewCompetitiveAnalysisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewActivitiesActivity.this,VisitCompetitiveAnalysisActivity.class);
                intent.putExtra("visitId",visitId);
                intent.putExtra("outletName",outletName);
                intent.putExtra("outletId",outletId);
                startActivity(intent);
            }

        });
        viewPOSAuditsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewActivitiesActivity.this,VisitPOSAuditsActivity.class);
                intent.putExtra("visitId",visitId);
                intent.putExtra("outletName",outletName);
                intent.putExtra("outletId",outletId);
                startActivity(intent);
            }
        });


    }
    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Activities");
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