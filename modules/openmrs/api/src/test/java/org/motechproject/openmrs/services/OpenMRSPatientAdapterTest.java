package org.motechproject.openmrs.services;

import org.apache.commons.collections.ListUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.mrs.EventKeys;
import org.motechproject.mrs.exception.PatientNotFoundException;
import org.motechproject.mrs.helper.EventHelper;
import org.motechproject.mrs.model.OpenMRSAttribute;
import org.motechproject.mrs.model.OpenMRSFacility;
import org.motechproject.mrs.model.OpenMRSPatient;
import org.motechproject.mrs.model.OpenMRSPerson;
import org.motechproject.openmrs.IdentifierType;
import org.motechproject.openmrs.helper.PatientHelper;
import org.motechproject.openmrs.util.PatientTestUtil;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class OpenMRSPatientAdapterTest {

    @Mock
    private PatientService mockPatientService;
    @Mock
    private PersonService mockPersonService;
    @Mock
    private OpenMRSFacilityAdapter mockFacilityAdapter;
    @Mock
    private OpenMRSConceptAdapter mockOpenMRSConceptAdapter;
    @Mock
    private EventRelay eventRelay;

    OpenMRSPatientAdapter openMRSPatientAdapter;
    PatientTestUtil patientTestUtil;
    @Mock
    private OpenMRSPersonAdapter mockPersonAdapter;

    @Before
    public void setUp() {
        initMocks(this);
        openMRSPatientAdapter = new OpenMRSPatientAdapter();
        patientTestUtil = new PatientTestUtil();
        ReflectionTestUtils.setField(openMRSPatientAdapter, "patientService", mockPatientService);
        ReflectionTestUtils.setField(openMRSPatientAdapter, "personService", mockPersonService);
        ReflectionTestUtils.setField(openMRSPatientAdapter, "facilityAdapter", mockFacilityAdapter);
        ReflectionTestUtils.setField(openMRSPatientAdapter, "patientHelper", new PatientHelper());
        ReflectionTestUtils.setField(openMRSPatientAdapter, "personAdapter", mockPersonAdapter);
        ReflectionTestUtils.setField(openMRSPatientAdapter, "openMrsConceptAdapter", mockOpenMRSConceptAdapter);
        ReflectionTestUtils.setField(openMRSPatientAdapter, "eventRelay", eventRelay);
    }

    @Test
    public void shouldSaveAPatient() {
        final Person person = new Person();
        final String first = "First";
        final String middle = "Middle";
        final String last = "Last";
        final String address1 = "a good street in ghana";
        final Date birthDate = new LocalDate(1970, 3, 11).toDate();
        final boolean birthdateEstimated = true;
        final String gender = "male";
        String facilityId = "1000";
        final OpenMRSFacility facility = new OpenMRSFacility(facilityId, "name", "country", "region", "district", "province");
        String motechId = "1234567";
        final Location location = new Location(Integer.parseInt(facilityId));

        org.openmrs.Patient openMRSPatient = patientTestUtil.setUpOpenMRSPatient(person, first, middle, last, address1, birthDate, birthdateEstimated, gender, facility, motechId);

        when(mockPatientService.savePatient(Matchers.<org.openmrs.Patient>any())).thenReturn(openMRSPatient);
        when(mockFacilityAdapter.getLocation(facilityId)).thenReturn(location);
        when(mockFacilityAdapter.convertLocationToFacility(any(Location.class))).thenReturn(facility);

        OpenMRSPerson mrsPerson = new OpenMRSPerson().firstName(first).middleName(middle).lastName(last).birthDateEstimated(birthdateEstimated).dateOfBirth(new DateTime(birthDate)).address(address1).gender(gender);
        when(mockPersonAdapter.openMRSToMRSPerson(openMRSPatient)).thenReturn(mrsPerson);

        OpenMRSPatient mrsPatient = new OpenMRSPatient(motechId, mrsPerson, facility);
        final OpenMRSPatient actualPatient = openMRSPatientAdapter.savePatient(mrsPatient);

        verify(eventRelay).sendEventMessage(new MotechEvent(EventKeys.CREATED_NEW_PATIENT_SUBJECT, EventHelper.patientParameters(actualPatient)));
        verify(mockPersonAdapter).openMRSToMRSPerson(openMRSPatient);

        ArgumentCaptor<org.openmrs.Patient> openMrsPatientArgumentCaptor = ArgumentCaptor.forClass(org.openmrs.Patient.class);
        verify(mockPatientService).savePatient(openMrsPatientArgumentCaptor.capture());
        patientTestUtil.assertEqualsForOpenMrsPatient(openMrsPatientArgumentCaptor.getValue(), openMRSPatient);

        patientTestUtil.verifyReturnedPatient(first, middle, last, address1, birthDate, birthdateEstimated, gender, facility, actualPatient, motechId);
    }

    @Test
    public void shouldGetPatientById() {
        final Person person = new Person();
        final String first = "First";
        final String middle = "Middle";
        final String last = "Last";
        final String address1 = "a good street in ghana";
        final Date birthDate = new LocalDate(1970, 3, 11).toDate();
        final boolean birthDateEstimated = true;
        final String gender = "male";
        final OpenMRSFacility facility = new OpenMRSFacility("1000", "name", "country", "region", "district", "province");
        String motechId = "1234567";

        final org.openmrs.Patient openMRSPatient = patientTestUtil.setUpOpenMRSPatient(person, first, middle, last, address1, birthDate, birthDateEstimated, gender, facility, motechId);
        int patientId = 12;
        when(mockPatientService.getPatient(patientId)).thenReturn(openMRSPatient);
        when(mockFacilityAdapter.convertLocationToFacility(any(Location.class))).thenReturn(facility);

        OpenMRSPerson mrsPerson = new OpenMRSPerson().firstName(first).middleName(middle).lastName(last).birthDateEstimated(birthDateEstimated).dateOfBirth(new DateTime(birthDate)).address(address1).gender(gender);
        when(mockPersonAdapter.openMRSToMRSPerson(openMRSPatient)).thenReturn(mrsPerson);

        OpenMRSPatient returnedPatient = openMRSPatientAdapter.getPatient(String.valueOf(patientId));

        verify(mockPatientService).getPatient(patientId);
        verify(mockPersonAdapter).openMRSToMRSPerson(openMRSPatient);

        patientTestUtil.verifyReturnedPatient(first, middle, last, address1, birthDate, birthDateEstimated, gender, facility, returnedPatient, motechId);
    }

    @Test
    public void shouldGetPatientByMotechId() {
        final Person person = new Person();
        final String first = "First";
        final String middle = "Middle";
        final String last = "Last";
        final String address1 = "a good street in ghana";
        final Date birthDate = new LocalDate(1970, 3, 11).toDate();
        final boolean birthDateEstimated = true;
        final String gender = "male";
        final OpenMRSFacility facility = new OpenMRSFacility("1000", "name", "country", "region", "district", "province");
        String motechId = "11";
        PatientIdentifierType motechIdType = mock(PatientIdentifierType.class);

        final org.openmrs.Patient openMRSPatient = patientTestUtil.setUpOpenMRSPatient(person, first, middle, last, address1, birthDate, birthDateEstimated, gender, facility, motechId);
        List<PatientIdentifierType> idTypes = Arrays.asList(motechIdType);
        when(mockPatientService.getPatients(null, motechId, idTypes, true)).thenReturn(Arrays.asList(openMRSPatient));
        when(mockPatientService.getPatientIdentifierTypeByName(IdentifierType.IDENTIFIER_MOTECH_ID.getName())).thenReturn(motechIdType);
        when(mockFacilityAdapter.convertLocationToFacility(any(Location.class))).thenReturn(facility);

        OpenMRSPerson mrsPerson = new OpenMRSPerson().firstName(first).middleName(middle).lastName(last).birthDateEstimated(birthDateEstimated).dateOfBirth(new DateTime(birthDate)).address(address1).gender(gender);
        when(mockPersonAdapter.openMRSToMRSPerson(openMRSPatient)).thenReturn(mrsPerson);

        OpenMRSPatient returnedPatient = openMRSPatientAdapter.getPatientByMotechId(motechId);

        verify(mockPersonAdapter).openMRSToMRSPerson(openMRSPatient);
        patientTestUtil.verifyReturnedPatient(first, middle, last, address1, birthDate, birthDateEstimated, gender, facility, returnedPatient, motechId);
    }

    @Test
    public void shouldReturnNullGetPatientByMotechId() {
        String motechId = "11";
        PatientIdentifierType motechIdType = mock(PatientIdentifierType.class);

        List<PatientIdentifierType> idTypes = Arrays.asList(motechIdType);
        when(mockPatientService.getPatients(null, motechId, idTypes, true)).thenReturn(ListUtils.EMPTY_LIST);
        when(mockPatientService.getPatientIdentifierTypeByName(IdentifierType.IDENTIFIER_MOTECH_ID.getName())).thenReturn(motechIdType);
        assertNull(openMRSPatientAdapter.getPatientByMotechId(motechId));

    }

    @Test
    public void shouldReturnNullIfPatientByIdIsNotFound() {
        int patientId = 12;
        when(mockPatientService.getPatient(patientId)).thenReturn(null);
        assertNull(openMRSPatientAdapter.getPatient(String.valueOf(patientId)));
    }

    @Test
    public void shouldRetrieveOpenMrsIdentifierTypeGivenTheIdentifierName() {
        PatientIdentifierType patientIdentiferTypeMock = mock(PatientIdentifierType.class);
        when(mockPatientService.getPatientIdentifierTypeByName(IdentifierType.IDENTIFIER_MOTECH_ID.getName())).thenReturn(patientIdentiferTypeMock);
        assertThat(openMRSPatientAdapter.getPatientIdentifierType(IdentifierType.IDENTIFIER_MOTECH_ID), is(patientIdentiferTypeMock));
    }

    @Test
    public void shouldGetOpenMrsPatientById() {
        org.openmrs.Patient mrsPatient = mock(org.openmrs.Patient.class);
        Integer patientId = 1000;

        when(mockPatientService.getPatient(patientId)).thenReturn(mrsPatient);
        org.openmrs.Patient returnedPatient = openMRSPatientAdapter.getOpenMrsPatient(String.valueOf(patientId));
        assertThat(returnedPatient, is(equalTo(mrsPatient)));
    }

    @Test
    public void shouldGetAgeOfThePersonUsingMotechId() {
        String motechId = "1234567";
        Integer expectedAge = 4;
        Patient mockOpenMRSPatient = mock(Patient.class);
        OpenMRSPatientAdapter openMRSPatientAdapterSpy = spy(openMRSPatientAdapter);

        doReturn(mockOpenMRSPatient).when(openMRSPatientAdapterSpy).getOpenmrsPatientByMotechId(motechId);
        when(mockOpenMRSPatient.getAge()).thenReturn(expectedAge);
        Integer age = openMRSPatientAdapterSpy.getOpenmrsPatientByMotechId(motechId).getAge();
        verify(mockOpenMRSPatient).getAge();
        assertEquals(age, expectedAge);
    }

    @Test
    public void shouldSearchByPatientNameOrId() {
        OpenMRSPatientAdapter openMRSPatientAdapterSpy = spy(openMRSPatientAdapter);
        String name = "name";
        String id = "1000";
        Patient openMrsPatient1 = mock(Patient.class);
        Patient openMrsPatient2 = mock(Patient.class);
        List<Patient> patientsMatchingSearchQuery = Arrays.asList(openMrsPatient1, openMrsPatient2);
        PatientIdentifierType identifierTypeMock = mock(PatientIdentifierType.class);
        when(mockPatientService.getPatientIdentifierTypeByName(IdentifierType.IDENTIFIER_MOTECH_ID.getName())).thenReturn(identifierTypeMock);
        when(mockPatientService.getPatients(name, id, Arrays.asList(identifierTypeMock), false)).thenReturn(patientsMatchingSearchQuery);

        OpenMRSPatient mrsPatient1 = new OpenMRSPatient(null, new OpenMRSPerson().firstName("Zef"), null);
        OpenMRSPatient mrsPatient2 = new OpenMRSPatient(null, new OpenMRSPerson().firstName("Abc"), null);
        doReturn(mrsPatient1).when(openMRSPatientAdapterSpy).getMrsPatient(openMrsPatient1);
        doReturn(mrsPatient2).when(openMRSPatientAdapterSpy).getMrsPatient(openMrsPatient2);

        List<org.motechproject.mrs.domain.Patient> returnedPatients = openMRSPatientAdapterSpy.search(name, id);
        List<org.motechproject.mrs.domain.Patient> otherPatients = new ArrayList<org.motechproject.mrs.domain.Patient>();
        otherPatients.add(mrsPatient2);
        otherPatients.add(mrsPatient1);
        assertThat(returnedPatients, is(equalTo(otherPatients)));

    }

    @Test
    public void shouldSaveCauseOfDeath() throws PatientNotFoundException {
        String patientId = "patientId";
        Patient patient = new Patient();
        Date dateOfDeath = mock(Date.class);
        Concept concept = mock(Concept.class);
        String conceptName = "NONE";

        openMRSPatientAdapter = spy(openMRSPatientAdapter);
        doReturn(patient).when(openMRSPatientAdapter).getOpenmrsPatientByMotechId(patientId);
        when(mockOpenMRSConceptAdapter.getConceptByName(conceptName)).thenReturn(concept);

        openMRSPatientAdapter.deceasePatient(patientId, conceptName, dateOfDeath, null);

        verify(eventRelay).sendEventMessage(new MotechEvent(EventKeys.PATIENT_DECEASED_SUBJECT, EventHelper.patientParameters(openMRSPatientAdapter.getMrsPatient(patient))));
        InOrder order = inOrder(mockPatientService);
        order.verify(mockPatientService).savePatient(patient);
        order.verify(mockPatientService).saveCauseOfDeathObs(patient, dateOfDeath, concept, null);
        assertThat(patient.getCauseOfDeath(), is(concept));
    }

    @Test(expected = PatientNotFoundException.class)
    public void shouldThrowPatientNotFoundExceptionIfPatientIsNotFound() throws PatientNotFoundException {
        String patientId = "patientId";
        Date dateOfDeath = mock(Date.class);
        String conceptName = "NONE";

        openMRSPatientAdapter = spy(openMRSPatientAdapter);
        doReturn(null).when(openMRSPatientAdapter).getOpenmrsPatientByMotechId(patientId);
        openMRSPatientAdapter.deceasePatient(patientId, conceptName, dateOfDeath, null);
    }

    @Test
    public void shouldUpdateAPatient() {
        String firstName = "first";
        String middleName = "middle";
        String lastName = "last";
        String preferredName = "preferred";
        Date dateOfBirth = new Date();
        Boolean estimatedDate = false;
        Boolean insured = false;
        String nhisNumber = "1234";
        String gender = "male";
        String address = "address";
        String facilityName = "facility";
        String facilityCountry = "ghana";
        String facilityRegion = "region";
        String facilityDistrict = "district";
        String facilitySubDistrict = "province";
        final String motechId = "12";
        final String nhisNumberAttribute = "NHIS Number";
        final String nhisExpirationDateAttribute = "NHIS Expiration Date";
        final String insuredAttribute = "Insured";

        final String nhisExpiryDateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        final OpenMRSPerson person = new OpenMRSPerson().firstName(firstName).middleName(middleName).lastName(lastName)
                .gender(gender).address(address).dateOfBirth(new DateTime(dateOfBirth)).birthDateEstimated(estimatedDate).preferredName(preferredName)
                .addAttribute(new OpenMRSAttribute(nhisNumberAttribute, nhisNumber)).addAttribute(new OpenMRSAttribute(nhisExpirationDateAttribute,
                        nhisExpiryDateString)).addAttribute(new OpenMRSAttribute(insuredAttribute, String.valueOf(insured)));
        String newFacilityId = "60";
        final OpenMRSFacility mrsFacility = new OpenMRSFacility(newFacilityId);
        Location location = location(facilityName, facilityCountry, facilityRegion, facilityDistrict, facilitySubDistrict);
        when(mockFacilityAdapter.getLocation(newFacilityId)).thenReturn(location);
        final OpenMRSFacility mrsFacilityOld = new OpenMRSFacility("61", facilityName + "Old", facilityCountry + "Old", facilityRegion + "Old", facilityDistrict + "Old", facilitySubDistrict + "Old");
        OpenMRSPatient mrsPatient = new OpenMRSPatient(motechId, person, mrsFacility);

        final org.openmrs.Patient mockPatient = patientTestUtil.setUpOpenMRSPatient(new Person(), "diffFirst", "diffMiddle", "diffLast", "diffAddress", new LocalDate(2001, 10, 10).toDate(), !estimatedDate, "female", mrsFacilityOld, motechId);

        final PersonAttributeType nhisAttributeType = new PersonAttributeType(1);
        nhisAttributeType.setName(nhisNumberAttribute);
        final PersonAttributeType insuredAttributeType = new PersonAttributeType(2);
        insuredAttributeType.setName(insuredAttribute);
        when(mockPersonService.getPersonAttributeTypeByName(nhisNumberAttribute)).thenReturn(nhisAttributeType);
        when(mockPersonService.getPersonAttributeTypeByName(insuredAttribute)).thenReturn(insuredAttributeType);
        final PersonAttributeType expirationDateAttributeType = new PersonAttributeType(3);
        expirationDateAttributeType.setName(nhisExpirationDateAttribute);
        when(mockPersonService.getPersonAttributeTypeByName(nhisExpirationDateAttribute)).thenReturn(expirationDateAttributeType);
        when(mockPatientService.getPatient(Integer.valueOf(mrsPatient.getMotechId()))).thenReturn(mockPatient);
        when(mockPatientService.getPatient(Integer.valueOf(mrsPatient.getMotechId()))).thenReturn(mockPatient);
        when(mockPatientService.savePatient(Matchers.any(Patient.class))).thenReturn(mockPatient);

        final OpenMRSPatientAdapter spyPatientAdapter = spy(openMRSPatientAdapter);
        doReturn(mockPatient).when(spyPatientAdapter).getOpenmrsPatientByMotechId(mrsPatient.getMotechId());
        spyPatientAdapter.updatePatient(mrsPatient);

        verify(eventRelay).sendEventMessage(new MotechEvent(EventKeys.UPDATED_PATIENT_SUBJECT, EventHelper.patientParameters(openMRSPatientAdapter.getMrsPatient(mockPatient))));

        final ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(mockPatientService).savePatient(captor.capture());
        final Patient actualPatient = captor.getValue();
        assertThat(actualPatient.getPatientIdentifier().getIdentifier(), is(motechId));
        assertThat(actualPatient.getGivenName(), is(preferredName));
        assertThat(actualPatient.getMiddleName(), is(middleName));
        assertThat(actualPatient.getFamilyName(), is(lastName));
        assertThat(actualPatient.getAddresses().iterator().next().getAddress1(), is(address));
        assertThat(actualPatient.getBirthdate(), is(dateOfBirth));
        assertThat(actualPatient.getGender(), is(gender));
        assertThat(actualPatient.isBirthdateEstimated(), is(estimatedDate));
        assertThat(actualPatient.getAttribute(nhisNumberAttribute).getValue(), is(nhisNumber));
        assertThat(actualPatient.getAttribute(nhisExpirationDateAttribute).getValue(), is(nhisExpiryDateString));
        assertThat(actualPatient.getAttribute(insuredAttribute).getValue(), is(String.valueOf(insured)));
        assertThat(actualPatient.getPatientIdentifier().getLocation().getCountry(), is(facilityCountry));
        assertThat(actualPatient.getPatientIdentifier().getLocation().getAddress6(), is(facilityRegion));
        assertThat(actualPatient.getPatientIdentifier().getLocation().getCountyDistrict(), is(facilityDistrict));
        assertThat(actualPatient.getPatientIdentifier().getLocation().getStateProvince(), is(facilitySubDistrict));
        assertThat(actualPatient.getPatientIdentifier().getLocation().getName(), is(facilityName));
    }

    private Location location(String facilityName, String facilityCountry, String facilityRegion, String facilityDistrict, String facilitySubDistrict) {
        Location location = new Location();
        location.setName(facilityName);
        location.setAddress6(facilityRegion);
        location.setStateProvince(facilitySubDistrict);
        location.setCountyDistrict(facilityDistrict);
        location.setCountry(facilityCountry);
        location.setName(facilityName);
        return location;
    }
}
