package org.motechproject.pillreminder.builder;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.motechproject.commons.date.model.Time;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.pillreminder.contract.DosageRequest;
import org.motechproject.pillreminder.contract.MedicineRequest;
import org.motechproject.pillreminder.domain.Dosage;
import org.motechproject.pillreminder.domain.Medicine;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class DosageBuilderTest {

    private DosageBuilder builder = new DosageBuilder();

    @Test
    public void shouldBuildADosageFromRequest() {
        LocalDate startDate = DateUtil.today();
        LocalDate endDate = startDate.plusDays(2);
        MedicineRequest medicineRequest = new MedicineRequest("m1", startDate, endDate);

        DosageRequest dosageRequest = new DosageRequest(9, 5, Arrays.asList(medicineRequest));

        Dosage dosage = builder.createFrom(dosageRequest);

        assertEquals(new Time(9, 5), dosage.getDosageTime());
        assertEquals(1, dosage.getMedicines().size());

        for (Medicine medicine : dosage.getMedicines()) {
            assertEquals("m1", medicine.getName());
            assertEquals(startDate, medicine.getStartDate());
            assertEquals(endDate, medicine.getEndDate());
        }
    }
}
