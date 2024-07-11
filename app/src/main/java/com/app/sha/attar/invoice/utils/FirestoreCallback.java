package com.app.sha.attar.invoice.utils;

import com.app.sha.attar.invoice.model.BillingInvoiceModel;
import com.app.sha.attar.invoice.model.ProductModel;

import java.util.List;

public interface FirestoreCallback<T> {
    void onCallback(T result);
}