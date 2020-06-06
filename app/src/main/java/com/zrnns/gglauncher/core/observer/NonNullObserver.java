package com.zrnns.gglauncher.core.observer;

import androidx.annotation.NonNull;

public interface NonNullObserver<T> {
    void onChanged(@NonNull T t);
}
