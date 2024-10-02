package com.scg.sf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import com.scg.sf.models.CompetitiveAnalysis;

public class IndividualCompetitiveAnalysisActivity extends AppCompatActivity {
    private CompetitiveAnalysis mCompetiveAnalysis;

    private EditText mOutletNameText;
    private EditText mBrandVisibilityText;
    private EditText mPricingText;
    private EditText mDateTimeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_competitive_analysis);

        initToolbar();

        mCompetiveAnalysis=(CompetitiveAnalysis)getIntent().getSerializableExtra("competitiveAnalysis");

        mOutletNameText = (EditText) findViewById(R.id.outletNameText);
        mBrandVisibilityText=(EditText) findViewById(R.id.brandVisibilityText);
        mPricingText=(EditText) findViewById(R.id.pricingText);
        mDateTimeText=(EditText) findViewById(R.id.dateTimeText);

        mDateTimeText.setText(mCompetiveAnalysis.created_at);
        mBrandVisibilityText.setText(mCompetiveAnalysis.brandVisibility);
        mPricingText.setText(mCompetiveAnalysis.pricing);
        mOutletNameText.setText(mCompetiveAnalysis.created_outlet);

        mDateTimeText.setEnabled(false);
        mPricingText.setEnabled(false);
        mOutletNameText.setEnabled(false);
        mBrandVisibilityText.setEnabled(false);



    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Competitive Analysis");
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