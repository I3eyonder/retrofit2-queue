package com.hieupt.retrofit2;

import java.util.LinkedList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class RetrofitQueue {

    public static final int DEFAULT_MAX_REQUEST_ACTIVE = 1;

    private final FixedSizeArrayList<Request<?>> activeList;

    private final LinkedList<Request<?>> requestQueue;

    public RetrofitQueue() {
        this(DEFAULT_MAX_REQUEST_ACTIVE);
    }

    public RetrofitQueue(int maxActiveRequest) {
        this.requestQueue = new LinkedList<>();
        this.activeList = new FixedSizeArrayList<>(calculateMaxActiveRequest(maxActiveRequest));
    }

    /**
     * @return Current max active request number
     */
    public synchronized int getMaxActiveRequest() {
        return activeList.getMaxSize();
    }

    private int calculateMaxActiveRequest(int maxActiveRequest) {
        int calculated = DEFAULT_MAX_REQUEST_ACTIVE;
        if (maxActiveRequest >= DEFAULT_MAX_REQUEST_ACTIVE) {
            calculated = maxActiveRequest;
        }
        return calculated;
    }

    /**
     * Update max active request number and execute pending request if current active number lesser than max active request number
     *
     * @param maxActiveRequest Max active request number
     * @throws IllegalArgumentException If maxActiveRequest is lesser than 0
     */
    public synchronized void updateMaxActiveRequest(int maxActiveRequest) {
        activeList.setMaxSize(calculateMaxActiveRequest(maxActiveRequest));
        executeRemainAcceptableRequests();
    }

    private void executeRemainAcceptableRequests() {
        while (!requestQueue.isEmpty() && activeList.canAdd()) {
            tryToExecuteNextRequest();
        }
    }

    private void tryToExecuteNextRequest() {
        if (activeList.canAdd()) {
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

    /**
     * Add request to queue. Execute immediately if current active lesser max active
     *
     * @param request  request call
     * @param callback callback
     * @param <T>      Type of response data
     */
    public synchronized <T> void addRequest(Call<T> request, Callback<T> callback) {
        if (request != null) {
            Request<T> requestWrap = new Request<>(request, callback);
            requestQueue.add(requestWrap);
            tryToExecuteNextRequest();
        }
    }

    /**
     * Add request to front of queue. Execute immediately if current active lesser max active
     *
     * @param request  request call
     * @param callback callback
     * @param <T>      Type of response data
     */
    public synchronized <T> void addRequestToFrontQueue(Call<T> request, Callback<T> callback) {
        if (request != null) {
            Request<T> requestWrap = new Request<>(request, callback);
            requestQueue.addFirst(requestWrap);
            tryToExecuteNextRequest();
        }
    }

    /**
     * Execute request immediately
     *
     * @param request  request call
     * @param callback callback
     * @param <T>      Type of response data
     */
    public synchronized <T> void requestNow(Call<T> request, Callback<T> callback) {
        if (request != null) {
            request.enqueue(new CallbackWrapperDelegate<>(callback));
        }
    }

    /**
     * Remove {@code request} from pending queue
     *
     * @param request Request need to remove
     */
    public synchronized void removeRequest(Call<?> request) {
        if (request != null) {
            requestQueue.removeIf(requestWrap -> requestWrap != null && requestWrap.request == request);
        }
    }

    /**
     * Clear request queue. Executing request do not affect by this call.
     */
    public synchronized void clearQueue() {
        requestQueue.clear();
    }

    /**
     * Cancel {@code request} if it is activating
     *
     * @param request Request need to cancel
     */
    public synchronized void cancel(Call<?> request) {
        if (request != null) {
            Request<?> requestWrap = activeList.stream().filter(r -> r.request == request).findFirst().orElse(null);
            if (requestWrap != null) {
                requestWrap.cancel();
                activeList.remove(requestWrap);
            }
        }
    }

    /**
     * Cancel all activating requests.
     */
    public synchronized void cancel() {
        activeList.forEach(request -> {
            if (request != null) {
                request.cancel();
            }
        });
        activeList.clear();
    }

    /**
     * Cancel all activating requests and clear pending request queue also.
     */
    public synchronized void cancelAndClear() {
        cancel();
        clearQueue();
    }

    private final class Request<T> {

        private final Call<T> request;

        private final Callback<T> callback;

        private Request(Call<T> request, Callback<T> callback) {
            this.request = request;
            this.callback = callback;
        }

        private void execute() {
            activeList.add(this);
            requestQueue.remove(this);
            request.enqueue(new CallbackDecorator<>(this));
        }

        private void cancel() {
            request.cancel();
        }
    }

    private final class CallbackDecorator<T> extends CallbackWrapperDelegate<T> {

        private final Request<T> request;

        private CallbackDecorator(Request<T> request) {
            super(request.callback);
            this.request = request;
        }

        @Override
        public void onResponse(Call<T> call, Response<T> response, T responseData) {
            super.onResponse(call, response, responseData);
            performNextRequest();
        }

        @Override
        public void onFailure(Call<T> call, boolean isCanceled, Throwable t) {
            super.onFailure(call, isCanceled, t);
            performNextRequest();
        }

        private void performNextRequest() {
            activeList.remove(request);
            if (activeList.canAdd() && !requestQueue.isEmpty()) {
                Request<?> request = requestQueue.peek();
                if (request != null) {
                    request.execute();
                }
            }
        }
    }
}
