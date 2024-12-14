package com.app.sha.attar.invoice.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.listener.ClickListener;
import com.app.sha.attar.invoice.model.ReportModel;
import com.app.sha.attar.invoice.viewholder.ProductViewHolder;
import com.app.sha.attar.invoice.viewholder.ReportViewHolder;

import java.util.List;

public class ReportViewAdapter extends RecyclerView.Adapter<ReportViewHolder> {

    Context context;
    List<ReportModel> displayList;
    ClickListener listener;

    public ReportViewAdapter(Context context, List<ReportModel> displayList, ClickListener listener) {
        this.context = context;
        this.displayList = displayList;
        this.listener =listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReportViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.report_item_design, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        int index = holder.getAdapterPosition();

        holder.date.setText(displayList.get(index).getDate());
        holder.name.setText(displayList.get(index).getName());
        holder.quantity.setText(String.valueOf(displayList.get(index).getQuantity()));
        holder.actual_amt.setText("Rs. "+String.format("%.1f", displayList.get(index).getActualPrice()));
        holder.selling_amount.setText("Rs. "+String.format("%.1f", displayList.get(index).getSoldPrice()));
        holder.profit.setText("Rs. "+String.format("%.1f", displayList.get(index).getProfit()));

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.click(index);
            }
        });

    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }
}
