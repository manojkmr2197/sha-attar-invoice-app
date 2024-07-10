package com.app.sha.attar.invoice.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.listener.ClickListener;
import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.viewholder.AccessoriesViewHolder;
import com.app.sha.attar.invoice.viewholder.ProductViewHolder;

import java.util.ArrayList;
import java.util.List;

public class AccessoriesViewAdapter  extends RecyclerView.Adapter<AccessoriesViewHolder> {

    Context context;
    List<AccessoriesModel> contentList = new ArrayList<>();
    ClickListener clickListener;

    public AccessoriesViewAdapter(Context context, List<AccessoriesModel> contentList, ClickListener clickListener) {
        this.context = context;
        this.contentList = contentList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public AccessoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AccessoriesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.accessories_item_design, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AccessoriesViewHolder holder, int position) {
        int index = holder.getAdapterPosition();

        holder.name.setText(contentList.get(index).getName());
        holder.price.setText("Rs. "+String.valueOf(contentList.get(index).getPrice()));

        holder.edit.setOnClickListener(new View.OnClickListener() {
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
