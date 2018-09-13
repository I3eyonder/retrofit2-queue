package com.hieupt.retrofit2;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallbackWrapper<T> implements Callback<T> {

    private final Callback<T> delegateCallback;

    public CallbackWrapper(Callback<T> callback) {
        this.delegateCallback = callback;
    }

    public Callback<T> getDelegateCallback() {
        return delegateCallback;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (delegateCallback != null) {
            delegateCallback.onResponse(call, response);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        if (delegateCallback != null) {
            delegateCallback.onFailure(call, t);
        }
    }
}
