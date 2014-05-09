package org.motechproject.scheduletracking.events;

import org.joda.time.DateTime;
import org.junit.Test;
import org.motechproject.commons.date.model.Time;
import org.motechproject.event.MotechEvent;
import org.motechproject.scheduletracking.domain.Enrollment;
import org.motechproject.scheduletracking.domain.EnrollmentStatus;
import org.motechproject.scheduletracking.domain.Milestone;
import org.motechproject.scheduletracking.domain.MilestoneAlert;
import org.motechproject.scheduletracking.domain.MilestoneWindow;
import org.motechproject.scheduletracking.domain.Schedule;
import org.motechproject.scheduletracking.domain.WindowName;
import org.motechproject.scheduletracking.events.constants.EventDataKeys;
import org.motechproject.scheduletracking.events.constants.EventSubjects;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.motechproject.commons.date.util.DateUtil.now;
import static org.motechproject.scheduletracking.utility.PeriodUtil.weeks;

public class MilestoneEventTest {
    private static final String MILESTONE_DATA_KEY = "key";

    @Test
    public void shouldCreateMotechEvent() {
        String externalId = "externalId";
        String scheduleName = "scheduleName";
        MilestoneAlert milestoneAlert = MilestoneAlert.fromMilestone(new Milestone("M1", weeks(1), weeks(1), weeks(1), weeks(1)), now());
        String windowName = "windowName";
        Map<String, String> milestoneData = new HashMap<>(1);
        milestoneData.put(MILESTONE_DATA_KEY, "FooBar");

        MilestoneEvent milestoneEvent = new MilestoneEvent(externalId, scheduleName, milestoneAlert, windowName, now(), milestoneData);
        MotechEvent motechEvent = milestoneEvent.toMotechEvent();

        assertEventDetails(motechEvent, externalId, scheduleName, milestoneAlert, windowName, milestoneData);
    }

    @Test
    public void shouldCreateMotechEventFromEnrollment() {
        String externalId = "externalId";
        String scheduleName = "scheduleName";
        Map<String, String> milestoneData = new HashMap<>(1);
        milestoneData.put(MILESTONE_DATA_KEY, "FooBar");

        Milestone milestone = new Milestone("M1", weeks(1), weeks(1), weeks(1), weeks(1));
        milestone.setData(milestoneData);

        Schedule schedule = new Schedule("scheduleName");
        schedule.addMilestones(milestone);

        DateTime referenceDateTime = now();
        DateTime enrollmentDateTime = now();
        MilestoneAlert milestoneAlert = MilestoneAlert.fromMilestone(milestone, referenceDateTime);

        MilestoneWindow milestoneWindow = new MilestoneWindow(WindowName.due, weeks(1));
        MilestoneEvent milestoneEvent = new MilestoneEvent(new Enrollment().setExternalId(externalId).setSchedule(schedule).setCurrentMilestoneName(milestone.getName()).setStartOfSchedule(referenceDateTime).setEnrolledOn(enrollmentDateTime).setPreferredAlertTime(new Time(8, 10)).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null), milestoneAlert, milestoneWindow);
        MotechEvent motechEvent = milestoneEvent.toMotechEvent();

        assertEventDetails(motechEvent, externalId, scheduleName, milestoneAlert, milestoneWindow.getName().toString(), milestone.getData());
    }

    private void assertEventDetails(MotechEvent motechEvent, String externalId, String scheduleName, MilestoneAlert milestoneAlert, String windowName, Map<String, String> milestoneData) {
        assertEquals(EventSubjects.MILESTONE_ALERT, motechEvent.getSubject());

        Map<String, Object> parameters = motechEvent.getParameters();

        assertEquals(externalId, parameters.get(EventDataKeys.EXTERNAL_ID));
        assertEquals(scheduleName, parameters.get(EventDataKeys.SCHEDULE_NAME));
        assertEquals(milestoneAlert.getMilestoneName(), parameters.get(EventDataKeys.MILESTONE_NAME));
        assertEquals(milestoneAlert.getEarliestDateTime(), parameters.get(EventDataKeys.EARLIEST_DATE_TIME));
        assertEquals(milestoneAlert.getDueDateTime(), parameters.get(EventDataKeys.DUE_DATE_TIME));
        assertEquals(milestoneAlert.getLateDateTime(), parameters.get(EventDataKeys.LATE_DATE_TIME));
        assertEquals(milestoneAlert.getDefaultmentDateTime(), parameters.get(EventDataKeys.DEFAULTMENT_DATE_TIME));
        assertEquals(windowName, parameters.get(EventDataKeys.WINDOW_NAME));

        Map<String, String> eventData = (Map<String, String>) parameters.get(EventDataKeys.MILESTONE_DATA);

        assertEquals(milestoneData.size(), eventData.size());
        assertTrue(eventData.containsKey(MILESTONE_DATA_KEY));
        assertEquals(milestoneData.get(MILESTONE_DATA_KEY), eventData.get(MILESTONE_DATA_KEY));
    }
}
