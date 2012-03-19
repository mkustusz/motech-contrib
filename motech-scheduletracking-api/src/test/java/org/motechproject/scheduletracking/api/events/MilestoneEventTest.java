package org.motechproject.scheduletracking.api.events;

import org.joda.time.DateTime;
import org.junit.Test;
import org.motechproject.model.MotechEvent;
import org.motechproject.model.Time;
import org.motechproject.scheduletracking.api.domain.*;
import org.motechproject.scheduletracking.api.events.constants.EventDataKeys;
import org.motechproject.scheduletracking.api.events.constants.EventSubjects;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.motechproject.scheduletracking.api.utility.PeriodFactory.weeks;
import static org.motechproject.util.DateUtil.now;

public class MilestoneEventTest {
    @Test
    public void shouldCreateMotechEvent() {
        String externalId = "externalId";
        String scheduleName = "scheduleName";
        MilestoneAlert milestoneAlert =  MilestoneAlert.fromMilestone(new Milestone("M1", weeks(1), weeks(1), weeks(1), weeks(1)), now());
        String windowName = "windowName";

        MilestoneEvent milestoneEvent = new MilestoneEvent(externalId, scheduleName, milestoneAlert, windowName, now());
        MotechEvent motechEvent = milestoneEvent.toMotechEvent();

        assertEquals(EventSubjects.MILESTONE_ALERT, motechEvent.getSubject());
        Map<String, Object> parameters = motechEvent.getParameters();
        assertEquals(externalId, parameters.get(EventDataKeys.EXTERNAL_ID));
        assertEquals(scheduleName, parameters.get(EventDataKeys.SCHEDULE_NAME));
        assertEquals(milestoneAlert, parameters.get(EventDataKeys.MILESTONE_NAME));
        assertEquals(windowName, parameters.get(EventDataKeys.WINDOW_NAME));
    }

    @Test
    public void shouldCreateMotechEventFromEnrollment() {
        String externalId = "externalId";
        String scheduleName = "scheduleName";
        Schedule schedule = new Schedule("scheduleName");
        Milestone milestone = new Milestone("M1", weeks(1), weeks(1), weeks(1), weeks(1));
        DateTime referenceDateTime = now();
        DateTime enrollmentDateTime = now();
        MilestoneAlert milestoneAlert =  MilestoneAlert.fromMilestone(milestone, referenceDateTime);

        MilestoneWindow milestoneWindow = new MilestoneWindow(WindowName.due, weeks(1));
        MilestoneEvent milestoneEvent = new  MilestoneEvent(new Enrollment(externalId, schedule, milestone.getName(), referenceDateTime, enrollmentDateTime, new Time(8, 10), EnrollmentStatus.ACTIVE), milestoneAlert, milestoneWindow);
        MotechEvent motechEvent = milestoneEvent.toMotechEvent();

        assertEquals(EventSubjects.MILESTONE_ALERT, motechEvent.getSubject());
        Map<String, Object> parameters = motechEvent.getParameters();
        assertEquals(externalId, parameters.get(EventDataKeys.EXTERNAL_ID));
        assertEquals(scheduleName, parameters.get(EventDataKeys.SCHEDULE_NAME));
        assertEquals(milestoneAlert, parameters.get(EventDataKeys.MILESTONE_NAME));
        assertEquals(milestoneWindow.getName().toString(), parameters.get(EventDataKeys.WINDOW_NAME));
    }
}
