package com.interface21.jdbc.core;

@FunctionalInterface
public interface ServiceCallback <T>{

    T execute();
}
