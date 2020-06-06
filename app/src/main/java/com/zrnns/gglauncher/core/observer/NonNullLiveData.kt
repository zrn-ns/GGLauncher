package com.zrnns.gglauncher.core.observer

open class NonNullLiveData<T>(initialValue: T) : LateInitLiveData<T>() {

    init {
        value = initialValue
    }

}