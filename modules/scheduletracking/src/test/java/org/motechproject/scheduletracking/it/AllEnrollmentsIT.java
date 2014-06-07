package org.motechproject.scheduletracking.it;

import ch.lambdaj.Lambda;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.commons.date.model.Time;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.scheduletracking.domain.Enrollment;
import org.motechproject.scheduletracking.domain.EnrollmentStatus;
import org.motechproject.scheduletracking.domain.Schedule;
import org.motechproject.scheduletracking.domain.ScheduleFactory;
import org.motechproject.scheduletracking.domain.json.ScheduleRecord;
import org.motechproject.scheduletracking.repository.AllEnrollments;
import org.motechproject.scheduletracking.repository.AllSchedules;
import org.motechproject.scheduletracking.repository.TrackedSchedulesJsonReader;
import org.motechproject.scheduletracking.repository.TrackedSchedulesJsonReaderImpl;
import org.motechproject.scheduletracking.service.EnrollmentRequest;
import org.motechproject.scheduletracking.service.ScheduleTrackingService;
import org.motechproject.scheduletracking.service.impl.EnrollmentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Locale;

import static ch.lambdaj.Lambda.on;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.motechproject.commons.date.util.DateUtil.newDateTime;
import static org.motechproject.commons.date.util.DateUtil.now;
import static org.motechproject.commons.date.util.DateUtil.today;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/motech/*.xml")
public class AllEnrollmentsIT {

    @Autowired
    private AllEnrollments allEnrollments;
    @Autowired
    private EnrollmentServiceImpl enrollmentService;

    @Autowired
    private AllSchedules allSchedules;
    @Autowired
    private ScheduleTrackingService scheduleTrackingService;
    @Autowired
    private ScheduleFactory scheduleFactory;

    @Before
    public void setUp() {
        TrackedSchedulesJsonReader schedulesJsonReader = new TrackedSchedulesJsonReaderImpl();
        for (ScheduleRecord scheduleRecord : schedulesJsonReader.getAllSchedules("/schedules")) {
            Schedule schedule = scheduleFactory.build(scheduleRecord, Locale.ENGLISH);
            allSchedules.add(schedule);
        }

        allEnrollments.removeAll();
    }

    @After
    public void tearDown() {
        allEnrollments.removeAll();
        allSchedules.removeAll();
    }

    @Test
    public void shouldAddEnrollment() {
        Schedule schedule = allSchedules.getByName("IPTI Schedule");
        Enrollment enrollment = new Enrollment().setExternalId("externalId").setSchedule(schedule).setCurrentMilestoneName("IPTI 1").setStartOfSchedule(now()).setEnrolledOn(now()).setPreferredAlertTime(new Time(now().toLocalTime())).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null);
        allEnrollments.add(enrollment);

        Enrollment enrollmentFromDb = allEnrollments.get(enrollment.getId());

        assertNotNull(enrollmentFromDb);
        assertNull(enrollmentFromDb.getSchedule());
        assertEquals(EnrollmentStatus.ACTIVE, enrollmentFromDb.getStatus());
    }

    @Test
    public void shouldGetAllEnrollmentsWithSchedulePopulatedInThem() {
        Schedule schedule = allSchedules.getByName("IPTI Schedule");
        allEnrollments.add(new Enrollment().setExternalId("externalId").setSchedule(schedule).setCurrentMilestoneName("IPTI 1").setStartOfSchedule(now()).setEnrolledOn(now()).setPreferredAlertTime(new Time(now().toLocalTime())).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null));

        List<Enrollment> enrollments = allEnrollments.getAll();

        assertEquals(1, enrollments.size());
        Schedule actualSchedule = enrollments.get(0).getSchedule();
        assertEquals(allSchedules.getByName("IPTI Schedule"), actualSchedule);
    }

    @Test
    public void shouldFindActiveEnrollmentByExternalIdAndScheduleNameWithSchedulePopulatedInThem() {
        String scheduleName = "IPTI Schedule";
        Schedule schedule = allSchedules.getByName(scheduleName);
        Enrollment enrollment = new Enrollment().setExternalId("entity_1").setSchedule(schedule).setCurrentMilestoneName("IPTI 1").setStartOfSchedule(now()).setEnrolledOn(now()).setPreferredAlertTime(new Time(DateUtil.now().toLocalTime())).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null);
        enrollment.setStatus(EnrollmentStatus.UNENROLLED);
        allEnrollments.add(enrollment);

        enrollment = new Enrollment().setExternalId("entity_1").setSchedule(schedule).setCurrentMilestoneName("IPTI 1").setStartOfSchedule(now()).setEnrolledOn(now()).setPreferredAlertTime(new Time(DateUtil.now().toLocalTime())).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null);
        allEnrollments.add(enrollment);

        Enrollment activeEnrollment = allEnrollments.getActiveEnrollment("entity_1", scheduleName);
        assertNotNull(activeEnrollment);
        assertEquals(schedule, activeEnrollment.getSchedule());
    }

    @Test
    public void shouldFindActiveEnrollmentByExternalIdAndScheduleName() {
        String scheduleName = "IPTI Schedule";
        Enrollment enrollment = new Enrollment().setExternalId("entity_1").setSchedule(allSchedules.getByName(scheduleName)).setCurrentMilestoneName("IPTI 1").setStartOfSchedule(now()).setEnrolledOn(now()).setPreferredAlertTime(new Time(DateUtil.now().toLocalTime())).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null);
        enrollment.setStatus(EnrollmentStatus.UNENROLLED);
        allEnrollments.add(enrollment);

        assertNull(allEnrollments.getActiveEnrollment("entity_1", scheduleName));
    }

    @Test
    public void shouldConvertTheFulfillmentDateTimeIntoCorrectTimeZoneWhenRetrievingAnEnrollmentWithFulfilledMilestoneFromDatabase() {
        String scheduleName = "IPTI Schedule";
        Enrollment enrollment = new Enrollment().setExternalId("entity_1").setSchedule(allSchedules.getByName(scheduleName)).setCurrentMilestoneName("IPTI 1").setStartOfSchedule(now()).setEnrolledOn(now()).setPreferredAlertTime(new Time(DateUtil.now().toLocalTime())).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null);
        allEnrollments.add(enrollment);
        DateTime fulfillmentDateTime = DateTime.now();
        enrollment.fulfillCurrentMilestone(fulfillmentDateTime);
        allEnrollments.update(enrollment);

        Enrollment enrollmentFromDatabase = allEnrollments.getActiveEnrollment("entity_1", scheduleName);
        assertEquals(fulfillmentDateTime, enrollmentFromDatabase.getLastFulfilledDate());
    }

    @Test
    public void shouldReturnTheMilestoneStartDateTimeInCorrectTimeZoneForFirstMilestone() {
        DateTime now = DateTime.now();
        String scheduleName = "IPTI Schedule";
        Enrollment enrollment = new Enrollment().setExternalId("entity_1").setSchedule(allSchedules.getByName(scheduleName)).setCurrentMilestoneName("IPTI 1").setStartOfSchedule(now.minusDays(2)).setEnrolledOn(now).setPreferredAlertTime(new Time(now.toLocalTime())).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null);
        allEnrollments.add(enrollment);

        Enrollment enrollmentFromDatabase = allEnrollments.getActiveEnrollment("entity_1", scheduleName);
        assertEquals(now.minusDays(2), enrollmentFromDatabase.getCurrentMilestoneStartDate());
    }

    @Test
    public void shouldReturnTheMilestoneStartDateTimeInCorrectTimeZoneForSecondMilestone() {
        Schedule schedule = allSchedules.getByName("IPTI Schedule");
        DateTime now = DateTime.now();
        Enrollment enrollment = new Enrollment().setExternalId("entity_1").setSchedule(schedule).setCurrentMilestoneName("IPTI 1").setStartOfSchedule(now.minusDays(2)).setEnrolledOn(now).setPreferredAlertTime(new Time(now.toLocalTime())).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null);
        allEnrollments.add(enrollment);
        enrollmentService.fulfillCurrentMilestone(enrollment, now);
        allEnrollments.update(enrollment);

        Enrollment enrollmentFromDatabase = allEnrollments.getActiveEnrollment("entity_1", "IPTI Schedule");
        assertEquals(now, enrollmentFromDatabase.getCurrentMilestoneStartDate());
    }

    @Test
    public void shouldReturnTheMilestoneStartDateTimeInCorrectTimeZoneWhenEnrollingIntoSecondMilestone() {
        Schedule schedule = allSchedules.getByName("IPTI Schedule");
        DateTime now = DateTime.now();
        Enrollment enrollment = new Enrollment().setExternalId("entity_1").setSchedule(schedule).setCurrentMilestoneName("IPTI 2").setStartOfSchedule(now.minusDays(2)).setEnrolledOn(now).setPreferredAlertTime(new Time(now.toLocalTime())).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null);
        allEnrollments.add(enrollment);

        Enrollment enrollmentFromDatabase = allEnrollments.getActiveEnrollment("entity_1", "IPTI Schedule");
        assertEquals(now, enrollmentFromDatabase.getCurrentMilestoneStartDate());
    }

    @Test
    public void shouldReturnEnrollmentsThatMatchAGivenExternalId() {
        DateTime now = now();
        Schedule iptiSchedule = allSchedules.getByName("IPTI Schedule");
        Schedule deliverySchedule = allSchedules.getByName("Delivery");
        allEnrollments.add(new Enrollment().setExternalId("entity_1").setSchedule(iptiSchedule).setCurrentMilestoneName("IPTI 1").setStartOfSchedule(now).setEnrolledOn(now).setPreferredAlertTime(new Time(8, 10)).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null));
        allEnrollments.add(new Enrollment().setExternalId("entity_1").setSchedule(deliverySchedule).setCurrentMilestoneName("Default").setStartOfSchedule(now).setEnrolledOn(now).setPreferredAlertTime(new Time(8, 10)).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null));
        allEnrollments.add(new Enrollment().setExternalId("entity_2").setSchedule(iptiSchedule).setCurrentMilestoneName("IPTI 1").setStartOfSchedule(now).setEnrolledOn(now).setPreferredAlertTime(new Time(8, 10)).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null));
        allEnrollments.add(new Enrollment().setExternalId("entity_3").setSchedule(iptiSchedule).setCurrentMilestoneName("IPTI 1").setStartOfSchedule(now).setEnrolledOn(now).setPreferredAlertTime(new Time(8, 10)).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null));

        List<Enrollment> filteredEnrollments = allEnrollments.findByExternalId("entity_1");
//        assertNotNull(filteredEnrollments.get(0).getSchedule());
        assertEquals(asList(new String[] { "entity_1", "entity_1"}), Lambda.extract(filteredEnrollments, on(Enrollment.class).getExternalId()));
    }

    @Test
    public void shouldFindAllEnrollmentsThatMatchesGivenScheduleNames() {
        Schedule iptiSchedule = allSchedules.getByName("IPTI Schedule");
        Schedule absoluteSchedule = allSchedules.getByName("Absolute Schedule");
        Schedule relativeSchedule = allSchedules.getByName("Relative Schedule");

        allEnrollments.add(new Enrollment().setExternalId("entity_1").setSchedule(iptiSchedule).setCurrentMilestoneName("IPTI 1").setStartOfSchedule(now()).setEnrolledOn(now()).setPreferredAlertTime(new Time(DateUtil.now().toLocalTime())).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null));
        allEnrollments.add(new Enrollment().setExternalId("entity_2").setSchedule(absoluteSchedule).setCurrentMilestoneName("milestone1").setStartOfSchedule(now()).setEnrolledOn(now()).setPreferredAlertTime(new Time(DateUtil.now().toLocalTime())).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null));
        allEnrollments.add(new Enrollment().setExternalId("entity_3").setSchedule(relativeSchedule).setCurrentMilestoneName("milestone1").setStartOfSchedule(now()).setEnrolledOn(now()).setPreferredAlertTime(new Time(DateUtil.now().toLocalTime())).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null));
        allEnrollments.add(new Enrollment().setExternalId("entity_4").setSchedule(relativeSchedule).setCurrentMilestoneName("milestone1").setStartOfSchedule(now()).setEnrolledOn(now()).setPreferredAlertTime(new Time(DateUtil.now().toLocalTime())).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null));

        List<Enrollment> filteredEnrollments = allEnrollments.findBySchedule(asList(new String[]{"IPTI Schedule", "Relative Schedule"}));

        assertEquals(3, filteredEnrollments.size());
        assertEquals(asList(new String[] { "entity_1", "entity_3", "entity_4" }), Lambda.extract(filteredEnrollments, on(Enrollment.class).getExternalId()));
        assertEquals(asList(new String[] {"IPTI Schedule", "Relative Schedule", "Relative Schedule"}), Lambda.extract(filteredEnrollments, on(Enrollment.class).getScheduleName()));
    }

    @Test
    public void shouldReturnEnrollmentsThatMatchGivenStatus() {
        DateTime now = now();
        Schedule iptiSchedule = allSchedules.getByName("IPTI Schedule");
        Schedule deliverySchedule = allSchedules.getByName("Delivery");
        allEnrollments.add(new Enrollment().setExternalId("entity_1").setSchedule(iptiSchedule).setCurrentMilestoneName("IPTI 1").setStartOfSchedule(now).setEnrolledOn(now).setPreferredAlertTime(new Time(8, 10)).setStatus(EnrollmentStatus.COMPLETED).setMetadata(null));
        allEnrollments.add(new Enrollment().setExternalId("entity_2").setSchedule(deliverySchedule).setCurrentMilestoneName("Default").setStartOfSchedule(now).setEnrolledOn(now).setPreferredAlertTime(new Time(8, 10)).setStatus(EnrollmentStatus.DEFAULTED).setMetadata(null));
        allEnrollments.add(new Enrollment().setExternalId("entity_3").setSchedule(iptiSchedule).setCurrentMilestoneName("IPTI 1").setStartOfSchedule(now).setEnrolledOn(now).setPreferredAlertTime(new Time(8, 10)).setStatus(EnrollmentStatus.UNENROLLED).setMetadata(null));
        allEnrollments.add(new Enrollment().setExternalId("entity_4").setSchedule(iptiSchedule).setCurrentMilestoneName("IPTI 1").setStartOfSchedule(now).setEnrolledOn(now).setPreferredAlertTime(new Time(8, 10)).setStatus(EnrollmentStatus.ACTIVE).setMetadata(null));

        List<Enrollment> filteredEnrollments = allEnrollments.findByStatus(EnrollmentStatus.ACTIVE);
        assertEquals(asList(new String[] { "entity_4"}), Lambda.extract(filteredEnrollments, on(Enrollment.class).getExternalId()));

        filteredEnrollments = allEnrollments.findByStatus(EnrollmentStatus.DEFAULTED);
        assertNotNull(filteredEnrollments.get(0).getSchedule());
        assertEquals(asList(new String[] { "entity_2"}), Lambda.extract(filteredEnrollments, on(Enrollment.class).getExternalId()));
    }

    @Test
    public void shouldReturnEnrollmentsThatWereCompletedDuringTheGivenTimeRage() {
        LocalDate today = today();
        scheduleTrackingService.enroll(new EnrollmentRequest().setExternalId("entity_1").setScheduleName("IPTI Schedule").setPreferredAlertTime(new Time(8, 10)).setReferenceDate(today).setReferenceTime(null).setEnrollmentDate(today).setEnrollmentTime(null).setStartingMilestoneName("IPTI 1").setMetadata(null));

        scheduleTrackingService.enroll(new EnrollmentRequest().setExternalId("entity_2").setScheduleName("IPTI Schedule").setPreferredAlertTime(new Time(8, 10)).setReferenceDate(today.minusWeeks(2)).setReferenceTime(null).setEnrollmentDate(today.minusWeeks(2)).setEnrollmentTime(null).setStartingMilestoneName("IPTI 1").setMetadata(null));
        scheduleTrackingService.fulfillCurrentMilestone("entity_2", "IPTI Schedule", today.minusDays(2), new Time(0, 0));
        scheduleTrackingService.fulfillCurrentMilestone("entity_2", "IPTI Schedule", today.minusDays(1), new Time(0, 0));

        scheduleTrackingService.enroll(new EnrollmentRequest().setExternalId("entity_3").setScheduleName("IPTI Schedule").setPreferredAlertTime(new Time(8, 10)).setReferenceDate(today).setReferenceTime(null).setEnrollmentDate(today).setEnrollmentTime(null).setStartingMilestoneName("IPTI 2").setMetadata(null));
        scheduleTrackingService.fulfillCurrentMilestone("entity_3", "IPTI Schedule", today, new Time(0, 0));

        scheduleTrackingService.enroll(new EnrollmentRequest().setExternalId("entity_4").setScheduleName("IPTI Schedule").setPreferredAlertTime(new Time(8, 10)).setReferenceDate(today.minusYears(2)).setReferenceTime(null).setEnrollmentDate(today).setEnrollmentTime(null).setStartingMilestoneName("IPTI 1").setMetadata(null));

        scheduleTrackingService.enroll(new EnrollmentRequest().setExternalId("entity_5").setScheduleName("IPTI Schedule").setPreferredAlertTime(new Time(8, 10)).setReferenceDate(today.minusWeeks(2)).setReferenceTime(null).setEnrollmentDate(today.minusWeeks(2)).setEnrollmentTime(null).setStartingMilestoneName("IPTI 1").setMetadata(null));
        scheduleTrackingService.fulfillCurrentMilestone("entity_5", "IPTI Schedule", today.minusDays(10), new Time(0, 0));
        scheduleTrackingService.fulfillCurrentMilestone("entity_5", "IPTI Schedule", today.minusDays(9), new Time(0, 0));

        DateTime start = newDateTime(today.minusWeeks(1), new Time(0, 0));
        DateTime end = newDateTime(today, new Time(0, 0));
        List<Enrollment> filteredEnrollments = allEnrollments.completedDuring(start, end);
        assertNotNull(filteredEnrollments.get(0).getSchedule());
        assertEquals(asList(new String[] { "entity_2", "entity_3" }), Lambda.extract(filteredEnrollments, on(Enrollment.class).getExternalId()));
    }
}
