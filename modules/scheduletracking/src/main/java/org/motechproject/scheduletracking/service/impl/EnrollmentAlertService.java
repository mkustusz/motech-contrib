package org.motechproject.scheduletracking.service.impl;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.scheduler.contract.RepeatingSchedulableJob;
import org.motechproject.scheduler.contract.RunOnceSchedulableJob;
import org.motechproject.scheduletracking.domain.Alert;
import org.motechproject.scheduletracking.domain.AlertWindow;
import org.motechproject.scheduletracking.domain.Enrollment;
import org.motechproject.scheduletracking.domain.Milestone;
import org.motechproject.scheduletracking.domain.MilestoneAlert;
import org.motechproject.scheduletracking.domain.MilestoneWindow;
import org.motechproject.scheduletracking.domain.Schedule;
import org.motechproject.scheduletracking.events.MilestoneEvent;
import org.motechproject.scheduletracking.events.constants.EventSubjects;
import org.motechproject.scheduletracking.service.MilestoneAlerts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.motechproject.commons.date.util.DateUtil.now;

@Component
public class EnrollmentAlertService {

    public static final int MILLIS_IN_A_SEC = 1000;
    private MotechSchedulerService schedulerService;

    private EventRelay eventRelay;

    @Autowired
    public EnrollmentAlertService(MotechSchedulerService schedulerService, EventRelay eventRelay) {
        this.schedulerService = schedulerService;
        this.eventRelay = eventRelay;
    }

    public void scheduleAlertsForCurrentMilestone(Enrollment enrollment) {
        Schedule schedule = enrollment.getSchedule();
        Milestone currentMilestone = schedule.getMilestone(enrollment.getCurrentMilestoneName());
        if (currentMilestone == null) {
            return;
        }

        DateTime currentMilestoneStartDate = enrollment.getCurrentMilestoneStartDate();
        for (MilestoneWindow milestoneWindow : currentMilestone.getMilestoneWindows()) {
            if (currentMilestone.windowElapsed(milestoneWindow.getName(), currentMilestoneStartDate)) {
                continue;
            }

            MilestoneAlert milestoneAlert = MilestoneAlert.fromMilestone(currentMilestone, currentMilestoneStartDate);
            for (Alert alert : milestoneWindow.getAlerts()) {
                scheduleAlertJob(alert, enrollment, currentMilestone, milestoneWindow, milestoneAlert);
            }
        }
    }

    public MilestoneAlerts getAlertTimings(Enrollment enrollment) {
        Schedule schedule = enrollment.getSchedule();
        Milestone currentMilestone = schedule.getMilestone(enrollment.getCurrentMilestoneName());
        MilestoneAlerts milestoneAlerts = new MilestoneAlerts();
        if (currentMilestone == null) {
            return milestoneAlerts;
        }

        for (MilestoneWindow milestoneWindow : currentMilestone.getMilestoneWindows()) {
            List<DateTime> alertTimingsForWindow = new ArrayList<DateTime>();
            for (Alert alert : milestoneWindow.getAlerts()) {
                AlertWindow alertWindow = createAlertWindowFor(alert, enrollment, currentMilestone, milestoneWindow);
                alertTimingsForWindow.addAll(alertWindow.allPossibleAlerts());
            }
            milestoneAlerts.getAlertTimings().put(milestoneWindow.getName().toString(), alertTimingsForWindow);
        }
        return milestoneAlerts;
    }

    private void scheduleAlertJob(Alert alert, Enrollment enrollment, Milestone currentMilestone, MilestoneWindow milestoneWindow, MilestoneAlert milestoneAlert) {
        MotechEvent event = new MilestoneEvent(enrollment, milestoneAlert, milestoneWindow).toMotechEvent();
        event.getParameters().put(MotechSchedulerService.JOB_ID_KEY, String.format("%s.%d", enrollment.getId(), alert.getIndex()));
        long repeatIntervalInMillis = (long) alert.getInterval().toStandardSeconds().getSeconds() * MILLIS_IN_A_SEC;

        AlertWindow alertWindow = createAlertWindowFor(alert, enrollment, currentMilestone, milestoneWindow);
        int numberOfAlertsToSchedule = alertWindow.numberOfAlertsToSchedule();

        if (numberOfAlertsToSchedule == 1) {
            // if there is only one alert to schedule, a run once job will be scheduled.
            DateTime alertsStartTime = alertWindow.scheduledAlertStartDate();

            if (alertsStartTime.isBefore(now())) {
                eventRelay.sendEventMessage(event);
            } else {
                RunOnceSchedulableJob job = new RunOnceSchedulableJob(event, alertsStartTime.toDate());

                schedulerService.safeScheduleRunOnceJob(job);
            }
        } else if (numberOfAlertsToSchedule > 0) {
            // We take one away from the number to schedule since one is
            // always fired from a RepeatingSchedulableJob
            int numberOfAlertsToFire = numberOfAlertsToSchedule - 1;
            DateTime alertsStartTime = alertWindow.scheduledAlertStartDate();

            // If the first alert should have gone out already, go ahead and raise
            // the event for it and decrease the numberOfAlertsToFire since we've
            // already fired one
            if (alertsStartTime.isBefore(now())) {
                alertsStartTime = alertsStartTime.plus(repeatIntervalInMillis);
                numberOfAlertsToFire = numberOfAlertsToFire - 1;
                eventRelay.sendEventMessage(event);
            }

            // Schedule the repeating job with the scheduler.
            RepeatingSchedulableJob job = new RepeatingSchedulableJob()
                    .setMotechEvent(event)
                    .setStartTime(alertsStartTime.toDate())
                    .setEndTime(null)
                    .setRepeatCount(numberOfAlertsToFire)
                    .setRepeatIntervalInMilliSeconds(repeatIntervalInMillis)
                    .setIgnorePastFiresAtStart(false);

            schedulerService.safeScheduleRepeatingJob(job);
        }
    }

    private AlertWindow createAlertWindowFor(Alert alert, Enrollment enrollment, Milestone currentMilestone, MilestoneWindow milestoneWindow) {
        Period windowStart = currentMilestone.getWindowStart(milestoneWindow.getName());
        Period windowEnd = currentMilestone.getWindowEnd(milestoneWindow.getName());

        DateTime currentMilestoneStartDate = enrollment.getCurrentMilestoneStartDate();

        DateTime windowStartDateTime = currentMilestoneStartDate.plus(windowStart);
        DateTime windowEndDateTime = currentMilestoneStartDate.plus(windowEnd);

        return new AlertWindow(windowStartDateTime, windowEndDateTime, enrollment.getEnrolledOn(), enrollment.getPreferredAlertTime(), alert);
    }

    public void unscheduleAllAlerts(Enrollment enrollment) {
        schedulerService.safeUnscheduleAllJobs(String.format("%s-%s", EventSubjects.MILESTONE_ALERT, enrollment.getId()));
    }
}
