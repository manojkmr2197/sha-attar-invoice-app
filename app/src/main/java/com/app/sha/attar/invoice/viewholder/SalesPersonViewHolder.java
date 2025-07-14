package com.app.sha.attar.invoice.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;

public class SalesPersonViewHolder extends RecyclerView.ViewHolder {

    public Button delete, edit;
    public TextView name,type,status,phoneNo;

    public SalesPersonViewHolder(@NonNull View itemView) {
        super(itemView);
        delete = (Button) itemView.findViewById(R.id.sales_person_item_delete);
        edit = (Button) itemView.findViewById(R.id.sales_person_item_edit);
        name = (TextView) itemView.findViewById(R.id.sales_person_item_name);
        phoneNo = (TextView) itemView.findViewById(R.id.sales_person_item_phoneNo);
        type = (TextView) itemView.findViewById(R.id.sales_person_item_type);
        status = (TextView) itemView.findViewById(R.id.sales_person_item_status);
    }
}
