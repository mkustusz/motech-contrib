package org.motechproject.pillreminder.builder;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.pillreminder.contract.MedicineResponse;
import org.motechproject.pillreminder.domain.Medicine;

import static org.junit.Assert.assertEquals;

public class MedicineResponseBuilderTest {
    private MedicineResponseBuilder builder = new MedicineResponseBuilder();

    @Test
    public void shouldBuildMedicineResponse() {
        String medicineName = "paracetamol";
        LocalDate startDate = DateUtil.today();
        LocalDate endDate = startDate.plusDays(2);
        Medicine medicine = new Medicine(medicineName, startDate, endDate);

        MedicineResponse medicineResponse = builder.createFrom(medicine);

        assertEquals(startDate, medicineResponse.getStartDate());
        assertEquals(endDate, medicineResponse.getEndDate());
        assertEquals(medicineName, medicineResponse.getName());
    }
}
