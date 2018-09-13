package com.hieupt.retrofit2.queue;

import com.hieupt.retrofit2.CallbackWrapper;
import com.hieupt.utils.Counter;

import java.io.IOException;
import java.util.LinkedList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public final class RetrofitQueue {

    public interface SyncRequestCallback<T> {

        default void onResponse(Call<T> request, T responseData) {

        }

        default void onFailure(Call<T> request, Throwable throwable) {

        }
    }

    public static final int DEFAULT_MAX_REQUEST_ACTIVE = 1;

    private final Counter activeCounter;

    private final LinkedList<Request<?>> requestQueue;

    public RetrofitQueue() {
        this(DEFAULT_MAX_REQUEST_ACTIVE);
    }

    public RetrofitQueue(int maxActiveRequest) {
        this.requestQueue = new LinkedList<>();
        this.activeCounter = new Counter(calculateMaxActiveRequest(maxActiveRequest));
    }

    public int getMaxActiveRequest() {
        return activeCounter.getMax();
    }

    private int calculateMaxActiveRequest(int maxActiveRequest) {
        int calculated = DEFAULT_MAX_REQUEST_ACTIVE;
        if (maxActiveRequest >= DEFAULT_MAX_REQUEST_ACTIVE) {
            calculated = maxActiveRequest;
        }
        return calculated;
    }

    public void updateMaxActiveRequest(int maxActiveRequest) {
        activeCounter.setMax(calculateMaxActiveRequest(maxActiveRequest));
        executeRemainAcceptableRequests();
    }

    private void executeRemainAcceptableRequests() {
        while (activeCounter.canIncrease()) {
            tryToExecuteNextRequest();
        }
    }

    private void tryToExecuteNextRequest() {
        if (activeCounter.canIncrease()) {
            Request<?> request = getFirstPendingRequest();
            if (request != null) {
                request.execute();
            }
        }
    }

    private Request<?> getFirstPendingRequest() {
        if (!requestQueue.isEmpty()) {
            return requestQueue.peek();
        }
        return null;
    }

    public <T> void addRequest(Call<T> request, Callback<T> callback) {
        if (request != null) {
            Request<T> requestWrap = new Request<>(request, callback);
            requestQueue.add(requestWrap);
            tryToExecuteNextRequest();
        }
    }

    public <T> void addRequestToFrontQueue(Call<T> request, Callback<T> callback) {
        if (request != null) {
            Request<T> requestWrap = new Request<>(request, callback);
            requestQueue.addFirst(requestWrap);
            tryToExecuteNextRequest();
        }
    }

    public <T> void requestNow(Call<T> request, Callback<T> callback) {
        if (request != null) {
            request.enqueue(new CallbackWrapper<>(callback));
        }
    }

    public <T> T executeRequestSync(Call<T> request) {
        return executeRequestSync(request, null);
    }

    public <T> T executeRequestSync(Call<T> request, SyncRequestCallback<T> callback) {
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

    private final class Request<T> {

        private final Call<T> request;

        private final Callback<T> callback;

        private Request(Call<T> request, Callback<T> callback) {
            this.request = request;
            this.callback = callback;
        }

        private void execute() {
            activeCounter.increase();
            requestQueue.remove(this);
            request.enqueue(new CallbackDecorator<>(callback));
        }
    }

    private final class CallbackDecorator<T> extends CallbackWrapper<T> {

        private CallbackDecorator(Callback<T> callback) {
            super(callback);
        }

        @Override
        public void onResponse(Call<T> call, Response<T> response) {
            super.onResponse(call, response);
            performNextRequest();
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            super.onFailure(call, t);
            performNextRequest();
        }

        private void performNextRequest() {
            activeCounter.decrease();
            if (!requestQueue.isEmpty()) {
                Request<?> request = requestQueue.peek();
                request.execute();
            }
        }
    }
}
