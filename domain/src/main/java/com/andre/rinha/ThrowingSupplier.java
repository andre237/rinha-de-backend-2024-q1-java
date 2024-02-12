package com.andre.rinha;

@FunctionalInterface
public interface ThrowingSupplier<T> {

    T get() throws Exception;

}
