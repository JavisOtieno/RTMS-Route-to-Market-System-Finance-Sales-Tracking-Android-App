package com.scg.rtmssf;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.scg.rtmssf.models.Feedback;
import com.scg.rtmssf.models.Followup;

public class IndividualFollowupActivity extends AppCompatActivity {
    private Followup mFollowup;
    private EditText mOutletNameText;
    private EditText mDescriptionText;
    private EditText mDateTimeText;
    private EditText mTimeText;
    private EditText mDateText;
    private EditText mStatusText;
    private EditText mTypeText;
    private Button editFollowupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_individual_followup);

        initToolbar();
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        mFollowup=(Followup) getIntent().getSerializableExtra("followup");
        mOutletNameText = (EditText) findViewById(R.id.outletNameText);
        mDescriptionText=(EditText) findViewById(R.id.descriptionText);
        mDateTimeText=(EditText) findViewById(R.id.dateTimeText);
        mDateText = (EditText) findViewById(R.id.dateText);
        mTimeText = (EditText)findViewById(R.id.timeText);
        mStatusText = (EditText)findViewById(R.id.statusText);
        mTypeText = (EditText) findViewById(R.id.typeText);
        editFollowupButton = (Button) findViewById(R.id.editFollowupButton);


        mOutletNameText.setText(mFollowup.created_outlet);
        mDescriptionText.setText(mFollowup.description);
        mDateTimeText.setText(mFollowup.created_at);
        mDateText.setText(mFollowup.date);
        mTimeText.setText(mFollowup.time);

        mDateTimeText.setEnabled(false);
        mOutletNameText.setEnabled(false);
        mDescriptionText.setEnabled(false);
        mDateText.setEnabled(false);
        mStatusText.setEnabled(false);
        mTypeText.setEnabled(false);
        mTimeText.setEnabled(false);
//        mStatusText.setText(mFollowup.status);
        mStatusText.setText(mFollowup.status.substring(0, 1).toUpperCase()
                + mFollowup.status.substring(1));
        mTypeText.setText(mFollowup.type.substring(0, 1).toUpperCase()
                + mFollowup.type.substring(1));
//        mTypeText.setText(mFollowup.type);

        editFollowupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(IndividualFollowupActivity.this, EditFollowupActivity.class);
                intent.putExtra("followup", mFollowup);
                startActivity(intent);

            }
        });





    }
    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Follow up");
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