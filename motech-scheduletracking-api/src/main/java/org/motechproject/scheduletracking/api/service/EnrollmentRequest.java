package org.motechproject.scheduletracking.api.service;

import org.joda.time.LocalDate;
import org.motechproject.model.Time;
import org.motechproject.scheduletracking.api.domain.Milestone;

public class EnrollmentRequest {
    private String externalId;
    private String scheduleName;
	private Time preferredAlertTime;
	private LocalDate referenceDate;
    private String startingMilestoneName;

    public EnrollmentRequest(String externalId, String scheduleName, Time preferredAlertTime, LocalDate referenceDate) {
        this.externalId = externalId;
        this.scheduleName = scheduleName;
        this.preferredAlertTime = preferredAlertTime;
        this.referenceDate = referenceDate;
    }

	public EnrollmentRequest(String externalId, String scheduleName, Time preferredAlertTime, LocalDate referenceDate, String startingMilestoneName) {
        this(externalId, scheduleName, preferredAlertTime, referenceDate);
        this.startingMilestoneName = startingMilestoneName;
	}

    public String getExternalId() {
        return externalId;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public String getStartingMilestoneName() {
        return startingMilestoneName;
    }

    public boolean enrollIntoMilestone() {
        return startingMilestoneName != null;
    }

    public Time getPreferredAlertTime() {
        return preferredAlertTime;
    }

	public LocalDate getReferenceDate() {
		return referenceDate;
	}
}
