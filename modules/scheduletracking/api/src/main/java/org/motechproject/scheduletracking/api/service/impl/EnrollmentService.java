package org.motechproject.scheduletracking.api.service.impl;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.motechproject.commons.date.model.Time;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.scheduletracking.api.domain.Enrollment;
import org.motechproject.scheduletracking.api.domain.EnrollmentStatus;
import org.motechproject.scheduletracking.api.domain.Milestone;
import org.motechproject.scheduletracking.api.domain.MilestoneWindow;
import org.motechproject.scheduletracking.api.domain.Schedule;
import org.motechproject.scheduletracking.api.domain.WindowName;
import org.motechproject.scheduletracking.api.domain.exception.NoMoreMilestonesToFulfillException;
import org.motechproject.scheduletracking.api.events.EnrolledUserEvent;
import org.motechproject.scheduletracking.api.events.UnenrolledUserEvent;
import org.motechproject.scheduletracking.api.repository.AllEnrollments;
import org.motechproject.scheduletracking.api.repository.AllSchedules;
import org.motechproject.scheduletracking.api.service.MilestoneAlerts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.motechproject.commons.date.util.StringUtil.isNullOrEmpty;
import static org.motechproject.scheduletracking.api.domain.EnrollmentStatus.COMPLETED;
import static org.motechproject.scheduletracking.api.domain.EnrollmentStatus.UNENROLLED;

@Component
public class EnrollmentService {

    private AllSchedules allSchedules;
    private AllEnrollments allEnrollments;
    private EnrollmentAlertService enrollmentAlertService;
    private EnrollmentDefaultmentService enrollmentDefaultmentService;
    private EventRelay eventRelay;

    @Autowired
    public EnrollmentService(AllSchedules allSchedules, AllEnrollments allEnrollments, EnrollmentAlertService enrollmentAlertService, EnrollmentDefaultmentService enrollmentDefaultmentService, EventRelay eventRelay) {
        this.allSchedules = allSchedules;
        this.allEnrollments = allEnrollments;
        this.enrollmentAlertService = enrollmentAlertService;
        this.enrollmentDefaultmentService = enrollmentDefaultmentService;
        this.eventRelay = eventRelay;
    }

    public String enroll(String externalId, String scheduleName, String startingMilestoneName, DateTime referenceDateTime, DateTime enrollmentDateTime, Time preferredAlertTime, Map<String, String> metadata) {
        Schedule schedule = allSchedules.getByName(scheduleName);
        Enrollment enrollment = new Enrollment().setExternalId(externalId).setSchedule(schedule).setCurrentMilestoneName(startingMilestoneName).setStartOfSchedule(referenceDateTime).setEnrolledOn(enrollmentDateTime).setPreferredAlertTime(preferredAlertTime).setStatus(EnrollmentStatus.ACTIVE).setMetadata(metadata);

        if (schedule.hasExpiredSince(enrollment.getCurrentMilestoneStartDate(), startingMilestoneName)) {
            enrollment.setStatus(EnrollmentStatus.DEFAULTED);
        }

        Enrollment activeEnrollment = allEnrollments.getActiveEnrollment(externalId, scheduleName);
        if (activeEnrollment == null) {
            allEnrollments.add(enrollment);
            eventRelay.sendEventMessage(new EnrolledUserEvent(enrollment.getExternalId(),
                    enrollment.getScheduleName(), enrollment.getPreferredAlertTime(), referenceDateTime,
                    enrollmentDateTime, enrollment.getCurrentMilestoneName()).toMotechEvent());
        } else {
            unscheduleJobs(activeEnrollment);
            enrollment = activeEnrollment.copyFrom(enrollment);
            allEnrollments.update(enrollment);
        }

        scheduleJobs(enrollment);
        return enrollment.getId();
    }

    public void fulfillCurrentMilestone(Enrollment enrollment, DateTime fulfillmentDateTime) {
        Schedule schedule = allSchedules.getByName(enrollment.getScheduleName());
        if (isNullOrEmpty(enrollment.getCurrentMilestoneName())) {
            throw new NoMoreMilestonesToFulfillException();
        }

        unscheduleJobs(enrollment);

        enrollment.fulfillCurrentMilestone(fulfillmentDateTime);
        String nextMilestoneName = schedule.getNextMilestoneName(enrollment.getCurrentMilestoneName());
        enrollment.setCurrentMilestoneName(nextMilestoneName);
        if (nextMilestoneName == null) {
            enrollment.setStatus(COMPLETED);
        } else {
            scheduleJobs(enrollment);
        }

        allEnrollments.update(enrollment);
    }

    public void unenroll(Enrollment enrollment) {
        unscheduleJobs(enrollment);
        enrollment.setStatus(UNENROLLED);
        allEnrollments.update(enrollment);
        eventRelay.sendEventMessage(new UnenrolledUserEvent(enrollment.getExternalId(), enrollment.getScheduleName()).toMotechEvent());
    }

    public WindowName getCurrentWindowAsOf(Enrollment enrollment, DateTime asOf) {
        Schedule schedule = allSchedules.getByName(enrollment.getScheduleName());
        DateTime milestoneStart = enrollment.getCurrentMilestoneStartDate();
        Milestone milestone = schedule.getMilestone(enrollment.getCurrentMilestoneName());
        for (MilestoneWindow window : milestone.getMilestoneWindows()) {
            Period windowStart = milestone.getWindowStart(window.getName());
            Period windowEnd = milestone.getWindowEnd(window.getName());
            DateTime windowStartDateTime = milestoneStart.plus(windowStart);
            DateTime windowEndDateTime = milestoneStart.plus(windowEnd);
            if (inRange(asOf, windowStartDateTime, windowEndDateTime)) {
                return window.getName();
            }
        }
        return null;
    }

    public DateTime getEndOfWindowForCurrentMilestone(Enrollment enrollment, WindowName windowName) {
        Schedule schedule = allSchedules.getByName(enrollment.getScheduleName());
        DateTime currentMilestoneStartDate = enrollment.getCurrentMilestoneStartDate();
        Milestone currentMilestone = schedule.getMilestone(enrollment.getCurrentMilestoneName());
        return currentMilestoneStartDate.plus(currentMilestone.getWindowEnd(windowName));
    }

    private boolean inRange(DateTime asOf, DateTime windowStartDateTime, DateTime windowEndDateTime) {
        return (asOf.equals(windowStartDateTime) || asOf.isAfter(windowStartDateTime)) && asOf.isBefore(windowEndDateTime);
    }

    private void scheduleJobs(Enrollment enrollment) {
        enrollmentAlertService.scheduleAlertsForCurrentMilestone(enrollment);
        enrollmentDefaultmentService.scheduleJobToCaptureDefaultment(enrollment);
    }

    private void unscheduleJobs(Enrollment enrollment) {
        enrollmentAlertService.unscheduleAllAlerts(enrollment);
        enrollmentDefaultmentService.unscheduleDefaultmentCaptureJob(enrollment);
    }

    public MilestoneAlerts getAlertTimings(String externalId, String scheduleName, String milestoneName, DateTime referenceDateTime, DateTime enrollmentDateTime, Time preferredAlertTime) {
        Schedule schedule = allSchedules.getByName(scheduleName);
        return enrollmentAlertService.getAlertTimings(new Enrollment().setExternalId(externalId).setSchedule(schedule).setCurrentMilestoneName(milestoneName).setStartOfSchedule(referenceDateTime).setEnrolledOn(enrollmentDateTime).setPreferredAlertTime(preferredAlertTime).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null));
    }
}
