package org.motechproject.server.alerts.service;

import org.motechproject.server.alerts.domain.Alert;
import org.motechproject.server.alerts.domain.AlertCriteria;
import org.motechproject.server.alerts.domain.AlertStatus;
import org.motechproject.server.alerts.domain.AlertType;

import java.util.List;
import java.util.Map;

public interface AlertService {
    @Deprecated
    void createAlert(Alert alert);

    void create(String entityId, String name, String description, AlertType type, AlertStatus status, int priority, Map<String, String> data);

    @Deprecated
    List<Alert> getBy(String externalId, AlertType type, AlertStatus status, Integer priority, int limit);

    List<Alert> search(AlertCriteria alertCriteria);

    void changeStatus(String id, AlertStatus status);

    void setData(String id, String key, String value);

    Alert get(String id);
}