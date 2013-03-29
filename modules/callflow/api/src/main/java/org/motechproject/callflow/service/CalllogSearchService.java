package org.motechproject.callflow.service;

import org.motechproject.decisiontree.core.CallDetail;

import java.util.List;

public interface CalllogSearchService {

    List<CallDetail> search(CalllogSearchParameters searchParameters);

    long count(CalllogSearchParameters params);

    long findMaxCallDuration();

    List<String> getAllPhoneNumbers();
}
