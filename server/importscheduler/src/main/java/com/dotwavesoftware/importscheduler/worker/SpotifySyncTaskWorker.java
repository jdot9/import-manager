package com.dotwavesoftware.importscheduler.worker;

import org.springframework.stereotype.Service;

@Service
public class SpotifySyncTaskWorker implements TaskWorker<SpotifyPayload> {

    @Override
    public void execute(SpotifyPayload payload) {
        // API calls, DB writes, etc.
    }
}
