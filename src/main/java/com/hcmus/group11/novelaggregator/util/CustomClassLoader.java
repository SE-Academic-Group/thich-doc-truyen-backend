package com.hcmus.group11.novelaggregator.util;

public class CustomClassLoader extends ClassLoader {
    private byte[] classBytes;

    public CustomClassLoader(byte[] classBytes) {
        this.classBytes = classBytes;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return defineClass(name, classBytes, 0, classBytes.length);
    }
}
