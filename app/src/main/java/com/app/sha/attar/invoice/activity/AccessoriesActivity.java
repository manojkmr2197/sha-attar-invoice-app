package com.app.sha.attar.invoice.activity;

import static java.lang.Boolean.TRUE;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.adapter.AccessoriesViewAdapter;
import com.app.sha.attar.invoice.adapter.ProductViewAdapter;
import com.app.sha.attar.invoice.listener.ClickListener;
import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.app.sha.attar.invoice.utils.SharedPrefHelper;
import com.app.sha.attar.invoice.utils.SingleTon;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AccessoriesActivity extends AppCompatActivity implements View.OnClickListener {

    FrameLayout data_fl, no_data_fl;
    Context context;
    Activity activity;

    List<AccessoriesModel> itemList = new ArrayList<>();

    RecyclerView recyclerView;
    ClickListener listener;

    AccessoriesViewAdapter adapter;

    DBUtil dbObj;
    SharedPrefHelper sharedPrefHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessories);

        context = AccessoriesActivity.this;
        activity = AccessoriesActivity.this;

        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.white));
        }

        no_data_fl = (FrameLayout) findViewById(R.id.accessories_no_data_ll);
        data_fl = (FrameLayout) findViewById(R.id.accessories_data_ll);

        recyclerView = (RecyclerView) findViewById(R.id.accessories_recyclerView);

        FloatingActionButton add_fab = (FloatingActionButton) findViewById(R.id.accessories_add_fab);
        add_fab.setOnClickListener(this);

        TextView back = (TextView) findViewById(R.id.accessories_back);
        back.setOnClickListener(this);

        dbObj = new DBUtil();
        sharedPrefHelper = new SharedPrefHelper(context);
        listener = new ClickListener() {
            @Override
            public void click(int index) {
                createDialogBox(context, itemList.get(index));
            }
        };
        checkInternet();

    }

    @Override
    public void onClick(View view) {
        if (R.id.product_back == view.getId()) {
            finish();
        } else if (R.id.product_add_fab == view.getId()) {
            createDialogBox(context, null);
        }
    }

    private void checkInternet() {
        if (SingleTon.isNetworkConnected(activity)) {
            no_data_fl.setVisibility(View.GONE);
            data_fl.setVisibility(View.VISIBLE);
            callApiData();
        } else {
            no_data_fl.setVisibility(View.VISIBLE);
            data_fl.setVisibility(View.GONE);
            Toast.makeText(context, "No Internet connection. Please try again .! ", Toast.LENGTH_LONG).show();
        }

    }

    private void createDialogBox(Context context, AccessoriesModel accessoriesModel) {

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(R.layout.dialog_accessories_create);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        TextInputEditText name = (TextInputEditText) dialog.findViewById(R.id.accessories_add_name);
        TextInputEditText price = (TextInputEditText) dialog.findViewById(R.id.accessories_add_price);

        Button submit = (Button) dialog.findViewById(R.id.accessories_add_submit);
        TextView close = (TextView) dialog.findViewById(R.id.accessories_add_close);
        TextView delete = (TextView) dialog.findViewById(R.id.accessories_add_delete);
        if (accessoriesModel != null) {
            name.setText(accessoriesModel.getName());
            price.setText(accessoriesModel.getPrice());

            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "Loading .! ", Toast.LENGTH_LONG).show();
                    dbObj.deleteAccessories(accessoriesModel.getDocumentId(), new FirestoreCallback<Void>() {
                        @Override
                        public void onCallback(Void result) {
                            Toast.makeText(AccessoriesActivity.this, "Accessories deleted (" + accessoriesModel.getName() + ") ..!", Toast.LENGTH_SHORT).show();
                            callReloadData();
                            dialog.dismiss();
                        }
                    });
                }
            });
        } else {
            delete.setVisibility(View.GONE);
        }
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtils.isEmpty(name.getText().toString())) {
                    Toast.makeText(AccessoriesActivity.this, "Please enter Product name ..!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (StringUtils.isEmpty(price.getText().toString())) {
                    Toast.makeText(AccessoriesActivity.this, "Please enter Product Price ..!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(context, "Loading .! ", Toast.LENGTH_LONG).show();
                if (accessoriesModel != null) {
                    accessoriesModel.setName(name.getText().toString());
                    accessoriesModel.setPrice(Integer.valueOf(price.getText().toString()));

                    Boolean isUpdateSuccessfully = dbObj.updateAccessories(accessoriesModel.getDocumentId(), accessoriesModel);
                    if (isUpdateSuccessfully == TRUE) {
                        Toast.makeText(AccessoriesActivity.this, "Update Product ID - " + accessoriesModel.getId(), Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(AccessoriesActivity.this, "Error while updating new product product - " + name.getText(), Toast.LENGTH_LONG).show();
                    }
                    callReloadData();
                } else {
                    AccessoriesModel accessoriesModel = new AccessoriesModel();
                    accessoriesModel.setName(name.getText().toString());
                    accessoriesModel.setPrice(Integer.valueOf(price.getText().toString()));
                    accessoriesModel.setId(getLatestProductID());

                    Boolean isSavedSuccessfully = dbObj.addAccessories(accessoriesModel);
                    if (isSavedSuccessfully == TRUE) {
                        Toast.makeText(AccessoriesActivity.this, "New Accessories - " + name.getText() + " Added", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(AccessoriesActivity.this, "Error while adding new Accessories product - " + name.getText(), Toast.LENGTH_LONG).show();
                    }
                    callReloadData();
                }

                dialog.dismiss();
            }
        });
        dialog.show();

    }

    private Integer getLatestProductID() {
        List<AccessoriesModel> accessories = sharedPrefHelper.getTotalAccessoriesList();

        if (accessories.isEmpty()) {
            return 1;
        } else {
            Optional<AccessoriesModel> maxProductModelrOptional = accessories.stream()
                    .max(Comparator.comparingInt(AccessoriesModel::getId));

            return maxProductModelrOptional.map(accessoriesModel -> accessoriesModel.getId() + 1).orElse(1);
        }
    }

    private void callReloadData() {
        Toast.makeText(context, "Loading .! ", Toast.LENGTH_LONG).show();
        sharedPrefHelper.setTotalAccessoriesItem();
        callApiData();
    }

    private void callApiData() {

        List<AccessoriesModel> accessories = sharedPrefHelper.getTotalAccessoriesList();
        System.out.println("Number of products: " + itemList.size());
        if (accessories.isEmpty()) {
            Toast.makeText(context, "Accessories is empty. Please try again .! ", Toast.LENGTH_LONG).show();
        }
        itemList.clear();
        itemList.addAll(accessories);
        adapter = new AccessoriesViewAdapter(context, itemList, listener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

}