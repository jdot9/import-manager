package com.dotwavesoftware.importscheduler.worker;

import org.springframework.stereotype.Service;

import com.dotwavesoftware.importscheduler.service.Five9Service;

@Service
public class HubSpotTaskWorker implements TaskWorker<HubSpotPayload> {

    private final Five9Service five9Service;

    public HubSpotTaskWorker(Five9Service five9Service) {
        this.five9Service = five9Service;
    }

    @Override
    public void execute(HubSpotPayload payload) {
        five9Service.importContactsToDialingList(
            payload.five9ConnectionId(),
            payload.hubspotConnectionId(),
            payload.hubspotListId(),
            payload.importId()
        ).subscribe(
            success -> {
                if (success) {
                    System.out.println("HubSpot to Five9 import completed successfully for importId: " + payload.importId());
                } else {
                    System.err.println("HubSpot to Five9 import failed for importId: " + payload.importId());
                }
            },
            error -> System.err.println("HubSpot to Five9 import error: " + error.getMessage())
        );
    }
}
