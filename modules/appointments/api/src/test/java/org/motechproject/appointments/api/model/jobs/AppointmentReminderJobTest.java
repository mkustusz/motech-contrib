package org.motechproject.appointments.api.model.jobs;

import org.junit.Test;
import org.motechproject.appointments.api.EventKeys;
import org.motechproject.appointments.api.model.Appointment;
import org.motechproject.appointments.api.model.Reminder;
import org.motechproject.appointments.api.model.Visit;
import org.motechproject.scheduler.service.MotechSchedulerService;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;

public class AppointmentReminderJobTest {

    private Visit visit;
    private AppointmentReminderJob appointmentReminderJob;
    private int reminderCount;

    public AppointmentReminderJobTest() {
        Reminder reminder = new Reminder();
        visit = new Visit().appointment(new Appointment().reminders(Arrays.asList(reminder))).name("visitName");
        reminderCount = 0;
        appointmentReminderJob = new AppointmentReminderJob("externalId", AppointmentReminderJob.getJobIdUsing("externalId", visit.name(), reminderCount), reminder, visit.name());
    }

    @Test
    public void shouldCreateMotechEventWithTheCorrectSubject() {
        assertEquals("org.motechproject.appointments.api.Appointment.Reminder", appointmentReminderJob.getMotechEvent().getSubject());
    }

    @Test
    public void shouldAddVisitNameToParameters() {
        assertEquals(visit.name(), appointmentReminderJob.getMotechEvent().getParameters().get(EventKeys.VISIT_NAME));
    }

    @Test
    public void shouldAddCombinationOfVisitNameAndExternalId_AsJobId() {
        assertEquals("externalId" + visit.name() + reminderCount, appointmentReminderJob.getMotechEvent().getParameters().get(MotechSchedulerService.JOB_ID_KEY));
    }
}