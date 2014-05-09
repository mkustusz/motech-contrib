package org.motechproject.scheduletracking.api.service.impl;

import org.joda.time.DateTime;
import org.motechproject.event.MotechEvent;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.scheduler.contract.RunOnceSchedulableJob;
import org.motechproject.scheduletracking.api.domain.Enrollment;
import org.motechproject.scheduletracking.api.domain.Milestone;
import org.motechproject.scheduletracking.api.domain.Schedule;
import org.motechproject.scheduletracking.api.events.DefaultmentCaptureEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.motechproject.commons.date.util.DateUtil.now;
import static org.motechproject.scheduletracking.api.events.constants.EventSubjects.DEFAULTMENT_CAPTURE;

@Component
public class EnrollmentDefaultmentService {
    private MotechSchedulerService schedulerService;

    @Autowired
    public EnrollmentDefaultmentService(MotechSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    public void scheduleJobToCaptureDefaultment(Enrollment enrollment) {
        Schedule schedule = enrollment.getSchedule();
        Milestone currentMilestone = schedule.getMilestone(enrollment.getCurrentMilestoneName());
        if (currentMilestone == null) {
            return;
        }

        DateTime currentMilestoneStartDate = enrollment.getCurrentMilestoneStartDate();
        DateTime milestoneEndDateTime = currentMilestoneStartDate.plus(currentMilestone.getMaximumDuration());

        if (milestoneEndDateTime.isBefore(now())) {
            return;
        }

        MotechEvent event = new DefaultmentCaptureEvent(enrollment.getId(), enrollment.getId(), enrollment.getExternalId()).toMotechEvent();
        schedulerService.safeScheduleRunOnceJob(new RunOnceSchedulableJob(event, milestoneEndDateTime.toDate()));
    }

    public void unscheduleDefaultmentCaptureJob(Enrollment enrollment) {
        schedulerService.safeUnscheduleAllJobs(String.format("%s-%s", DEFAULTMENT_CAPTURE, enrollment.getId()));
    }
}
