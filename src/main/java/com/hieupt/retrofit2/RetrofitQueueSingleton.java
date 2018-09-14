package com.hieupt.retrofit2;

public final class RetrofitQueueSingleton {

    /**
     * @return singleton instance of {@link RetrofitQueue}
     */
    public static RetrofitQueue getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private RetrofitQueueSingleton() {
        //no instance
    }

    private static class InstanceHolder {

        private static final RetrofitQueue INSTANCE = new RetrofitQueue();
    }
}
