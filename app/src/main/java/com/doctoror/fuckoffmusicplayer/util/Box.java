package com.doctoror.fuckoffmusicplayer.util;

/**
 * Holds a value
 */
public class Box<T> {

    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(final T value) {
        this.value = value;
    }
}