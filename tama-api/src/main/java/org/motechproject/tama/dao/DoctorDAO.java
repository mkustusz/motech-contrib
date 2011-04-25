package org.motechproject.tama.dao;

import java.util.List;

import org.motechproject.dao.BaseDao;
import org.motechproject.tama.model.Doctor;

public interface DoctorDAO extends BaseDao<Doctor> {
	
	public List<Doctor> findByClinicId(String clinicId);
}
