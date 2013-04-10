package org.motechproject.openmrs.services;

import ch.lambdaj.Lambda;
import ch.lambdaj.function.matcher.LambdaJMatcher;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.mrs.EventKeys;
import org.motechproject.mrs.helper.EventHelper;
import org.motechproject.mrs.domain.MRSFacility;
import org.motechproject.mrs.services.MRSFacilityAdapter;
import org.motechproject.openmrs.model.OpenMRSFacility;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;

/**
 * Manages OpenMRS Facilities
 */
@Service
public class OpenMRSFacilityAdapter implements MRSFacilityAdapter {

    @Autowired
    private LocationService locationService;

    @Autowired
    private EventRelay eventRelay;

    /**
     * Saves a MRSFacility into OpenMRS
     *
     * @param theFacility Object to be saved
     * @return The saved Facility
     */
    @Override
    public MRSFacility saveFacility(final MRSFacility theFacility) {
        MRSFacility facility = theFacility;
        String facilityId = facility.getFacilityId();
        Location location = new Location();
        if (facilityId != null) {
            location = locationService.getLocation(Integer.parseInt(facilityId));
        } else {
            final List<Location> locationsWithSameName = locationService.getLocations(facility.getName());
            final List<Location> matchedLocation = Lambda.select(locationsWithSameName, having(on(Location.class), locationMatcher(facility)));
            if (CollectionUtils.isNotEmpty(matchedLocation)) {
                location = matchedLocation.get(0);
            }
        }
        location.setName(facility.getName());
        location.setCountry(facility.getCountry());
        location.setAddress6(facility.getRegion());
        location.setStateProvince(facility.getStateProvince());
        location.setCountyDistrict(facility.getCountyDistrict());

        Location savedLocation = this.locationService.saveLocation(location);
        MRSFacility facilityInst = convertLocationToFacility(savedLocation);
        if (facilityId == null) {
            eventRelay.sendEventMessage(new MotechEvent(EventKeys.CREATED_NEW_FACILITY_SUBJECT, EventHelper.facilityParameters(facilityInst)));
        } else {
            eventRelay.sendEventMessage(new MotechEvent(EventKeys.UPDATED_FACILITY_SUBJECT, EventHelper.facilityParameters(facilityInst)));
        }
        return facilityInst;
    }

    private LambdaJMatcher<Location> locationMatcher(final MRSFacility facility) {
        return new LambdaJMatcher<Location>() {
            @Override
            public boolean matches(Object o) {
                Location location = (Location) o;
                return new EqualsBuilder().append(location.getName(), facility.getName())
                        .append(location.getCountry(), facility.getCountry())
                        .append(location.getAddress6(), facility.getRegion())
                        .append(location.getStateProvince(), facility.getStateProvince())
                        .append(location.getCountyDistrict(), facility.getCountyDistrict())
                        .isEquals();
            }
        };
    }

    /**
     * Gets all the Facilities in OpenMRS
     *
     * @return List of all Facilities
     */
    @Override
    public List<MRSFacility> getFacilities() {
        List<Location> locations = locationService.getAllLocations();
        List<MRSFacility> facilities = new ArrayList<MRSFacility>();
        for (Location location : locations) {
            facilities.add(convertLocationToFacility(location));
        }
        return facilities;
    }

    /**
     * Finds List of all Facilities matching the given facility name.
     *
     * @param locationName Value to be used to search
     * @return List of matches Facilities, else empty list
     */
    @Override
    public List<MRSFacility> getFacilities(String locationName) {
        final List<Location> locations = locationService.getLocations(locationName);
        final ArrayList<MRSFacility> facilities = new ArrayList<MRSFacility>();
        for (Location location : locations) {
            facilities.add(convertLocationToFacility(location));
        }
        return facilities;
    }

    /**
     * Finds the Facility in OpenMRS by facility ID.
     *
     * @param facilityId Id of the facility to be fetched
     * @return Facility Object if found, else null
     */
    @Override
    public MRSFacility getFacility(String facilityId) {
        if (StringUtils.isEmpty(facilityId)) {
            return null;
        }
        final Location location = getLocation(facilityId);
        return (location != null) ? convertLocationToFacility(location) : null;
    }

    Location getLocation(String facilityId) {
        return locationService.getLocation(Integer.parseInt(facilityId));
    }

    MRSFacility convertLocationToFacility(Location savedLocation) {
        return new OpenMRSFacility(String.valueOf(savedLocation.getId()), savedLocation.getName(), savedLocation.getCountry(),
                savedLocation.getAddress6(), savedLocation.getCountyDistrict(), savedLocation.getStateProvince());
    }

    @Override
    public void deleteFacility(String facilityId) {
        Location existingOpenMrsFacility = locationService.getLocation(Integer.parseInt(facilityId));
        if (existingOpenMrsFacility != null) {
            locationService.purgeLocation(existingOpenMrsFacility);
            eventRelay.sendEventMessage(new MotechEvent(EventKeys.DELETED_FACILITY_SUBJECT, EventHelper.facilityParameters(convertLocationToFacility(existingOpenMrsFacility))));
        }
    }

    @Override
    public MRSFacility updateFacility(MRSFacility facility) {
        return saveFacility(facility);
    }
}
