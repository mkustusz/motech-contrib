package org.motechproject.scheduletracking.api.domain;


import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Objects;

public final class MilestoneAlert implements Serializable {
    private String milestoneName;

    private DateTime earliestDateTime;
    private DateTime dueDateTime;
    private DateTime lateDateTime;
    private DateTime defaultmentDateTime;

    public static MilestoneAlert fromMilestone(Milestone milestone, DateTime startOfMilestone) {
        return new MilestoneAlert(milestone.getName(),
                getWindowStartDate(milestone, startOfMilestone, WindowName.earliest),
                getWindowStartDate(milestone, startOfMilestone, WindowName.due),
                getWindowStartDate(milestone, startOfMilestone, WindowName.late),
                getWindowStartDate(milestone, startOfMilestone, WindowName.max));
    }

    private static DateTime getWindowStartDate(Milestone milestone, DateTime startOfMilestone, WindowName windowName) {
        return startOfMilestone.plus(milestone.getWindowStart(windowName));
    }

    private MilestoneAlert() {
    }

    public MilestoneAlert(String milestoneName, DateTime earliestDateTime, DateTime dueDateTime, DateTime lateDateTime, DateTime defaultmentDateTime) {
        this.milestoneName = milestoneName;
        this.earliestDateTime = earliestDateTime;
        this.dueDateTime = dueDateTime;
        this.lateDateTime = lateDateTime;
        this.defaultmentDateTime = defaultmentDateTime;
    }

    public String getMilestoneName() {
        return milestoneName;
    }

    public DateTime getEarliestDateTime() {
        return earliestDateTime;
    }

    public DateTime getDueDateTime() {
        return dueDateTime;
    }

    public DateTime getLateDateTime() {
        return lateDateTime;
    }

    public DateTime getDefaultmentDateTime() {
        return defaultmentDateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof MilestoneAlert)) {
            return false;
        }

        MilestoneAlert that = (MilestoneAlert) o;

        return Objects.equals(defaultmentDateTime, that.defaultmentDateTime) &&
                Objects.equals(dueDateTime, that.dueDateTime) && Objects.equals(earliestDateTime, that.earliestDateTime) &&
                Objects.equals(lateDateTime, that.lateDateTime) && Objects.equals(milestoneName, that.milestoneName);
    }

    @Override
    public int hashCode() {
        int result = milestoneName != null ? milestoneName.hashCode() : 0;
        result = 31 * result + (earliestDateTime != null ? earliestDateTime.hashCode() : 0);
        result = 31 * result + (dueDateTime != null ? dueDateTime.hashCode() : 0);
        result = 31 * result + (lateDateTime != null ? lateDateTime.hashCode() : 0);
        result = 31 * result + (defaultmentDateTime != null ? defaultmentDateTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MilestoneAlert{" +
                "milestoneName='" + milestoneName + '\'' +
                ", earliestDateTime=" + earliestDateTime +
                ", dueDateTime=" + dueDateTime +
                ", lateDateTime=" + lateDateTime +
                ", defaultmentDateTime=" + defaultmentDateTime +
                '}';
    }
}
