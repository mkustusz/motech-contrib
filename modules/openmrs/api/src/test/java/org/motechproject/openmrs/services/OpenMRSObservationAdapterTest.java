package org.motechproject.openmrs.services;

import org.hamcrest.Matchers;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.mrs.EventKeys;
import org.motechproject.mrs.exception.ObservationNotFoundException;
import org.motechproject.mrs.helper.EventHelper;
import org.motechproject.mrs.domain.MRSObservation;
import org.motechproject.openmrs.model.OpenMRSConcept;
import org.motechproject.openmrs.model.OpenMRSConceptName;
import org.motechproject.openmrs.model.OpenMRSObservation;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.ObsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.selectFirst;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class OpenMRSObservationAdapterTest {

    OpenMRSObservationAdapter observationAdapter;

    @Mock
    OpenMRSConceptAdapter mockConceptAdapter;
    @Mock
    private ObsService mockObservationService;
    @Mock
    private Patient patient;
    @Mock
    private Location facility;
    @Mock
    private Encounter encounter;
    @Mock
    private OpenMRSUserAdapter mockOpenMRSUserAdapter;
    @Mock
    private OpenMRSPatientAdapter mockOpenMRSPatientAdapter;
    @Mock
    private User creator;
    @Mock
    EventRelay eventRelay;

    @Before
    public void setUp() {
        initMocks(this);
        observationAdapter = new OpenMRSObservationAdapter();
        ReflectionTestUtils.setField(observationAdapter, "openMRSConceptAdapter", mockConceptAdapter);
        ReflectionTestUtils.setField(observationAdapter, "obsService", mockObservationService);
        ReflectionTestUtils.setField(observationAdapter, "openMRSUserAdapter", mockOpenMRSUserAdapter);
        ReflectionTestUtils.setField(observationAdapter, "openMRSPatientAdapter", mockOpenMRSPatientAdapter);
        ReflectionTestUtils.setField(observationAdapter, "eventRelay", eventRelay);
    }

    @Test
    public void shouldCreateAnObservationToHoldConceptValue() {

        String observationConceptName = "concept1";
        String observationValueConceptName = "concept2";
        String dependentObservationValueConceptName = "concept3";

        Date observationDate = new LocalDate(2011, 12, 31).toDate();
        Date dependentObservationDate = new LocalDate(1999, 1, 1).toDate();

        OpenMRSConcept observationValueConcept = new OpenMRSConcept(new OpenMRSConceptName(observationValueConceptName));
        OpenMRSConcept dependentConceptValue = new OpenMRSConcept(new OpenMRSConceptName(dependentObservationValueConceptName));

        Concept openMrsConceptUsedForObsName = mock(Concept.class);
        Concept openMrsConceptUsedForObsValue = mock(Concept.class);
        Concept openMrsConceptUsedForDepObsValue = mock(Concept.class);

        when(mockConceptAdapter.getConceptByName(observationConceptName)).thenReturn(openMrsConceptUsedForObsName);
        when(mockConceptAdapter.getConceptByName(observationValueConceptName)).thenReturn(openMrsConceptUsedForObsValue);
        when(mockConceptAdapter.getConceptByName(dependentObservationValueConceptName)).thenReturn(openMrsConceptUsedForDepObsValue);

        OpenMRSObservation<OpenMRSConcept> expectedDeliveryConcept = new OpenMRSObservation<OpenMRSConcept>(observationDate, observationConceptName, observationValueConcept);
        expectedDeliveryConcept.addDependantObservation(new OpenMRSObservation<OpenMRSConcept>(dependentObservationDate, dependentObservationValueConceptName, dependentConceptValue));

        Obs openMRSObservation = observationAdapter.createOpenMRSObservationForEncounter(expectedDeliveryConcept, encounter, patient, facility, creator);

        assertOpenMrsObservationProperties(openMRSObservation, expectedDeliveryConcept, patient, facility, encounter, creator, openMrsConceptUsedForObsName);

        assertThat(openMRSObservation.getValueCoded(), is(equalTo(openMrsConceptUsedForObsValue)));
        assertThat(openMRSObservation.getGroupMembers().size(), is(1));
        final Obs returnedDependentObservation = openMRSObservation.getGroupMembers().iterator().next();
        assertThat(returnedDependentObservation.getObsDatetime(), is(dependentObservationDate));
        assertThat(returnedDependentObservation.getValueCoded(), is(openMrsConceptUsedForDepObsValue));
    }


    @Test
    public void shouldCreateAObservationToHoldDateStringDoubleBooleanValues() {
        Date observationDate = new LocalDate(2011, 12, 31).toDate();
        String observationConceptName = "concept1";

        String feverValue = "high";
        Double temperatureValue = 99.0;
        Boolean hivValue = false;
        Date expectedDeliveryDateValue = new LocalDate(2012, 12, 21).toDate();


        OpenMRSObservation<String> fever = new OpenMRSObservation<String>(observationDate, observationConceptName, feverValue);
        OpenMRSObservation<Double> temperature = new OpenMRSObservation<Double>(observationDate, observationConceptName, temperatureValue);
        OpenMRSObservation<Boolean> hiv = new OpenMRSObservation<Boolean>(observationDate, observationConceptName, hivValue);
        OpenMRSObservation<Date> expectedDeliveryDate = new OpenMRSObservation<Date>(observationDate, observationConceptName, expectedDeliveryDateValue);

        Concept openMrsConceptUsedForObsName = mock(Concept.class);
        when(mockConceptAdapter.getConceptByName(observationConceptName)).thenReturn(openMrsConceptUsedForObsName);

        Obs openMrsObservation = observationAdapter.createOpenMRSObservationForEncounter(fever, encounter, patient, facility, creator);
        assertOpenMrsObservationProperties(openMrsObservation, fever, patient, facility, encounter, creator, openMrsConceptUsedForObsName);
        assertThat(openMrsObservation.getValueText(), is(equalTo(feverValue)));

        openMrsObservation = observationAdapter.createOpenMRSObservationForEncounter(temperature, encounter, patient, facility, creator);
        assertOpenMrsObservationProperties(openMrsObservation, temperature, patient, facility, encounter, creator, openMrsConceptUsedForObsName);
        assertThat(openMrsObservation.getValueNumeric(), is(equalTo(temperatureValue)));

        openMrsObservation = observationAdapter.createOpenMRSObservationForEncounter(hiv, encounter, patient, facility, creator);
        assertOpenMrsObservationProperties(openMrsObservation, hiv, patient, facility, encounter, creator, openMrsConceptUsedForObsName);
        assertThat(openMrsObservation.getValueAsBoolean(), is(equalTo(hivValue)));

        openMrsObservation = observationAdapter.createOpenMRSObservationForEncounter(expectedDeliveryDate, encounter, patient, facility, creator);
        assertOpenMrsObservationProperties(openMrsObservation, expectedDeliveryDate, patient, facility, encounter, creator, openMrsConceptUsedForObsName);
        assertThat(openMrsObservation.getValueDatetime(), is(equalTo(expectedDeliveryDateValue)));
    }

    @Test
    public void shouldCreateObservationsForAnEncounter() {
        Patient patient = mock(Patient.class);
        Location facility = mock(Location.class);
        Encounter encounter = mock(Encounter.class);
        User creator = mock(User.class);

        OpenMRSObservationAdapter observationAdapterSpy = spy(observationAdapter);
        final OpenMRSObservation observation1 = mock(OpenMRSObservation.class);
        final OpenMRSObservation observation2 = mock(OpenMRSObservation.class);

        Set<OpenMRSObservation> mrsObservations = new HashSet<OpenMRSObservation>() {{
            add(observation1);
            add(observation2);
        }};

        final Obs openMrsObservation1 = mock(Obs.class);
        final Obs openMrsObservation2 = mock(Obs.class);
        doReturn(openMrsObservation1).when(observationAdapterSpy).createOpenMRSObservationForEncounter(observation1, encounter, patient, facility, creator);
        doReturn(openMrsObservation2).when(observationAdapterSpy).createOpenMRSObservationForEncounter(observation2, encounter, patient, facility, creator);

        Set<Obs> openMrsObservations = observationAdapterSpy.createOpenMRSObservationsForEncounter(mrsObservations, encounter, patient, facility, creator);
        assertThat(openMrsObservations, is(equalTo((Set<Obs>) new HashSet<Obs>() {{
            add(openMrsObservation1);
            add(openMrsObservation2);
        }})));
    }

    @Test
    public void shouldConvertOpenMRSObservationsToMRSObservations() {
        final Obs obs1 = new Obs();
        final Obs obs2 = new Obs();

        Concept concept1 = mock(Concept.class);
        ConceptDatatype conceptDatatype = mock(ConceptDatatype.class);
        ConceptDatatype dependentConceptDataType = mock(ConceptDatatype.class);
        when(concept1.getDatatype()).thenReturn(conceptDatatype);
        when(conceptDatatype.isText()).thenReturn(true);
        when(dependentConceptDataType.isText()).thenReturn(true);
        ConceptName conceptName1 = mock(ConceptName.class);
        ConceptName dependentconceptName = mock(ConceptName.class);
        when(concept1.getName()).thenReturn(conceptName1);
        when(conceptName1.getName()).thenReturn("name1");

        final Concept concept2 = mock(Concept.class);
        final Concept dependentConcept = mock(Concept.class);
        ConceptDatatype conceptDatatype1 = mock(ConceptDatatype.class);
        when(concept2.getDatatype()).thenReturn(conceptDatatype1);
        when(dependentConcept.getDatatype()).thenReturn(dependentConceptDataType);
        when(conceptDatatype1.isNumeric()).thenReturn(true);
        ConceptName conceptName2 = mock(ConceptName.class);
        when(concept2.getName()).thenReturn(conceptName2);
        when(dependentConcept.getName()).thenReturn(dependentconceptName);
        when(conceptName2.getName()).thenReturn("name2");
        final String dependentConceptName = "name3";
        when(dependentconceptName.getName()).thenReturn(dependentConceptName);

        obs1.setId(1);
        obs1.setConcept(concept1);
        Encounter encounter = new Encounter();
        Patient person = new Patient();
        obs1.setEncounter(encounter);
        obs1.setPerson(person);
        obs1.setObsDatetime(new Date());
        obs1.setCreator(new User());
        obs1.setValueText("tr");
        final String dependentConceptValue = "2";
        final Date dependentObservationDate = new Date();

        obs1.addGroupMember(new Obs() {{
            setId(10);
            setObsDatetime(dependentObservationDate);
            setConcept(dependentConcept);
            setValueText(dependentConceptValue);
        }});

        obs2.setId(2);
        obs2.setConcept(concept2);
        obs2.setEncounter(encounter);
        obs2.setPerson(person);
        obs2.setObsDatetime(new Date());
        obs2.setCreator(new User());
        obs2.setValueNumeric(10.12);

        Set<Obs> openMRSObservations = new HashSet<Obs>() {{
            add(obs1);
            add(obs2);
        }};
        Set<? extends MRSObservation> actualMrsObservations = observationAdapter.convertOpenMRSToMRSObservations(openMRSObservations);

        assertThat(actualMrsObservations.size(), Matchers.is(equalTo(2)));
        final OpenMRSObservation expectedObservation1 = new OpenMRSObservation(obs1.getObsDatetime(), conceptName1.getName(), obs1.getValueText());
        expectedObservation1.addDependantObservation(new OpenMRSObservation("10", dependentObservationDate, dependentConceptName, dependentConceptValue));
        assertMRSObservation(observationBy(conceptName1, actualMrsObservations), expectedObservation1, true);
        assertMRSObservation(observationBy(conceptName2, actualMrsObservations),
                new OpenMRSObservation(obs2.getObsDatetime(), conceptName2.getName(), obs2.getValueNumeric()), false);
    }

    private OpenMRSObservation observationBy(ConceptName conceptName1, Set<? extends MRSObservation> actualMrsObservations) {
        return (OpenMRSObservation) selectFirst(actualMrsObservations, having(on(OpenMRSObservation.class).getConceptName(), equalTo(conceptName1.getName())));
    }

    private void assertMRSObservation(OpenMRSObservation actualObservation, OpenMRSObservation expectedObservation, boolean hasDependents) {
        assertThat(actualObservation.getConceptName(), is(expectedObservation.getConceptName()));
        assertThat(actualObservation.getValue(), is(expectedObservation.getValue()));
        assertThat(actualObservation.getDate(), is(expectedObservation.getDate()));
        if (hasDependents) {
            assertThat(actualObservation.getDependantObservations().size(), is(expectedObservation.getDependantObservations().size()));
            assertMRSObservation((OpenMRSObservation) actualObservation.getDependantObservations().iterator().next(),
                    (OpenMRSObservation) expectedObservation.getDependantObservations().iterator().next(), false);
        }
    }

    @Test
    public void shouldSetTheValueBasedOnType() {
        Obs fever = new Obs();
        Obs temperature = new Obs();
        Obs expectedDeliveryDate = new Obs();
        Obs HIV = new Obs();
        Obs conceptObs = new Obs();
        Obs nullObs = new Obs();

        String feverValue = "high";
        Double temperatureValue = 99.0;
        Boolean hivValue = false;
        Date expectedDeliveryDateValue = new LocalDate(2012, 12, 21).toDate();
        final OpenMRSConcept concept = new OpenMRSConcept(new OpenMRSConceptName("conceptName"));

        Concept openMrsConcept = mock(Concept.class);
        when(mockConceptAdapter.getConceptByName(concept.getName().getName())).thenReturn(openMrsConcept);

        observationAdapter.writeValueToOpenMRSObservation(feverValue, fever);
        observationAdapter.writeValueToOpenMRSObservation(temperatureValue, temperature);
        observationAdapter.writeValueToOpenMRSObservation(expectedDeliveryDateValue, expectedDeliveryDate);
        observationAdapter.writeValueToOpenMRSObservation(hivValue, HIV);
        observationAdapter.writeValueToOpenMRSObservation(concept, conceptObs);
        observationAdapter.writeValueToOpenMRSObservation(null, nullObs);

        assertThat(fever.getValueText(), is(equalTo(feverValue)));
        assertThat(temperature.getValueNumeric(), is(equalTo(temperatureValue)));
        assertThat(expectedDeliveryDate.getValueDatetime(), is(equalTo(expectedDeliveryDateValue)));
        assertThat(HIV.getValueAsBoolean(), is(equalTo(hivValue)));
        assertThat(conceptObs.getValueCoded(), is(equalTo(openMrsConcept)));
    }

    @Test
    public void shouldThrowExceptionIfInvalidArgumentIsSet() {
        try {
            observationAdapter.writeValueToOpenMRSObservation(new Object(), new Obs());
            Assert.fail("should throw exception");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void shouldSaveObservation() {
        OpenMRSObservationAdapter observationAdapterSpy = Mockito.spy(observationAdapter);
        MRSObservation mrsObservation = mock(OpenMRSObservation.class);
        MRSObservation savedMRSObservation = mock(OpenMRSObservation.class);
        Obs openMRSObservation = Mockito.mock(Obs.class);
        Obs savedOpenMRSObservation = Mockito.mock(Obs.class);

        Encounter encounter = new Encounter();
        Patient patient = new Patient();
        User creator = new User();
        Location facility = new Location();

        doReturn(openMRSObservation).when(observationAdapterSpy).createOpenMRSObservationForEncounter(mrsObservation, encounter, patient, facility, creator);
        when(mockObservationService.saveObs(openMRSObservation, null)).thenReturn(savedOpenMRSObservation);
        doReturn(savedMRSObservation).when(observationAdapterSpy).convertOpenMRSToMRSObservation(savedOpenMRSObservation);

        MRSObservation returnedMRSObservation = observationAdapterSpy.saveObservation(mrsObservation, encounter, patient, facility, creator);
        verify(eventRelay).sendEventMessage(new MotechEvent(EventKeys.CREATED_NEW_OBSERVATION_SUBJECT, EventHelper.observationParameters(returnedMRSObservation)));
        Assert.assertThat(returnedMRSObservation, Matchers.is(equalTo(savedMRSObservation)));
    }

    private void assertOpenMrsObservationProperties(Obs openMrsObservation, OpenMRSObservation mrsObservation, Patient patient,
                                                    Location facility, Encounter encounter, User creator, Concept concept) {
        assertThat(openMrsObservation.getObsDatetime(), is(equalTo(mrsObservation.getDate().toDate())));
        assertThat(openMrsObservation.getConcept(), is(equalTo(concept)));
        assertThat(openMrsObservation.getPerson(), is(equalTo((Person) patient)));
        assertThat(openMrsObservation.getLocation(), is(equalTo(facility)));
        assertThat(openMrsObservation.getEncounter(), is(equalTo(encounter)));
        assertThat(openMrsObservation.getCreator(), is(equalTo(creator)));
    }

    @Test
    public void shouldFindObservation() {
        OpenMRSObservationAdapter spyOpenMrsObservationAdapter = spy(observationAdapter);

        String patientMotechId = "234";
        String concept = "conceptName";
        Patient openMRSpatient = new Patient();
        Concept openMRSConcept = new Concept();
        final Obs mockObs = mock(Obs.class);
        OpenMRSObservation mockMrsObs = mock(OpenMRSObservation.class);
        ArrayList<Obs> obsList = new ArrayList<Obs>(){{
            add(mockObs);
        }};

        when(mockOpenMRSPatientAdapter.getOpenmrsPatientByMotechId(patientMotechId)).thenReturn(openMRSpatient);
        when(mockConceptAdapter.getConceptByName(concept)).thenReturn(openMRSConcept);
        when(mockObservationService.getObservationsByPersonAndConcept(openMRSpatient, openMRSConcept)).thenReturn(obsList);
        doReturn(mockMrsObs).when(spyOpenMrsObservationAdapter).convertOpenMRSToMRSObservation(mockObs);

        spyOpenMrsObservationAdapter.findObservation(patientMotechId, concept);
        verify(spyOpenMrsObservationAdapter).convertOpenMRSToMRSObservation(mockObs);
    }

    @Test
    public void shouldVoidObservation() throws ObservationNotFoundException {
        OpenMRSObservationAdapter spyOpenMrsObservationAdapter = spy(observationAdapter);
        final OpenMRSObservation mockMrsObs = mock(OpenMRSObservation.class);
        String observationId = "34";
        String mrsUserId="userId";
        OpenMRSObservation mrsObservation = new OpenMRSObservation(observationId, new Date(), "name", Integer.valueOf("34"));
        Obs expectedOpenmRSObs = new Obs();
        expectedOpenmRSObs.setVoided(false);
        String reason = "reason";
        User user = new User(12);

        when(mockObservationService.getObs(Integer.valueOf(observationId))).thenReturn(expectedOpenmRSObs);
        when(mockOpenMRSUserAdapter.getOpenMrsUserByUserName(mrsUserId)).thenReturn(user);
        Obs copyExpectedOpenmRSObs = expectedOpenmRSObs;
        copyExpectedOpenmRSObs.setVoided(true);
        copyExpectedOpenmRSObs.setVoidReason(reason);
        copyExpectedOpenmRSObs.setDateVoided(new Date());
        copyExpectedOpenmRSObs.setVoidedBy(user);
        doReturn(mockMrsObs).when(spyOpenMrsObservationAdapter).convertOpenMRSToMRSObservation(copyExpectedOpenmRSObs);

        spyOpenMrsObservationAdapter.voidObservation(mrsObservation, reason, mrsUserId);

        ArgumentCaptor<Obs> captor = ArgumentCaptor.forClass(Obs.class);
        verify(mockObservationService).voidObs(captor.capture(), eq(reason));
        verify(eventRelay).sendEventMessage(new MotechEvent(EventKeys.DELETED_OBSERVATION_SUBJECT, EventHelper.observationParameters(mockMrsObs)));

        Obs actualOpenMRSObservation = captor.getValue();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        assertThat(actualOpenMRSObservation.getVoided(), is(true));
        assertThat(actualOpenMRSObservation.getVoidReason(), is(reason));
        assertThat(actualOpenMRSObservation.getVoidedBy(), is(user));
        assertThat(format.format(actualOpenMRSObservation.getDateVoided()), is(format.format(Calendar.getInstance().getTime())));
    }

    @Test
    public void shouldReturnAllObservationsGivenPatientIdAndConceptName(){
        OpenMRSObservationAdapter spyOpenMrsObservationAdapter = spy(observationAdapter);

        String conceptName = "conceptName";
        String patientMotechId = "patientMotechId";
         Patient openMRSpatient = new Patient();
        Concept openMRSConcept = new Concept();
        final Obs mockObs1 = mock(Obs.class);
        final Obs mockObs2 = mock(Obs.class);
        final OpenMRSObservation mockMrsObs1 = mock(OpenMRSObservation.class);
        final OpenMRSObservation mockMrsObs2 = mock(OpenMRSObservation.class);
        List<Obs> obsList = new ArrayList<Obs>(){{
            add(mockObs1);
            add(mockObs2);
        }};

        List<MRSObservation> expectedMRSObservations = new ArrayList<MRSObservation>(){{
            add(mockMrsObs1);
            add(mockMrsObs2);
        }};

        when(mockOpenMRSPatientAdapter.getOpenmrsPatientByMotechId(patientMotechId)).thenReturn(openMRSpatient);
        when(mockConceptAdapter.getConceptByName(conceptName)).thenReturn(openMRSConcept);
        when(mockObservationService.getObservationsByPersonAndConcept(openMRSpatient, openMRSConcept)).thenReturn(obsList);
        doReturn(mockMrsObs1).when(spyOpenMrsObservationAdapter).convertOpenMRSToMRSObservation(mockObs1);
        doReturn(mockMrsObs2).when(spyOpenMrsObservationAdapter).convertOpenMRSToMRSObservation(mockObs2);

        List<MRSObservation> actualObservations = spyOpenMrsObservationAdapter.findObservations(patientMotechId, conceptName);
        ArgumentCaptor<Obs> obsArgumentCaptor=ArgumentCaptor.forClass(Obs.class);
        verify(spyOpenMrsObservationAdapter,times(2)).convertOpenMRSToMRSObservation(obsArgumentCaptor.capture());

        assertThat(obsArgumentCaptor.getAllValues(),is(obsList));
        assertThat(actualObservations,is(expectedMRSObservations));

    }
}
