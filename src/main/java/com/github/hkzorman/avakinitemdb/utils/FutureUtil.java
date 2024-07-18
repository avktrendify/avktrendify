package com.github.hkzorman.avakinitemdb.utils;

import java.util.concurrent.Future;

public class FutureUtil {
    public static <T> T getFutureSafe(Future<T> future) {
        try {
            return future.get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
