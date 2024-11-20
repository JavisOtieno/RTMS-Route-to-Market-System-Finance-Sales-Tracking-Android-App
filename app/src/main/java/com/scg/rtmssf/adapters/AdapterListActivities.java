package com.scg.rtmssf.adapters;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.scg.rtmssf.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdapterListActivities extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Map<String,String>> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Map<String,String> obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListActivities(Context context, List<Map<String,String>> items) {
        this.items = items;
        ctx = context;
    }
    public AdapterListActivities(List<Map<String,String>> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView type;
        public TextView description;
        public TextView time;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            type = (TextView) v.findViewById(R.id.type);
            description = (TextView) v.findViewById(R.id.description);
            time = (TextView) v.findViewById(R.id.time);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkin_activities_list, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            OriginalViewHolder view = (OriginalViewHolder) holder;

            Map<String,String> activity = items.get(position);
            view.type.setText(activity.get("type"));
            view.description.setText(activity.get("description"));
            view.time.setText(activity.get("time"));

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

