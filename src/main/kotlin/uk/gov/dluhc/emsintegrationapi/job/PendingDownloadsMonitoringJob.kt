package uk.gov.dluhc.emsintegrationapi.job

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.dluhc.emsintegrationapi.service.PendingDownloadsMonitoringService

@Component
@ConditionalOnProperty("jobs.pending-downloads-monitoring.enabled", havingValue = "true")
class PendingDownloadsMonitoringJob(
    private val pendingDownloadsMonitoringService: PendingDownloadsMonitoringService
) {
    @Scheduled(cron = "\${jobs.pending-downloads-monitoring.cron}")
    @SchedulerLock(name = "\${jobs.pending-downloads-monitoring.name}")
    fun monitorPendingDownloads() {
        pendingDownloadsMonitoringService.monitorPendingDownloads()
    }
}
