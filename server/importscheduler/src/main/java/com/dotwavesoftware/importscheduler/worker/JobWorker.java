package com.dotwavesoftware.importscheduler.worker;

import org.springframework.stereotype.Service;

@Service
public class JobWorker {

    private final HubSpotTaskWorker hubspotTask;
    private final SalesforceTaskWorker salesforceTask;

    public JobWorker(
                     HubSpotTaskWorker hubspotTask,
                     SalesforceTaskWorker salesforceTask) {
        this.hubspotTask = hubspotTask;
        this.salesforceTask = salesforceTask;
    }

    public void execute(Job job) {
        switch (job.getType()) {
            case hubspot ->
                hubspotTask.execute(job.parsePayload(HubSpotPayload.class));
            case SALESFORCE ->
                salesforceTask.execute(job.parsePayload(SalesforcePayload.class));
        }
    }
}
