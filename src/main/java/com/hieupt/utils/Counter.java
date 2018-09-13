package com.hieupt.utils;

public final class Counter {

    private static final int MIN = 0;

    private volatile int current;

    private volatile int max;

    public Counter(int max) {
        setMax(max);
    }

    public synchronized int getCurrent() {
        return current;
    }

    public synchronized int getMax() {
        return max;
    }

    public synchronized void setMax(int max) {
        if (max <= MIN) {
            throw new IllegalArgumentException("max must greater than 0");
        }
        this.max = max;
    }

    public synchronized boolean canIncrease() {
        return current < max;
    }

    public synchronized boolean canDecrease() {
        return current > MIN;
    }

    public synchronized boolean increase() {
        if (canIncrease()) {
            current++;
            return true;
        }
        return false;
    }

    public synchronized boolean decrease() {
        if (canDecrease()) {
            current--;
            return true;
        }
        return false;
    }
}
