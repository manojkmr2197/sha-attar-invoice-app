package com.app.sha.attar.invoice.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;

public class CustomerHistoryViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout item_ll;
    public TextView date,amount,discount;
    public CustomerHistoryViewHolder(@NonNull View itemView) {
        super(itemView);

        item_ll =(LinearLayout)itemView.findViewById(R.id.customer_history_item_ll);
        date =(TextView) itemView.findViewById(R.id.customer_history_date);
        amount =(TextView) itemView.findViewById(R.id.customer_history_price);
        discount =(TextView) itemView.findViewById(R.id.customer_history_discount);


    }
}
