package com.dotwavesoftware.importscheduler.worker;

public interface TaskWorker<T> {
    void execute(T payload);
}