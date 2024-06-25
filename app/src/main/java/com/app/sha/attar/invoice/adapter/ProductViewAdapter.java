package com.app.sha.attar.invoice.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.listener.ClickListener;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.viewholder.ProductViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ProductViewAdapter extends RecyclerView.Adapter<ProductViewHolder> {

    Context context;
    List<ProductModel> contentList = new ArrayList<>();
    ClickListener clickListener;

    public ProductViewAdapter(Context context, List<ProductModel> contentList, ClickListener clickListener) {
        this.context = context;
        this.contentList = contentList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item_design, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        int index = holder.getAdapterPosition();

        holder.name.setText(contentList.get(index).getName());
        holder.code.setText(contentList.get(index).getCode());
        holder.price.setText(contentList.get(index).getPrice());
        holder.owner.setText(contentList.get(index).getOwner());
        holder.status.setChecked("Y".equals(contentList.get(index).getIsavailable()));
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.click(index);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

}
