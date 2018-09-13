package com.hieupt.retrofit2;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Response;

public final class RetrofitRequestSync {

    public interface SyncRequestCallback<T> {

        default void onResponse(Call<T> request, T responseData) {

        }

        default void onFailure(Call<T> request, Throwable throwable) {

        }
    }

    public static <T> T executeRequest(Call<T> request) {
        return executeRequest(request, null);
    }

    public static <T> T executeRequest(Call<T> request, SyncRequestCallback<T> callback) {
        if (request != null) {
            try {
                Response<T> response = request.execute();
                T responseData = response.body();
                if (callback != null) {
                    if (response.isSuccessful()) {
                        callback.onResponse(request, responseData);
                    } else {
                        callback.onFailure(request, new HttpException(response));
                    }
                }
                return responseData;
            } catch (IOException e) {
                if (callback != null) {
                    callback.onFailure(request, e);
                }
            }
        }
        return null;
    }

    private RetrofitRequestSync() {
        //no instance
    }
}
