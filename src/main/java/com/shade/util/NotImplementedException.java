package com.shade.util;

public class NotImplementedException extends RuntimeException {
    public NotImplementedException() {
        super("���� " + getCallingMethod() + " δʵ��");
    }

    @NotNull
    private static String getCallingMethod() {
        // getStackTrace() + getCallingMethod() + NotImplementedException()
        final StackTraceElement element = Thread.currentThread().getStackTrace()[3];
        return element.getClassName() + '#' + element.getMethodName();
    }
}
