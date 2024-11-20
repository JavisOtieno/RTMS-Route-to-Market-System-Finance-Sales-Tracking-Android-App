package com.scg.rtmssf;

import static java.lang.Integer.parseInt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scg.rtmssf.models.Product;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Locale;

public class IndividualProductActivity extends AppCompatActivity {
    private Product mProduct;
    private String mOutletName;
    private String mSellerName;
    private String mPieces;
    private String mValue;
    private String mProducts;
    private EditText mProductNameText;
    private EditText mSkuText;
    private EditText mDesciptionText;
    private EditText mPriceText;
    private ImageView productImageView;
    private EditText mStockText;
    private LinearLayout mStockLayout;
    private LinearLayout mSKULayout;
    private LinearLayout mPriceLayout;
    private LinearLayout mTargetLayout;
    private LinearLayout mCurrentLayout;
    private TextView mTargetText;
    private TextView mCurrentText;
    private TextView mTargetLabel;
    //private ImageView productImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_product);

        initToolbar();

        mProduct=(Product)getIntent().getSerializableExtra("product");
        mProductNameText = (EditText) findViewById(R.id.productNameText);
        mDesciptionText=(EditText) findViewById(R.id.descriptionText);
        mPriceText=(EditText) findViewById(R.id.priceText);
        mSkuText=(EditText) findViewById(R.id.skuText);
        productImageView = (ImageView) findViewById(R.id.productImageView);
        mStockText = (EditText) findViewById(R.id.stockText);
        mTargetText = (TextView) findViewById(R.id.targetText);
        mCurrentText = (TextView) findViewById(R.id.currentQtyAmountText);
        mTargetLabel = (TextView) findViewById(R.id.targetTextView);
        //productImageView = (ImageView) findViewById(R.id.productImageView);

        mStockLayout = (LinearLayout) findViewById(R.id.stockLayout);
        mPriceLayout = (LinearLayout) findViewById(R.id.priceLayout);
        mSKULayout = (LinearLayout) findViewById(R.id.SKULayout);
        mTargetLayout = (LinearLayout) findViewById(R.id.targetLayout);
        mCurrentLayout = (LinearLayout) findViewById(R.id.currentLayout);

        if(mProduct.productType.equals("service")){
            mSKULayout.setVisibility(View.GONE);
            mPriceLayout.setVisibility(View.GONE);
            mStockLayout.setVisibility(View.GONE);
            mTargetLayout.setVisibility(View.VISIBLE);
            mCurrentLayout.setVisibility(View.VISIBLE);
        }else{
            mTargetLayout.setVisibility(View.GONE);
            mCurrentLayout.setVisibility(View.GONE);
        }

        mProductNameText.setText(mProduct.name);
        mPriceText.setText(mProduct.price);
        mPriceText.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(mProduct.price)));
        mSkuText.setText(mProduct.sku);
        mDesciptionText.setText(mProduct.description);
        mStockText.setText(NumberFormat.getNumberInstance(Locale.US).format(mProduct.stock));
        mProductNameText.setText(mProduct.name);
        String target="";
        if(mProduct.targetType.equals("amount")){

            mTargetText.setText("KSh "+
                    NumberFormat.getNumberInstance(Locale.US).format(
                            parseInt(mProduct.target)));

            mCurrentText.setText("KSh "+NumberFormat.getNumberInstance(Locale.US).format(parseInt(
                    mProduct.currentSalesTotal
            )));
            mTargetLabel.setText("Target Amount");


        }else if(mProduct.targetType.equals("quantity")) {
            mTargetText.setText(
                    NumberFormat.getNumberInstance(Locale.US).format(
                            parseInt(mProduct.target)));

            mCurrentText.setText(NumberFormat.getNumberInstance(Locale.US).format(parseInt(
                    mProduct.currentSalesQuantity
            )));
            mTargetLabel.setText("Target Quantity");

        }


        Picasso.get()
                .load(Constants.STORAGE_URL+mProduct.image)
//                .placeholder(R.drawable.placeholder) // Optional placeholder image
                .error(R.drawable.no_image_available) // Optional error image
                .into(productImageView);

        //Picasso.get().load(mProduct.image).into(productImageView);

//        Picasso.get()
//                .load(imageUrl)
//                .placeholder(R.drawable.placeholder) // Optional placeholder image
//                .error(R.drawable.error) // Optional error image
//                .into(imageView);

        mProductNameText.setEnabled(false);
        mPriceText.setEnabled(false);
        mSkuText.setEnabled(false);
        mDesciptionText.setEnabled(false);


    }

    private void initToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Product");
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