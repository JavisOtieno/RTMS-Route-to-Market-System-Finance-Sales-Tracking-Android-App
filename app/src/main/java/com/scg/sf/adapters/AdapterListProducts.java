package com.scg.sf.adapters;


import static java.lang.Integer.parseInt;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.scg.sf.models.Product;
import com.scg.sf.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdapterListProducts extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Product> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Product obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListProducts(Context context, List<Product> items) {
        this.items = items;
        ctx = context;
    }
    public AdapterListProducts(List<Product> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;
        public TextView skuQtyAmount;
        public TextView value;
        public TextView price;
        public View lyt_parent;
        public TextView productPriceTextView;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            name = (TextView) v.findViewById(R.id.name);
            skuQtyAmount = (TextView) v.findViewById(R.id.description);
            price = (TextView) v.findViewById(R.id.price);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
            productPriceTextView = (TextView) v.findViewById(R.id.productPriceTextView);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale_product, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            Product p = items.get(position);
            view.name.setText(p.name);
            view.skuQtyAmount.setText(p.sku);
            if(p.targetType.equals("amount")){
                view.productPriceTextView.setText(
                        "Target Amt: KSh "+NumberFormat.getNumberInstance(Locale.US).format(parseInt(p.target)));
                view.skuQtyAmount.setText(
                        "Current Month: KSh "+NumberFormat.getNumberInstance(Locale.US).format(parseInt(p.currentSalesTotal)));
            }
            else if(p.targetType.equals("quantity")){
                view.productPriceTextView.setText(
                        "Target Qty: "+NumberFormat.getNumberInstance(Locale.US).format(parseInt(p.target)));
                view.skuQtyAmount.setText(
                        "Current Month: "+NumberFormat.getNumberInstance(Locale.US).format(parseInt(p.currentSalesQuantity)));


            }
            else{
                view.productPriceTextView.setText("KSh " + NumberFormat.getNumberInstance(Locale.US).format(parseInt(p.price)));

            }
            view.price.setText(p.date);
            ;

            //Tools.displayImageRound(ctx, view.image, p.image);
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
