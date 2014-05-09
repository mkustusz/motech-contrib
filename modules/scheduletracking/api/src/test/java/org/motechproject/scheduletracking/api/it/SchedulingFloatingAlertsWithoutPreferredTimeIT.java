package org.motechproject.scheduletracking.api.it;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.commons.date.model.Time;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventListener;
import org.motechproject.event.listener.EventListenerRegistryService;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.scheduler.factory.MotechSchedulerFactoryBean;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.scheduletracking.api.events.constants.EventSubjects;
import org.motechproject.scheduletracking.api.repository.AllEnrollments;
import org.motechproject.scheduletracking.api.repository.AllSchedules;
import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
import org.motechproject.scheduletracking.api.service.ScheduleTrackingService;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.motechproject.commons.date.util.DateUtil.newDate;
import static org.motechproject.commons.date.util.DateUtil.newDateTime;
import static org.motechproject.commons.date.util.DateUtil.now;
import static org.motechproject.testing.utils.TimeFaker.fakeNow;
import static org.motechproject.testing.utils.TimeFaker.stopFakingTime;
import static org.quartz.TriggerKey.triggerKey;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/motech/*.xml")
public class SchedulingFloatingAlertsWithoutPreferredTimeIT {

    @Autowired
    private ScheduleTrackingService scheduleTrackingService;

    @Autowired
    MotechSchedulerService schedulerService;

    @Autowired
    private MotechSchedulerFactoryBean motechSchedulerFactoryBean;

    @Autowired
    private AllSchedules allSchedules;

    @Autowired
    private AllEnrollments allEnrollments;

    @Autowired
    EventListenerRegistryService eventListenerRegistry;

    Scheduler scheduler;

    @Before
    public void setup() {
        scheduler = motechSchedulerFactoryBean.getQuartzScheduler();
    }

    @After
    public void teardown() {
        schedulerService.unscheduleAllJobs("org.motechproject.scheduletracking");
        allEnrollments.removeAll();
        allSchedules.removeAll();
    }

    @Test
    public void shouldScheduleFloatingAlertsAtReferenceTime() throws SchedulerException, URISyntaxException, IOException {
        addSchedule("/schedulingIT/schedule_with_floating_alerts.json");

        String enrollmentId = scheduleTrackingService.enroll(new EnrollmentRequest().setExternalId("abcde").setScheduleName("schedule_with_floating_alerts").setPreferredAlertTime(null).setReferenceDate(newDate(2050, 5, 10)).setReferenceTime(new Time(8, 20)).setEnrollmentDate(newDate(2050, 5, 10)).setEnrollmentTime(new Time(8, 20)).setStartingMilestoneName("milestone1").setMetadata(null));

        List<DateTime> fireTimes = getFireTimes(format("org.motechproject.scheduletracking.api.milestone.alert-%s.0-repeat", enrollmentId)) ;
        assertEquals(asList(
                newDateTime(2050, 5, 17, 8, 20, 0),
                newDateTime(2050, 5, 18, 8, 20, 0),
                newDateTime(2050, 5, 19, 8, 20, 0),
                newDateTime(2050, 5, 20, 8, 20, 0)),
                fireTimes);
    }

    @Test
    public void shouldFloatTheAlertsForDelayedEnrollmentTriggeringAlertsAtReferenceTimeWhichIsBeforeNow() throws SchedulerException, URISyntaxException, IOException {
        addSchedule("/schedulingIT/schedule_with_floating_alerts.json");

        try {
            AlertListener alertListener = new AlertListener();
            eventListenerRegistry.registerListener(alertListener, EventSubjects.MILESTONE_ALERT);
            fakeNow(newDateTime(2050, 5, 19, 10, 0, 0));

            String enrollmentId = scheduleTrackingService.enroll(new EnrollmentRequest().setExternalId("abcde").setScheduleName("schedule_with_floating_alerts").setPreferredAlertTime(null).setReferenceDate(newDate(2050, 5, 10)).setReferenceTime(new Time(9, 0)).setEnrollmentDate(newDate(2050, 5, 18)).setEnrollmentTime(new Time(9, 0)).setStartingMilestoneName("milestone1").setMetadata(null));

            assertEquals(newDateTime(2050, 5, 19, 10, 0, 0), alertListener.getTriggerTime());

            List<DateTime> fireTimes = getFireTimes(format("org.motechproject.scheduletracking.api.milestone.alert-%s.0-repeat", enrollmentId)) ;
            assertEquals(asList(
                    newDateTime(2050, 5, 20, 9, 0, 0),
                    newDateTime(2050, 5, 21, 9, 0, 0),
                    newDateTime(2050, 5, 22, 9, 0, 0)),
                    fireTimes);
        } finally {
            stopFakingTime();
            eventListenerRegistry.clearListenersForBean("alertsTestListener");
        }
    }

    @Test
    public void shouldFloatTheAlertsForDelayedEnrollmentTriggeringAlertsAtReferenceTimeWhichIsAfterNow() throws SchedulerException, URISyntaxException, IOException {
        addSchedule("/schedulingIT/schedule_with_floating_alerts.json");

        try {
            fakeNow(newDateTime(2050, 5, 19, 8, 0, 0));
            String enrollmentId = scheduleTrackingService.enroll(new EnrollmentRequest().setExternalId("abcde").setScheduleName("schedule_with_floating_alerts").setPreferredAlertTime(null).setReferenceDate(newDate(2050, 5, 10)).setReferenceTime(new Time(9, 0)).setEnrollmentDate(newDate(2050, 5, 18)).setEnrollmentTime(new Time(9, 0)).setStartingMilestoneName("milestone1").setMetadata(null));

            List<DateTime> fireTimes = getFireTimes(format("org.motechproject.scheduletracking.api.milestone.alert-%s.0-repeat", enrollmentId)) ;
            assertEquals(asList(
                    newDateTime(2050, 5, 19, 9, 0, 0),
                    newDateTime(2050, 5, 20, 9, 0, 0),
                    newDateTime(2050, 5, 21, 9, 0, 0),
                    newDateTime(2050, 5, 22, 9, 0, 0)),
                    fireTimes);
        } finally {
            stopFakingTime();
        }
    }

    @Test
    public void shouldFloatTheAlertsForDelayedEnrollmentInTheTimeLeftTriggeringThemAtReferenceTime() throws SchedulerException, URISyntaxException, IOException {
        addSchedule("/schedulingIT/schedule_with_floating_alerts.json");

        try {
            AlertListener alertListener = new AlertListener();
            eventListenerRegistry.registerListener(alertListener, EventSubjects.MILESTONE_ALERT);
            fakeNow(newDateTime(2050, 5, 22, 10, 0, 0));
            String enrollmentId = scheduleTrackingService.enroll(new EnrollmentRequest().setExternalId("abcde").setScheduleName("schedule_with_floating_alerts").setPreferredAlertTime(null).setReferenceDate(newDate(2050, 5, 10)).setReferenceTime(new Time(9, 0)).setEnrollmentDate(newDate(2050, 5, 19)).setEnrollmentTime(new Time(9, 0)).setStartingMilestoneName("milestone1").setMetadata(null));

            assertEquals(newDateTime(2050, 5, 22, 10, 0, 0), alertListener.getTriggerTime());

            List<DateTime> fireTimes = getFireTimes(format("org.motechproject.scheduletracking.api.milestone.alert-%s.0-repeat", enrollmentId)) ;
            assertEquals(asList(
                    newDateTime(2050, 5, 23, 9, 0, 0)),
                    fireTimes);
        } finally {
            stopFakingTime();
            eventListenerRegistry.clearListenersForBean("alertsTestListener");
        }
    }

    @Test
    public void shouldScheduleSecondMilestoneAlertsAtLastMilestoneFulfilmentTime() throws IOException, URISyntaxException, SchedulerException {
        addSchedule("/schedulingIT/schedule_with_floating_alerts.json");

        try {
            AlertListener alertListener = new AlertListener();
            eventListenerRegistry.registerListener(alertListener, EventSubjects.MILESTONE_ALERT);
            fakeNow(newDateTime(2050, 5, 22, 11, 0, 0));

            String enrollmentId = scheduleTrackingService.enroll(new EnrollmentRequest().setExternalId("abcde").setScheduleName("schedule_with_floating_alerts").setPreferredAlertTime(null).setReferenceDate(newDate(2050, 5, 10)).setReferenceTime(new Time(9, 0)).setEnrollmentDate(newDate(2050, 5, 10)).setEnrollmentTime(new Time(9, 0)).setStartingMilestoneName("milestone1").setMetadata(null));
            scheduleTrackingService.fulfillCurrentMilestone("abcde", "schedule_with_floating_alerts", newDate(2050, 5, 21), new Time(10, 0));

            assertEquals(newDateTime(2050, 5, 22, 11, 0, 0), alertListener.getTriggerTime());

            List<DateTime> fireTimes = getFireTimes(format("org.motechproject.scheduletracking.api.milestone.alert-%s.1-repeat", enrollmentId)) ;
            assertEquals(asList(
                    newDateTime(2050, 5, 23, 10, 0, 0),
                    newDateTime(2050, 5, 24, 10, 0, 0)),
                    fireTimes);
        } finally {
            stopFakingTime();
            eventListenerRegistry.clearListenersForBean("alertsTestListener");
        }
    }

    class AlertListener implements EventListener {

        private DateTime eventRaisedAt;
        private MotechEvent event;

        @Override
        public String getIdentifier() {
            return "alertsTestListener";
        }

        @MotechListener(subjects = EventSubjects.MILESTONE_ALERT)
        public void handle(MotechEvent motechEvent) {
            event = motechEvent;
            eventRaisedAt = now();
        }

        public DateTime getTriggerTime() {
            DateTime pollUntil = new DateTime(System.currentTimeMillis()).plusSeconds(10);
            while (eventRaisedAt == null)
                if (new DateTime(System.currentTimeMillis()).isAfter(pollUntil))
                    break;
            return eventRaisedAt;
        }
    }

    private void addSchedule(String filename) throws URISyntaxException, IOException {
        File file = new File(getClass().getResource(filename).toURI());
        String scheduleJson = FileUtils.readFileToString(file);
        scheduleTrackingService.add(scheduleJson);
    }

    private List<DateTime> getFireTimes(String key) throws SchedulerException {
        Trigger trigger = scheduler.getTrigger(triggerKey(key, "default"));
        List<DateTime> fireTimes = new ArrayList<>();
        Date nextFireTime = trigger.getNextFireTime();
        while (nextFireTime != null) {
            fireTimes.add(newDateTime(nextFireTime));
            nextFireTime = trigger.getFireTimeAfter(nextFireTime);
        }
        return fireTimes;
    }
}
