package com.scg.rtmssf.adapters;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.scg.rtmssf.OnEditTextChangeListener;
import com.scg.rtmssf.models.Product;
import com.scg.rtmssf.R;
import com.scg.rtmssf.util.LongInputFilter;
import com.scg.rtmssf.util.MinMaxFilter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdapterListInputProducts extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Product> items = new ArrayList<>();
    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private String callingActivity;
    private Integer totalValue;
    private OnEditTextChangeListener editTextChangeListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Product obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListInputProducts(Context context, List<Product> items) {
        this.items = items;
        ctx = context;
    }
    public AdapterListInputProducts(Context context,String callingActivity, List<Product> items) {
        ctx = context;
        this.callingActivity = callingActivity;
        this.items = items;
    }
    public AdapterListInputProducts(List<Product> items) {
        this.items = items;
    }

    public AdapterListInputProducts(Context context,String callingActivity,
                                    List<Product> items,
                                    OnEditTextChangeListener editTextChangeListener) {
        ctx = context;
        this.callingActivity = callingActivity;
        this.editTextChangeListener = editTextChangeListener;
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;
        public TextView date;
        public TextView value;
        public TextView price;
        public View lyt_parent;
        public EditText productCount;
        public TextView productStock;
        public TextWatcher textWatcher;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            name = (TextView) v.findViewById(R.id.name);
            date = (TextView) v.findViewById(R.id.description);
            price = (TextView) v.findViewById(R.id.price);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
            productCount = (EditText) v.findViewById(R.id.productCount);
            if(callingActivity.equals("makeSale")){
                productStock = (TextView) v.findViewById(R.id.productStock);
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_posaudit_add, parent, false);
        //EditText productCount = v.findViewById(R.id.productCount);
        if(callingActivity.equals("makeSale")){
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_check_sales, parent, false);
        }
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            int position2 = holder.getAdapterPosition();

            if(position2 != RecyclerView.NO_POSITION){

                if (view.textWatcher != null) {
                    view.productCount.removeTextChangedListener(view.textWatcher);
                }

            Product p = items.get(position2);
            view.name.setText(p.name);
            if(p.productType.equals("service") && callingActivity.equals("makeSale")) {
                if(p.targetType.equals("amount")){
                    view.productCount.setHint("Sale Amount");
                }else if(p.targetType.equals("quantity")){
                    view.productCount.setHint("Sale Quantity");
                }

            }

            // Remove existing TextWatcher if it exists

                InputFilter minMaxFilter = new MinMaxFilter("0", p.stock + "");
                // Create the LongInputFilter
                InputFilter longInputFilter = new LongInputFilter();

            if (callingActivity.equals("makeSale")) {
                if(!p.productType.equals("service")) {
                    // Apply both filters
                    view.productCount.setFilters(new InputFilter[]{minMaxFilter, longInputFilter});
//                    view.productCount.setFilters(new InputFilter[]{new MinMaxFilter("0", p.stock + "")},);
                }else{
                    view.productCount.setFilters(new InputFilter[]{longInputFilter});
                }
                //productCountEditText.setText("40");
                System.out.println("EditText: Registers Loop");
            }


                // view.date.setText(p.price);
            if(p.productType.equals("service")){
                view.price.setText("N/A");

            }else{
                view.price.setText(
                        "KSh. " + NumberFormat.getNumberInstance(Locale.US).format(
                                Integer.parseInt(p.price))
                );
            }


            if (callingActivity.equals("makeSale")) {
                if(p.productType.equals("service")) {
                    view.productStock.setText("Target "+
                            p.targetType.substring(0, 1).toUpperCase() +
                            p.targetType.substring(1)+" : "+
                            NumberFormat.getNumberInstance(Locale.US).format(Integer.parseInt(p.target))
                    );
                }else {
                    view.productStock.setText("Stock : " + p.stock);
                }
            }

            if (p.currentNumberOfProducts == null) {
                view.productCount.setText("");

                System.out.println("PositionRecyclerNull: "+position2+" ProductName: "+
                        p.name+" - ProductId: "+
                        p.id+" - Stock"+p.currentNumberOfProducts);
            }
            else {
                System.out.println("PositionRecycler: "+position2+" ProductName: "+
                        p.name+" - ProductId: "+p.id+" - Stock"+p.currentNumberOfProducts);
//                Toast.makeText(ctx,"PositionRecycler-"+position2+" ProductName: "+
//                        p.name+" - ProductId: "+p.id+" - Stock"+p.currentNumberOfProducts,Toast.LENGTH_SHORT).show();
                view.productCount.setText(p.currentNumberOfProducts + "");
            }



                // Create a new TextWatcher
                view.textWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                        long productPieces = 0;

                        if (!s.toString().equals("")) {
                            productPieces = Long.parseLong(s.toString());
                        }
                        long previousPieces = 0;
                        if(p.currentNumberOfProducts!=null){
                            previousPieces = p.currentNumberOfProducts;
                        }
                        String price;
                        //temporary set price as 1 for services that have actual values
                        if(p.targetType.equals("amount")){
                            price="1";
                        }else{
                            price=p.price;
                        }

                        if (editTextChangeListener != null) {
                            editTextChangeListener.onEditTextChanged(
                                    productPieces*Long.parseLong(price),
                                    previousPieces*Long.parseLong(price)
                            );

                        }

                        if (!s.toString().equals("")) {

                            p.currentNumberOfProducts = Long.parseLong(s.toString());

                        }else{
                            p.currentNumberOfProducts = null;
                        }

                    }
                };


                // Attach the new TextWatcher
                view.productCount.addTextChangedListener(view.textWatcher);

//            view.productCount.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
//                    if (!s.toString().equals("")) {
//                        p.currentNumberOfProducts = Integer.parseInt(s.toString());
//                    }
//
//
//                }
//            });
            //view.productCount.setText(p.currentNumberOfProducts+"");

            //VIEW EDIT TEXT FROM PRODUCTS

            //Tools.displayImageRound(ctx, view.image, p.image);
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });

            totalValue = 0;

            if (callingActivity.equals("makeSale")) {

//                view.productCount.setFilters(new InputFilter[]{new MinMaxFilter("0", p.stock + "")});
//                    //productCountEditText.setText("40");
//                    System.out.println("EditText: Registers Loop");

//            view.productCount.addTextChangedListener(new TextWatcher() {
//
//                public void afterTextChanged(Editable s) {
//
//                    // you can call or do what you want with your EditText here
//
//                    // yourEditText...
//                    System.out.println("productCountAfterChange: " + s);
//                    //toastIconError(String.valueOf(s));
//                    Integer pieces = 0;
//                    if (!s.toString().equals("")) {
//                        pieces = Integer.parseInt(s.toString());
//                    }
//                    p.currentNumberOfProducts=pieces;
//
//                    Integer value = pieces * Integer.parseInt(p.price);
//                    totalValue = totalValue + value;
//                    //System.out.println("productTotalValue:  " + NumberFormat.getNumberInstance(Locale.US).format(totalValue));
//                    //totalValueTextView.setText("KSh. " + NumberFormat.getNumberInstance(Locale.US).format(totalValue));
//                    // Notify the activity of the text change
//                    if (editTextChangeListener != null) {
//                        editTextChangeListener.onEditTextChanged(position, s.toString(),"after");
//                    }
//                    //);
//
//                }
//
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//
//
//
//                }
//
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
////                            System.out.println("productCountChange: "+s);
////                            toastIconError(String.valueOf(s));
//
//                }
//            });


            }
        }

        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
