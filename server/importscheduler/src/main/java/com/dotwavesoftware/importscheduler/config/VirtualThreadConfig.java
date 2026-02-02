package com.dotwavesoftware.importscheduler.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

/*
AsyncConfigurer tells Spring explicitly to use virtualThreadExectuor
for @Async operations. This resolves the ambiguity between the multiple
executor beans. Only use @EnableAsync when you have one executor type bean.
*/

@Configuration
public class VirtualThreadConfig implements AsyncConfigurer {

    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public Executor getAsyncExecutor() {
        return virtualThreadExecutor();
    }
}
