package com.app.sha.attar.invoice.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;

public class AccessoriesViewHolder extends RecyclerView.ViewHolder {

    public Button edit;
    public TextView name,price;

    public AccessoriesViewHolder(@NonNull View itemView) {
        super(itemView);

        edit =(Button)itemView.findViewById(R.id.accessories_item_edit);
        name =(TextView) itemView.findViewById(R.id.accessories_item_name);
        price =(TextView) itemView.findViewById(R.id.accessories_item_price);
    }
}
