package com.app.sha.attar.invoice.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.listener.BillingClickListener;
import com.app.sha.attar.invoice.model.ExpenseModel;
import com.app.sha.attar.invoice.model.SalesPersonModel;
import com.app.sha.attar.invoice.viewholder.ExpenseViewHolder;
import com.app.sha.attar.invoice.viewholder.SalesPersonViewHolder;

import java.util.ArrayList;
import java.util.List;

public class SalesPersonViewAdapter extends RecyclerView.Adapter<SalesPersonViewHolder>{
    Context context;
    List<SalesPersonModel> contentList = new ArrayList<>();
    BillingClickListener clickListener;

    public SalesPersonViewAdapter(Context context, List<SalesPersonModel> contentList, BillingClickListener clickListener) {
        this.context = context;
        this.contentList = contentList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public SalesPersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SalesPersonViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sales_person_item_design, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SalesPersonViewHolder holder, int position) {
        int index = holder.getAdapterPosition();

        holder.name.setText(contentList.get(index).getName());
        holder.phoneNo.setText(contentList.get(index).getPhoneNo());
        holder.type.setText(contentList.get(index).getType());

        holder.status.setText(contentList.get(index).isActive()?"ACTIVE":"INACTIVE");

        if(contentList.get(index).isActive()){
            holder.status.setBackgroundResource(R.color.app_green);
        }else{
            holder.status.setBackgroundResource(R.color.app_color);
        }

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(index,"EDIT");
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(index,"DELETE");
            }
        });
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }
}
