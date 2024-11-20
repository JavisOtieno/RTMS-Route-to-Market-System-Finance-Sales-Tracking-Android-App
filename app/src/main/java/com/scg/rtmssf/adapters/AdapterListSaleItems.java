package com.scg.rtmssf.adapters;


import static java.lang.Integer.parseInt;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.scg.rtmssf.R;
import com.scg.rtmssf.models.SaleItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdapterListSaleItems extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SaleItem> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, SaleItem obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListSaleItems(Context context, List<SaleItem> items) {
        this.items = items;
        ctx = context;
    }
    public AdapterListSaleItems(List<SaleItem> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView productNameText;
        public TextView piecesText;
        public TextView priceText;
        public TextView valueText;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            productNameText = (TextView) v.findViewById(R.id.productNameText);
            priceText = (TextView) v.findViewById(R.id.priceText);
            piecesText = (TextView) v.findViewById(R.id.piecesText);
            valueText = (TextView) v.findViewById(R.id.valueText);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale_item, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            SaleItem saleItem = items.get(position);
            view.productNameText.setText(saleItem.productName);
            view.piecesText.setText(NumberFormat.getNumberInstance(Locale.US).format(parseInt(saleItem.pieces)));
            view.priceText.setText("KSh. "+NumberFormat.getNumberInstance(Locale.US).format(parseInt(saleItem.price)));
            view.valueText.setText("KSh. "+NumberFormat.getNumberInstance(Locale.US).format(parseInt(saleItem.value)));

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

