package org.motechproject.scheduletracking.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.commons.date.model.Time;
import org.motechproject.event.MotechEvent;
import org.motechproject.scheduletracking.domain.Enrollment;
import org.motechproject.scheduletracking.domain.Schedule;
import org.motechproject.scheduletracking.events.DefaultmentCaptureEvent;
import org.motechproject.scheduletracking.repository.AllEnrollments;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.motechproject.scheduletracking.utility.DateTimeUtil.weeksAgo;

public class EndOfMilestoneListenerTest {
    private EndOfMilestoneListener endOfMilestoneListener;

    @Mock
    private AllEnrollments allEnrollments;

    @Before
    public void setup() {
        initMocks(this);
        endOfMilestoneListener = new EndOfMilestoneListener(allEnrollments);
    }

    @Test
    public void shouldDefaultEnrollmentAtTheCurrentMilestoneIfNotFulfilled() {
        Schedule schedule = new Schedule("some_schedule");
        Enrollment enrollment = new Enrollment().setExternalId("entity_1").setSchedule(schedule).setCurrentMilestoneName("first_milestone").setStartOfSchedule(weeksAgo(4)).setEnrolledOn(weeksAgo(4)).setPreferredAlertTime(new Time(8, 10)).setStatus(null).setMetadata(null);
        enrollment.setId("enrollment_1");
        when(allEnrollments.get("enrollment_1")).thenReturn(enrollment);

        MotechEvent event = new DefaultmentCaptureEvent("enrollment_1", "job_id", "externalId").toMotechEvent();
        endOfMilestoneListener.handle(event);
        verify(allEnrollments).update(enrollment);
    }
}
