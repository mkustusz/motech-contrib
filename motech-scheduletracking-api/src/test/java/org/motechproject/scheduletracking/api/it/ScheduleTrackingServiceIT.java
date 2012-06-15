package org.motechproject.scheduletracking.api.it;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.model.Time;
import org.motechproject.scheduletracking.api.domain.Enrollment;
import org.motechproject.scheduletracking.api.repository.AllEnrollments;
import org.motechproject.scheduletracking.api.service.EnrollmentRecord;
import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
import org.motechproject.scheduletracking.api.service.EnrollmentsQuery;
import org.motechproject.scheduletracking.api.service.ScheduleTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.motechproject.util.DateUtil.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:testApplicationSchedulerTrackingAPI.xml")
public class ScheduleTrackingServiceIT {

    @Autowired
    private ScheduleTrackingService scheduleTrackingService;
    @Autowired
    private AllEnrollments allEnrollments;

    private Enrollment activeEnrollment;

    @After
    public void tearDown() {
        if (activeEnrollment != null)
            allEnrollments.remove(activeEnrollment);
    }

    @Test
    public void shouldUpdateEnrollmentIfAnActiveEnrollmentAlreadyExists() {
        activeEnrollment = allEnrollments.getActiveEnrollment("externalId", "IPTI Schedule");
        assertNull("Active enrollment present", activeEnrollment);

        Time originalPreferredAlertTime = new Time(8, 10);
        DateTime now = now();
        String enrollmentId = scheduleTrackingService.enroll(new EnrollmentRequest("externalId", "IPTI Schedule", originalPreferredAlertTime, now.toLocalDate(), null, null, null, null, null));
        assertNotNull("EnrollmentId is null", enrollmentId);

        activeEnrollment = allEnrollments.get(enrollmentId);
        assertNotNull("No active enrollment present", activeEnrollment);
        assertEquals(originalPreferredAlertTime, activeEnrollment.getPreferredAlertTime());
        assertEquals(newDateTime(now.toLocalDate(), new Time(0, 0)), activeEnrollment.getStartOfSchedule());

        Time updatedPreferredAlertTime = new Time(2, 5);
        DateTime updatedReferenceDate = now.minusDays(1);
        String updatedEnrollmentId = scheduleTrackingService.enroll(new EnrollmentRequest("externalId", "IPTI Schedule", updatedPreferredAlertTime, updatedReferenceDate.toLocalDate(), null, null, null, null, null));
        assertEquals(enrollmentId, updatedEnrollmentId);

        activeEnrollment = allEnrollments.get(updatedEnrollmentId);
        assertNotNull("No active enrollment present", activeEnrollment);
        assertEquals(updatedPreferredAlertTime, activeEnrollment.getPreferredAlertTime());
        assertEquals(newDateTime(updatedReferenceDate.toLocalDate(), new Time(0, 0)), activeEnrollment.getStartOfSchedule());
    }

    @Test
    public void fulfillMilestoneShouldBeIdempotent() {
        scheduleTrackingService.enroll(new EnrollmentRequest("entity_1", "IPTI Schedule", null, newDate(2012, 2, 10), null, newDate(2012, 2, 10), null, null, null));
        scheduleTrackingService.fulfillCurrentMilestone("entity_1", "IPTI Schedule", newDate(2012, 2, 20), new Time(8, 20));
        scheduleTrackingService.fulfillCurrentMilestone("entity_1", "IPTI Schedule", newDate(2012, 2, 20), new Time(8, 20));

        List<EnrollmentRecord> enrollment = scheduleTrackingService.search(new EnrollmentsQuery().havingExternalId("entity_1").havingSchedule("IPTI Schedule"));
        assertEquals("IPTI 2", enrollment.get(0).getCurrentMilestoneName());
    }
}
