package com.example.ssjb.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppExecutors {
    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();

    private AppExecutors() {
    }

    public static ExecutorService db() {
        return DB_EXECUTOR;
    }
}
