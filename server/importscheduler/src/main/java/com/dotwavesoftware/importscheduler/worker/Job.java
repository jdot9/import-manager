package com.dotwavesoftware.importscheduler.worker;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Job {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final JobType type;
    private final Object payload;

    public Job(JobType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public JobType getType() {
        return type;
    }

    public <T> T parsePayload(Class<T> clazz) {
        return objectMapper.convertValue(payload, clazz);
    }

    public Object parsePayload() {
        return payload;
    }
}
