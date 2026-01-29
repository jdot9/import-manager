package com.dotwavesoftware.importscheduler.worker;

public record HubSpotPayload(
    int five9ConnectionId,
    int hubspotConnectionId,
    String hubspotListId,
    int importId
) {}
