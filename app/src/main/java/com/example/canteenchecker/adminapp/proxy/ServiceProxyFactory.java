package com.example.canteenchecker.adminapp.proxy;

public class ServiceProxyFactory {
    public static ServiceProxy createProxy() {
        return new ServiceProxyImpl();
    }
}
