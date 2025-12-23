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

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartViewHolder> {

    private Context context;
    private List<BillingItemModel> cartItems;

    public CartItemAdapter(Context context, List<BillingItemModel> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
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
        holder.itemPrice.setText("₹" + item.getSellingItemPrice());
        
        if ("PRODUCT".equals(item.getType())) {
            holder.itemQuantity.setText("Qty: " + item.getUnits() + "ML");
            holder.itemType.setText(item.getProductCategory().substring(0,1).toUpperCase());
            holder.itemType.setVisibility(View.VISIBLE);
        } else {
            holder.itemQuantity.setText("Qty: 1");
            holder.itemType.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemQuantity, itemType, itemPrice;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.cart_item_name);
            itemQuantity = itemView.findViewById(R.id.cart_item_quantity);
            itemType = itemView.findViewById(R.id.cart_item_type);
            itemPrice = itemView.findViewById(R.id.cart_item_price);
        }
    }
}