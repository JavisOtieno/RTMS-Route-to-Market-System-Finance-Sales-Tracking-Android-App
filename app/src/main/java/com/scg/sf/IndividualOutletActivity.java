package com.scg.sf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.scg.sf.models.Outlet;
import com.scg.sf.util.Toasts;
import com.squareup.picasso.Picasso;

public class IndividualOutletActivity extends AppCompatActivity {
    private Outlet mOutlet;
    private EditText mOutletNameText;
    private EditText mContactPersonNameText;
    private EditText mContactPersonPhoneText;
    private EditText mPhoneText;
    private  EditText mLatText;
    private  EditText mLongText;
    private Button viewOnGoogleMapsButton;
    private Button viewOnMapsButton;
    private ImageView outletImageView;
    private Button contactButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_outlet);

        initToolbar();

        mOutlet=(Outlet)getIntent().getSerializableExtra("outlet");
        viewOnGoogleMapsButton = (Button) findViewById(R.id.viewOnGoogleMapsButton);
        viewOnMapsButton = (Button) findViewById(R.id.viewOnMapsButton);
        mOutletNameText = (EditText) findViewById(R.id.outletNameText);
        mContactPersonNameText=(EditText) findViewById(R.id.contactPersonText);
        mContactPersonPhoneText=(EditText) findViewById(R.id.contactPersonPhoneText);
        mPhoneText=(EditText) findViewById(R.id.phoneText);
        mLatText=(EditText) findViewById(R.id.latText);
        mLongText=(EditText) findViewById(R.id.longText);
        outletImageView = (ImageView) findViewById(R.id.outletImageView);
        contactButton = (Button) findViewById(R.id.contactButton);

        mOutletNameText.setText(mOutlet.name);
        mContactPersonNameText.setText(mOutlet.contact_person_name);
        mContactPersonPhoneText.setText(mOutlet.phone);
        mLatText.setText(mOutlet.lat);
        mLongText.setText(mOutlet.lon);

        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+mOutlet.phone));
                startActivity(intent);
            }
        });

        System.out.println("ImageUrl: "+Constants.STORAGE_URL+mOutlet.image);

        Picasso.get()
                .load(Constants.STORAGE_URL+mOutlet.image)
//                .placeholder(R.drawable.placeholder) // Optional placeholder image
                .error(R.drawable.no_image_available) // Optional error image
                .into(outletImageView);

       viewOnGoogleMapsButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               String strUri = "http://maps.google.com/maps?q=loc:" + mOutlet.lat + "," + mOutlet.lon + " (" + mOutlet.name + ")";
               Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(strUri));

// Check if there's an app that can handle the intent
               if (intent.resolveActivity(getPackageManager()) != null) {
                   try {
                       startActivity(intent);
                   } catch (ActivityNotFoundException e) {
                       // Handle the case where no app can handle the intent
                       Toasts.toastIconError(IndividualOutletActivity.this,"No application available to open maps");
                       FirebaseCrashlytics.getInstance().recordException(e);

                   }
               } else {
                   // Handle the case where no app can handle the intent
                   Toasts.toastIconError(IndividualOutletActivity.this,"No application available to open maps");
                   FirebaseCrashlytics.getInstance().recordException(new Exception("IndividualOutletActivity: Attempt to open google maps failed: " +
                           "ThisLine: intent.resolveActivity(getPackageManager()) != null"));

               }

//               String strUri = "http://maps.google.com/maps?q=loc:" + mOutlet.lat + "," + mOutlet.lon + " (" + mOutlet.name + ")";
//               Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
//               intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
//               startActivity(intent);

           }
       });




    }
    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Customer");
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