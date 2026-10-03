package com.app.sha.attar.invoice.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.model.BillingItemModel;

import java.text.DecimalFormat;
import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartViewHolder> {

    private Context context;
    private List<BillingItemModel> cartItems;
    private boolean isGstApplicable = false;
    private double discountPercentage = 0.0;
    private DecimalFormat df = new DecimalFormat("#.00");

    public CartItemAdapter(Context context, List<BillingItemModel> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    public CartItemAdapter(Context context, List<BillingItemModel> cartItems, boolean isGstApplicable, double discountPercentage) {
        this.context = context;
        this.cartItems = cartItems;
        this.isGstApplicable = isGstApplicable;
        this.discountPercentage = discountPercentage;
    }

    public void setGstDetails(boolean isGstApplicable, double discountPercentage) {
        this.isGstApplicable = isGstApplicable;
        this.discountPercentage = discountPercentage;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item_compact, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        BillingItemModel item = cartItems.get(position);

        holder.itemName.setText(item.getName());
        double itemTotal = item.getPieces() * item.getSellingItemPrice();
        holder.itemPrice.setText("₹" + df.format(itemTotal));

        if ("PRODUCT".equals(item.getType())) {
            holder.itemQuantity.setText("[" + item.getPieces() + " x ₹" + df.format(item.getSellingItemPrice()) + "]");
            String category = item.getProductCategory() != null && !item.getProductCategory().isEmpty() ? 
                    item.getProductCategory().substring(0, 1).toUpperCase() : "P";
            holder.itemType.setText(category + "-" + item.getUnits() + "ML");
            holder.itemType.setVisibility(View.VISIBLE);
        } else {
            holder.itemQuantity.setText("[" + item.getPieces() + " x ₹" + df.format(item.getSellingItemPrice()) + "]");
            holder.itemType.setVisibility(View.GONE);
        }

        if (isGstApplicable && holder.itemTaxInfo != null) {
            double gstRate = item.getGstPercentage() > 0 ? item.getGstPercentage() : 18.0;
            double taxable = itemTotal * (1.0 - discountPercentage / 100.0);
            double taxAmt = Math.round((taxable * gstRate / 100.0) * 100.0) / 100.0;
            
            String hsn = item.getHsnCode();
            if (hsn != null && !hsn.trim().isEmpty()) {
                holder.itemTaxInfo.setText(String.format("HSN: %s | GST %.0f%%: ₹%.2f", hsn, gstRate, taxAmt));
            } else {
                holder.itemTaxInfo.setText(String.format("GST %.0f%%: ₹%.2f", gstRate, taxAmt));
            }
            holder.itemTaxInfo.setVisibility(View.VISIBLE);
        } else if (holder.itemTaxInfo != null) {
            holder.itemTaxInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemTaxInfo, itemQuantity, itemType, itemPrice;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.cart_item_name);
            itemTaxInfo = itemView.findViewById(R.id.cart_item_tax_info);
            itemQuantity = itemView.findViewById(R.id.cart_item_quantity);
            itemType = itemView.findViewById(R.id.cart_item_type);
            itemPrice = itemView.findViewById(R.id.cart_item_price);
        }
    }
}