package com.app.sha.attar.invoice.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.model.ClientModel;

import java.util.List;
import java.util.Locale;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private final Context context;
    private final List<ClientModel> clientList;
    private final OnClientClickListener clickListener;

    public interface OnClientClickListener {
        void onClientClick(ClientModel client);
    }

    public ClientAdapter(Context context, List<ClientModel> clientList, OnClientClickListener clickListener) {
        this.context = context;
        this.clientList = clientList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.client_report_item_design, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        ClientModel client = clientList.get(position);
        holder.nameTv.setText(client.getName());
        holder.phoneTv.setText(client.getPhone());
        holder.totalSpentTv.setText(String.format(Locale.getDefault(), "₹%.2f", client.getTotalSpent()));

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClientClick(client);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clientList.size();
    }

    public static class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv, phoneTv, totalSpentTv;

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.client_item_name);
            phoneTv = itemView.findViewById(R.id.client_item_phone);
            totalSpentTv = itemView.findViewById(R.id.client_item_total_spent);
        }
    }
}
