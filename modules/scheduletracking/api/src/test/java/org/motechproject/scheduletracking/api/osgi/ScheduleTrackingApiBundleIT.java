package org.motechproject.scheduletracking.api.osgi;

import org.motechproject.scheduletracking.api.domain.Schedule;
import org.motechproject.scheduletracking.api.service.ScheduleTrackingService;
import org.motechproject.testing.osgi.BaseOsgiIT;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;

public class ScheduleTrackingApiBundleIT extends BaseOsgiIT {

    public void testScheduleTrackingService() {
        ScheduleTrackingService scheduleTrackingService = getService(ScheduleTrackingService.class);

        final String scheduleName = "ScheduleTrackingApiBundleIT-" + UUID.randomUUID();
        try {
            scheduleTrackingService.add("{name: " + scheduleName + "}");
            Schedule schedule = scheduleTrackingService.getScheduleByName(scheduleName);
            assertNotNull(schedule);
            assertEquals(scheduleName, schedule.getName());
        } finally {
            scheduleTrackingService.remove(scheduleName);
        }

    }

    @Override
    protected List<String> getImports() {
        return asList(
                "org.motechproject.scheduletracking.api.domain"
        );
    }
}
