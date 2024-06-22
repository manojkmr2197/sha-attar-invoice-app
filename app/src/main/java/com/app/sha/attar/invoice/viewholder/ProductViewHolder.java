package com.app.sha.attar.invoice.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;


public class ProductViewHolder extends RecyclerView.ViewHolder {

    public Button edit;
    public TextView name,code,price,owner;
    public SwitchCompat status;
    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);

        edit =(Button)itemView.findViewById(R.id.product_item_edit);
        name =(TextView) itemView.findViewById(R.id.product_item_name);
        code =(TextView) itemView.findViewById(R.id.product_item_code);
        price =(TextView) itemView.findViewById(R.id.product_item_price);
        owner =(TextView) itemView.findViewById(R.id.product_item_owner);
        status =(SwitchCompat) itemView.findViewById(R.id.product_item_switch);


    }
}
