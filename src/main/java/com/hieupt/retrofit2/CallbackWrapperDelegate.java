package com.hieupt.retrofit2;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class CallbackWrapperDelegate<T> extends CallbackWrapper<T> {

    private final Callback<T> delegate;

    CallbackWrapperDelegate(Callback<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response, T responseData) {
        if (delegate != null) {
            delegate.onResponse(call, response);
        }
    }

    @Override
    public void onFailure(Call<T> call, boolean isCanceled, Throwable t) {
        if (delegate != null) {
            delegate.onFailure(call, t);
        }
    }
}
