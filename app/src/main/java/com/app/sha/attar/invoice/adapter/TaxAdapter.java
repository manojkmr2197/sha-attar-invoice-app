package com.app.sha.attar.invoice.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.model.TaxModel;

import java.util.List;

public class TaxAdapter extends RecyclerView.Adapter<TaxAdapter.TaxViewHolder> {

    private List<TaxModel> taxList;
    private Context context;
    private TaxClickListener clickListener;

    public interface TaxClickListener {
        void onTaxClick(TaxModel tax);
    }

    public TaxAdapter(Context context, List<TaxModel> taxList, TaxClickListener clickListener) {
        this.context = context;
        this.taxList = taxList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public TaxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tax_list, parent, false);
        return new TaxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxViewHolder holder, int position) {
        TaxModel tax = taxList.get(position);
        if (tax != null) {
            holder.bind(tax);
        }
    }

    @Override
    public int getItemCount() {
        return taxList != null ? taxList.size() : 0;
    }

    public void updateList(List<TaxModel> newList) {
        this.taxList = newList;
        notifyDataSetChanged();
    }

    class TaxViewHolder extends RecyclerView.ViewHolder {
        TextView tax_name_tv;
        TextView tax_percentage_tv;
        TextView hsn_code_tv;
        TextView tax_status_tv;
        TextView tax_description_tv;

        TaxViewHolder(@NonNull View itemView) {
            super(itemView);
            tax_name_tv = itemView.findViewById(R.id.tax_name);
            tax_percentage_tv = itemView.findViewById(R.id.tax_percentage);
            hsn_code_tv = itemView.findViewById(R.id.hsn_code);
            tax_status_tv = itemView.findViewById(R.id.tax_status);
            tax_description_tv = itemView.findViewById(R.id.tax_description);
        }

        void bind(TaxModel tax) {
            if (tax_name_tv != null) {
                tax_name_tv.setText(tax.getTax_name());
            }

            if (tax_percentage_tv != null) {
                tax_percentage_tv.setText(String.format("%.1f%%", tax.getTax_percentage()));
            }

            if (hsn_code_tv != null) {
                hsn_code_tv.setText("HSN: " + tax.getHsn_code());
            }

            if (tax_status_tv != null) {
                boolean isActive = tax.getIs_active() != null && tax.getIs_active();
                tax_status_tv.setText(isActive ? "ACTIVE" : "INACTIVE");
                tax_status_tv.setBackgroundColor(isActive ?
                        android.graphics.Color.parseColor("#9FE2BF") :
                        android.graphics.Color.parseColor("#FFCDD2"));
                tax_status_tv.setTextColor(android.graphics.Color.BLACK);
            }

            if (tax_description_tv != null) {
                tax_description_tv.setText(tax.getDescription());
            }

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onTaxClick(tax);
                }
            });
        }
    }
}

