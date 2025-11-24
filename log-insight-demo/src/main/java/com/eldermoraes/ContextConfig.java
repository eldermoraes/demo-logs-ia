package com.eldermoraes;

import java.lang.ScopedValue;

public class ContextConfig {
    // JEP 506: Scoped Values. Immutable, efficient, and safe for Virtual Threads.
    // Replaces ThreadLocal for passing the correlation ID.
    public static final ScopedValue<String> CORRELATION_ID = ScopedValue.newInstance();
}