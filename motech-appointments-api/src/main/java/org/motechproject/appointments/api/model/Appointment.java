package org.motechproject.appointments.api.model;

import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;
import org.motechproject.model.ExtensibleDataObject;
import org.motechproject.util.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Appointment extends ExtensibleDataObject {

    @JsonProperty
    private String id;
    @JsonProperty
    private DateTime originalDueDate;
    @JsonProperty
    private DateTime dueDate;
    @JsonProperty
    private DateTime confirmedDate;
    @JsonProperty
    private List<Reminder> reminders;

    public Appointment() {
        id = UUID.randomUUID().toString();
    }

    public String id() {
        return id;
    }

    public DateTime dueDate() {
        return DateUtil.setTimeZone(dueDate);
    }

    public Appointment dueDate(DateTime dueDate) {
        this.dueDate = dueDate;
        this.originalDueDate = dueDate;
        return this;
    }

    public DateTime originalDueDate() {
        return DateUtil.setTimeZone(originalDueDate);
    }

    public Appointment adjustDueDate(DateTime adjustedDueDate) {
        this.dueDate = adjustedDueDate;
        return this;
    }

    public DateTime confirmedDate() {
        return DateUtil.setTimeZone(confirmedDate);
    }

    public Appointment confirmedDate(DateTime confirmedDate) {
        this.confirmedDate = confirmedDate;
        return this;
    }

    public Reminder reminder() {
        if (reminders != null && !reminders.isEmpty())
            return reminders.get(0);
        return null;
    }

    public Appointment reminder(Reminder reminder) {
        if(reminders == null) {
            reminders = new ArrayList<Reminder>();
        }
        this.reminders.add(reminder);
        return this;
    }
    
    public Appointment reminders(List<Reminder> reminders) {
        this.reminders = reminders;
        return this;
    }

    public List<Reminder> reminders() {
        return reminders;
    }
}
