package com.hieupt.retrofit2;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class CallbackWrapper<T> implements Callback<T> {

    @Override
    public final void onResponse(Call<T> call, Response<T> response) {
        onResponse(call, response, response.body());
    }

    @Override
    public final void onFailure(Call<T> call, Throwable t) {
        onFailure(call, call.isCanceled(), t);
    }

    public abstract void onResponse(Call<T> call, Response<T> response, T responseData);

    public abstract void onFailure(Call<T> call, boolean isCanceled, Throwable t);
}
