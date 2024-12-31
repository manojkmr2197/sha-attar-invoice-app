package com.app.sha.attar.invoice.listener;

import com.app.sha.attar.invoice.model.TimeResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface TimeApi {
    @GET("/api/timezone/Etc/UTC")
    Call<TimeResponse> getTime();
}



