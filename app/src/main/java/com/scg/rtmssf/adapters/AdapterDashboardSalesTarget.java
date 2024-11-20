package com.scg.rtmssf.adapters;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.scg.rtmssf.R;
import com.scg.rtmssf.models.Product;

import java.util.ArrayList;
import java.util.List;

public class AdapterDashboardSalesTarget extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Product> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view,Product obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterDashboardSalesTarget(Context context, List<Product> items) {
        this.items = items;
        ctx = context;
    }
    public AdapterDashboardSalesTarget(List<Product> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView productName;
        public TextView productTarget;
        public TextView productCurrentValue;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            productName = (TextView) v.findViewById(R.id.productName);
            productTarget = (TextView) v.findViewById(R.id.productTarget);
//            productCurrentValue = (TextView) v.findViewById(R.id.currentSales);
//            saleValue = (TextView) v.findViewById(R.id.saleValue);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_targets, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            Product p = items.get(position);
            view.productName.setText(p.name);
            view.productTarget.setText(p.target);
//            if(p.targetType.equals("amount")){
//
//                view.productCurrentValue.setText(p.currentSalesTotal);
//            }else{
//                view.productCurrentValue.setText(p.currentSalesQuantity);
//            }

//            Integer pendingTargets=0;
//            if(p.targetType.equals("amount")){
//                pendingTargets = Integer.valueOf(p.target)-Integer.valueOf(p.currentSalesTotal);
//                if(pendingTargets<0){
//                    pendingTargets = 0;
//                }
//
//            }else{
//                pendingTargets = Integer.valueOf(p.target)-Integer.valueOf(p.currentSalesQuantity);Z
//                if(pendingTargets<0){
//                    pendingTargets = 0;
//                }
//            }
//            view.productTarget.setText(pendingTargets+"");

            System.out.println("Nametarget"+p.name+" "+p.target);

            //Tools.displayImageRound(ctx, view.image, p.image);
//            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (mOnItemClickListener != null) {
//                        mOnItemClickListener.onItemClick(view, items.get(position), position);
//                    }
//                }
//            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
