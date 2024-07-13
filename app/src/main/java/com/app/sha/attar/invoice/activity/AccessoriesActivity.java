package com.app.sha.attar.invoice.activity;

import static com.app.sha.attar.invoice.utils.SharedConstants.ACCESSORIES_KEY;
import static com.app.sha.attar.invoice.utils.SharedConstants.PRODUCT_KEY;
import static com.app.sha.attar.invoice.utils.SharedConstants.SHA_ATTAR;
import static java.lang.Boolean.TRUE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.sha.attar.invoice.R;
import com.app.sha.attar.invoice.adapter.AccessoriesViewAdapter;
import com.app.sha.attar.invoice.listener.ClickListener;
import com.app.sha.attar.invoice.model.AccessoriesModel;
import com.app.sha.attar.invoice.model.ProductModel;
import com.app.sha.attar.invoice.utils.DBUtil;
import com.app.sha.attar.invoice.utils.DatabaseConstants;
import com.app.sha.attar.invoice.utils.FirestoreCallback;
import com.app.sha.attar.invoice.utils.SharedPrefHelper;
import com.app.sha.attar.invoice.utils.SingleTon;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

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
    FirebaseFirestore db;

    private SharedPreferences sharedPreferences;
    private Gson gson;

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
        db = DBUtil.getInstance();

        sharedPreferences = context.getSharedPreferences(SHA_ATTAR, Context.MODE_PRIVATE);
        gson = new Gson();

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
        if (R.id.accessories_back == view.getId()) {
            finish();
        } else if (R.id.accessories_add_fab == view.getId()) {
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
            price.setText(String.valueOf(accessoriesModel.getPrice()));

            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "Loading .! ", Toast.LENGTH_LONG).show();

                    db.collection(DatabaseConstants.ACCESSORIES_COLLECTION).document(accessoriesModel.getDocumentId())
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Call the callback with null since the task was successful
                                    System.out.println("Accessories successfully deleted!");
                                    Toast.makeText(context, "Accessories successfully deleted!", Toast.LENGTH_LONG).show();
                                    setTotalAccessoriesItem();
                                    dialog.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error while deleting Accessories. Please try again.!", Toast.LENGTH_LONG).show();
                                    System.err.println("Error deleting Accessories: " + e);
                                    dialog.dismiss();
                                }
                            });
                }
            });
        } else {
            delete.setVisibility(View.INVISIBLE);
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

                    db.collection(DatabaseConstants.ACCESSORIES_COLLECTION)
                            .document(accessoriesModel.getDocumentId())
                            .set(accessoriesModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(context, "Update Accessories - " + accessoriesModel.getName() + " Successfully", Toast.LENGTH_LONG).show();
                                    System.out.println("Accessories Updated successfully.");
                                    setTotalAccessoriesItem();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error while updating accessories. Please try again", Toast.LENGTH_LONG).show();
                                    System.out.println("Error while Updating accessories." + e);
                                }
                            });
                } else {
                    AccessoriesModel accessoriesModel = new AccessoriesModel();
                    accessoriesModel.setName(name.getText().toString());
                    accessoriesModel.setPrice(Integer.valueOf(price.getText().toString()));
                    accessoriesModel.setId(getLatestProductID());
                    accessoriesModel.setDocumentId(SingleTon.generateAccessoriesDocument());

                    db.collection(DatabaseConstants.ACCESSORIES_COLLECTION)
                            .document(accessoriesModel.getDocumentId())
                            .set(accessoriesModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(context, "New Accessories - " + accessoriesModel.getName() + " Added", Toast.LENGTH_LONG).show();
                                    System.out.println("Accessories Added successfully.");
                                    setTotalAccessoriesItem();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Error while saving Accessories. Please try again", Toast.LENGTH_LONG).show();
                                    System.out.println("Error while saving Accessories." + e);
                                }
                            });

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

    private void callApiData() {

        itemList = sharedPrefHelper.getTotalAccessoriesList();
        System.out.println("Number of Accessories-- " + itemList.size());
        if (itemList.isEmpty()) {
            no_data_fl.setVisibility(View.VISIBLE);
            data_fl.setVisibility(View.GONE);
            Toast.makeText(context, "Accessories is empty. Please try again .! ", Toast.LENGTH_LONG).show();
            return;
        }else{
            data_fl.setVisibility(View.VISIBLE);
            no_data_fl.setVisibility(View.GONE);
        }
        adapter = new AccessoriesViewAdapter(context, itemList, listener);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    public void setTotalAccessoriesItem() {
        Toast.makeText(context, "Loading .! ", Toast.LENGTH_LONG).show();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        dbObj.getAllAccessories(new FirestoreCallback<List<AccessoriesModel>>() {
            @Override
            public void onCallback(List<AccessoriesModel> accessories) {
                System.out.println("Number of Accessories: " + accessories.size());
                String json = gson.toJson(accessories);
                editor.putString(ACCESSORIES_KEY, json);
                editor.apply();
                editor.commit();
                callApiData();
            }
        });
    }

}