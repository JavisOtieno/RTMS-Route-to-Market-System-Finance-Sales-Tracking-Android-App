package com.scg.rtmssf.adapters;


import static java.lang.Integer.parseInt;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.scg.rtmssf.R;
import com.scg.rtmssf.models.RouteplanItem;

import java.util.ArrayList;
import java.util.List;

public class AdapterListRoutePlanItems extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<RouteplanItem> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, RouteplanItem obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListRoutePlanItems(Context context, List<RouteplanItem> items) {
        this.items = items;
        ctx = context;
    }
    public AdapterListRoutePlanItems(List<RouteplanItem> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView outletNameText;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            outletNameText = (TextView) v.findViewById(R.id.outletNameText);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routeplan_item, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            RouteplanItem routeplanItem = items.get(position);
            view.outletNameText.setText(routeplanItem.outletName);

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

