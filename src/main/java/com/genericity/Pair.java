package com.genericity;

public class Pair<T> {
    private T first;
    private T second;

    public void setFirst(T t) {
        this.first = t;
    }

    public T getFirst() {
        return first;
    }

    public void setSecond(T second) {
        this.second = second;
    }

    public T getSecond() {
        return second;
    }
}
