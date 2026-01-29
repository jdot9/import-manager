package com.dotwavesoftware.importscheduler.worker;

import org.springframework.stereotype.Service;

@Service
public class SalesforceTaskWorker implements TaskWorker<SalesforcePayload> {

    @Override
    public void execute(SalesforcePayload payload) {
        // TODO: Implement Salesforce integration
    }
}
