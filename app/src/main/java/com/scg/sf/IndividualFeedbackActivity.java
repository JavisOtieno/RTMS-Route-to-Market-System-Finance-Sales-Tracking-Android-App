package com.scg.sf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import com.scg.sf.models.Feedback;

public class IndividualFeedbackActivity extends AppCompatActivity {
    private Feedback mFeedback;
    private String mOutletName;
    private String mSellerName;
    private String mPieces;
    private String mValue;
    private String mProducts;
    private EditText mOutletNameText;
    private EditText mValueText;
    private EditText mFeedbackText;
    private EditText mDateTimeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_feedback);

        initToolbar();

        mFeedback=(Feedback) getIntent().getSerializableExtra("feedback");
        mOutletNameText = (EditText) findViewById(R.id.outletNameText);
        mFeedbackText=(EditText) findViewById(R.id.feedbackText);
        mDateTimeText=(EditText) findViewById(R.id.dateTimeText);

        mDateTimeText.setText(mFeedback.created_at);
        mFeedbackText.setText(mFeedback.feedback);
        mOutletNameText.setText(mFeedback.created_outlet);

        mDateTimeText.setEnabled(false);
        mFeedbackText.setEnabled(false);
        mOutletNameText.setEnabled(false);



    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Feedback");
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