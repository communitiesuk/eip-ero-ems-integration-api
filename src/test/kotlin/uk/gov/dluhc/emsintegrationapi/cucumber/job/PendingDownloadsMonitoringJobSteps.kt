package uk.gov.dluhc.emsintegrationapi.cucumber.rest

import io.cucumber.java8.En
import uk.gov.dluhc.emsintegrationapi.job.PendingDownloadsMonitoringJob

class PendingDownloadsMonitoringJobSteps(
    pendingDownloadsMonitoringJob: PendingDownloadsMonitoringJob
) : En {
    init {
        When("the pending downloads monitoring job runs") {
            pendingDownloadsMonitoringJob.monitorPendingDownloads()
        }
    }
}
