package org.motechproject.pillreminder.builder;

import org.motechproject.pillreminder.contract.DailyPillRegimenRequest;
import org.motechproject.pillreminder.contract.DosageRequest;
import org.motechproject.pillreminder.domain.DailyScheduleDetails;
import org.motechproject.pillreminder.domain.Dosage;
import org.motechproject.pillreminder.domain.PillRegimen;

import java.util.HashSet;
import java.util.Set;

public class PillRegimenBuilder {

    private DosageBuilder dosageBuilder = new DosageBuilder();

    public PillRegimen createDailyPillRegimenFrom(DailyPillRegimenRequest dailyPillRegimenRequest) {
        final int pillWindowInHours = dailyPillRegimenRequest.getPillWindowInHours();
        final int reminderRepeatIntervalInMinutes = dailyPillRegimenRequest.getReminderRepeatIntervalInMinutes();
        final int bufferOverDosageTime = dailyPillRegimenRequest.getBufferOverDosageTimeInMinutes();
        final DailyScheduleDetails scheduleDetails = new DailyScheduleDetails(reminderRepeatIntervalInMinutes, pillWindowInHours, bufferOverDosageTime);
        return new PillRegimen(dailyPillRegimenRequest.getExternalId(), getDosages(dailyPillRegimenRequest), scheduleDetails);
    }

    private Set<Dosage> getDosages(DailyPillRegimenRequest pillRegimenRequest) {
        Set<Dosage> dosages = new HashSet<Dosage>();
        for (DosageRequest dosageRequest : pillRegimenRequest.getDosageRequests()) {
            dosages.add(dosageBuilder.createFrom(dosageRequest));
        }
        return dosages;
    }
}
