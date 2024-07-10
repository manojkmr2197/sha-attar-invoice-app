package com.app.sha.attar.invoice.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.listener.ClickListener;
import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.BillingItemModel;
import com.app.sha.attar.invoice.model.CustomerHistoryModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.viewholder.CustomerHistoryViewHolder;
import com.app.sha.attar.invoice.viewholder.ProductViewHolder;

import java.util.ArrayList;
import java.util.List;


public class CustomerHistoryAdapter extends RecyclerView.Adapter<CustomerHistoryViewHolder> {

    Context context;
    List<BillingInvoiceModel> contentList = new ArrayList<>();
    ClickListener clickListener;

    public CustomerHistoryAdapter(Context context, List<BillingInvoiceModel> contentList, ClickListener clickListener) {
        this.context = context;
        this.contentList = contentList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CustomerHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomerHistoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_history_item_design, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerHistoryViewHolder holder, int position) {
        int index = holder.getAdapterPosition();

        holder.date.setText(contentList.get(index).getBillingDate().toString());
        holder.discount.setText(String.valueOf(contentList.get(index).getDiscount())+" %");
        holder.amount.setText(String.valueOf(contentList.get(index).getSellingCost()));

        holder.item_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.click(index);
            }
        });

    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }
}
