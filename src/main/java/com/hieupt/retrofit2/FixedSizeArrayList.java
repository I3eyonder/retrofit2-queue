package com.hieupt.retrofit2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class FixedSizeArrayList<E> extends ArrayList<E> {

    private int maxSize;

    public FixedSizeArrayList(int maxSize) {
        setMaxSize(maxSize);
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be greater than 0");
        }
        this.maxSize = maxSize;
    }

    public boolean canAdd() {
        return size() < maxSize;
    }

    @Override
    public boolean add(E e) {
        if (canAdd()) {
            return super.add(e);
        }
        return false;
    }

    @Override
    public void add(int index, E element) {
        if (canAdd()) {
            super.add(index, element);
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        int remain = maxSize - size();
        if (remain > 0) {
            List<? extends E> subList = c.stream().limit(remain).collect(Collectors.toList());
            return super.addAll(subList);
        }
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        int remain = maxSize - size();
        if (remain > 0) {
            List<? extends E> subList = c.stream().limit(remain).collect(Collectors.toList());
            return super.addAll(index, subList);
        }
        return false;
    }
}
