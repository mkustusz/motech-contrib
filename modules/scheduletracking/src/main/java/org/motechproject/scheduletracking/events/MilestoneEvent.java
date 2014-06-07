package org.motechproject.scheduletracking.events;

import org.joda.time.DateTime;
import org.motechproject.event.MotechEvent;
import org.motechproject.scheduletracking.domain.Enrollment;
import org.motechproject.scheduletracking.domain.MilestoneAlert;
import org.motechproject.scheduletracking.domain.MilestoneWindow;
import org.motechproject.scheduletracking.events.constants.EventDataKeys;
import org.motechproject.scheduletracking.events.constants.EventSubjects;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the event which will be raised as per the alert configuration
 */
public class MilestoneEvent {
    private String windowName;
    private MilestoneAlert milestoneAlert;
    private String scheduleName;
    private String externalId;
    private DateTime referenceDateTime;
    private Map<String, String> milestoneData;

    /**
     * Creates a MilestoneEvent
     * @param externalId
     * @param scheduleName
     * @param milestoneAlert
     * @param windowName
     * @param referenceDateTime
     * @param milestoneData
     */
    public MilestoneEvent(String externalId, String scheduleName, MilestoneAlert milestoneAlert, String windowName, DateTime referenceDateTime, Map<String, String> milestoneData) {
        this.scheduleName = scheduleName;
        this.milestoneAlert = milestoneAlert;
        this.windowName = windowName;
        this.externalId = externalId;
        this.referenceDateTime = referenceDateTime;
        this.milestoneData = milestoneData;
    }

    /**
     * Creates a MilestoneEvent from an Enrollment by passing in an MotechEvent
     * @param motechEvent
     */
    public MilestoneEvent(MotechEvent motechEvent) {
        this.scheduleName = (String) motechEvent.getParameters().get(EventDataKeys.SCHEDULE_NAME);
        this.milestoneAlert = new MilestoneAlert((String) motechEvent.getParameters().get(EventDataKeys.MILESTONE_NAME),
                (DateTime) motechEvent.getParameters().get(EventDataKeys.EARLIEST_DATE_TIME),
                (DateTime) motechEvent.getParameters().get(EventDataKeys.DUE_DATE_TIME), 
                (DateTime) motechEvent.getParameters().get(EventDataKeys.LATE_DATE_TIME),
                (DateTime) motechEvent.getParameters().get(EventDataKeys.DEFAULTMENT_DATE_TIME));
        this.windowName = (String) motechEvent.getParameters().get(EventDataKeys.WINDOW_NAME);
        this.externalId = (String) motechEvent.getParameters().get(EventDataKeys.EXTERNAL_ID);
        this.referenceDateTime = (DateTime) motechEvent.getParameters().get(EventDataKeys.REFERENCE_DATE);
        this.milestoneData = (Map<String, String>) motechEvent.getParameters().get(EventDataKeys.MILESTONE_DATA);
    }

    /**
     * Creates a MilestoneEvent from an Enrollment
     * @param enrollment
     * @param milestoneAlert
     * @param milestoneWindow
     */
    public MilestoneEvent(Enrollment enrollment, MilestoneAlert milestoneAlert, MilestoneWindow milestoneWindow) {
        this.externalId = enrollment.getExternalId();
        this.scheduleName = enrollment.getScheduleName();
        this.milestoneAlert = milestoneAlert;
        this.windowName = milestoneWindow.getName().toString();
        this.referenceDateTime = enrollment.getStartOfSchedule();
        this.milestoneData = enrollment.getSchedule().getMilestone(enrollment.getCurrentMilestoneName()).getData();
    }

    /**
     * Creates an MotechEvent from a MilestoneEvent
     * @return MotechEvent
     */
    public MotechEvent toMotechEvent() {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(EventDataKeys.WINDOW_NAME, windowName);
        parameters.put(EventDataKeys.MILESTONE_NAME, milestoneAlert.getMilestoneName());
        parameters.put(EventDataKeys.EARLIEST_DATE_TIME, milestoneAlert.getEarliestDateTime());
        parameters.put(EventDataKeys.DUE_DATE_TIME, milestoneAlert.getDueDateTime());
        parameters.put(EventDataKeys.LATE_DATE_TIME, milestoneAlert.getLateDateTime());
        parameters.put(EventDataKeys.DEFAULTMENT_DATE_TIME, milestoneAlert.getDefaultmentDateTime());
        parameters.put(EventDataKeys.SCHEDULE_NAME, scheduleName);
        parameters.put(EventDataKeys.EXTERNAL_ID, externalId);
        parameters.put(EventDataKeys.REFERENCE_DATE, referenceDateTime);
        parameters.put(EventDataKeys.MILESTONE_DATA, milestoneData);
        return new MotechEvent(EventSubjects.MILESTONE_ALERT, parameters);
    }

    /**
     * Returns the Window name of the MilestoneEvent
     * @return String
     */
    public String getWindowName() {
        return windowName;
    }

    /**
     * Returns the MilestoneAlert of the MilestoneEvent
     * @return MilestoneAlert
     */
    public MilestoneAlert getMilestoneAlert() {
        return milestoneAlert;
    }

    /**
     * Returns the Schedule Name of the MilestoneEvent
     * @return String
     */
    public String getScheduleName() {
        return scheduleName;
    }

    /**
     * Returns the External Id of the MilestoneEvent
     * @return String
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * Returns the ReferenceDateTime of the MilestoneEvent
     * @return DateTime
     */
    public DateTime getReferenceDateTime() {
        return referenceDateTime;
    }

    /**
     * Returns the Milestone Data of the MilestoneEvent
     * @return Map
     */
    public Map<String, String> getMilestoneData() {
        return milestoneData;
    }
}
