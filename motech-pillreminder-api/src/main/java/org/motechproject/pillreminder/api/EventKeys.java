package org.motechproject.pillreminder.api;

import org.motechproject.model.MotechEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventKeys {
	private final static Logger log = LoggerFactory.getLogger(EventKeys.class);

	
	public final static String PILLREMINDER_ID_KEY = "PillReminderID";
    public final static String PARTY_ID_KEY = "PartyID";
    public final static String SCHEDULE_JOB_ID_KEY = "JobID";
    public final static String LANGUAGE_KEY = "Language";
    public final static String BASE_SUBJECT = "org.motechproject.server.pillreminder.";

    public final static String PILLREMINDER_CREATED_SUBJECT = BASE_SUBJECT + "reminder.created";
    public final static String PILLREMINDER_UPDATED_SUBJECT = BASE_SUBJECT + "reminder.updated";
    public final static String PILLREMINDER_DELETED_SUBJECT = BASE_SUBJECT + "reminder.deleted";

    public final static String PILLREMINDER_REMINDER_EVENT_SUBJECT = BASE_SUBJECT + "reminder";

	public static final String PILLREMINDER_PUBLISH_REMINDER = BASE_SUBJECT + "publish-reminder";
    

    public static String getScheduleJobIdKey(MotechEvent event)
    {
        return getStringValue(event, EventKeys.SCHEDULE_JOB_ID_KEY);
    }

    public static String getPartyID(MotechEvent event)
    {
        return getStringValue(event, EventKeys.PARTY_ID_KEY);
    }

    public static String getReminderID(MotechEvent event)
    {
        return getStringValue(event, EventKeys.PILLREMINDER_ID_KEY);
    }
    
    public static String getLanguageKey(MotechEvent event)
    {
    	return getStringValue(event, EventKeys.LANGUAGE_KEY);
    }

    public static String getStringValue(MotechEvent event, String key)
    {
        String ret = null;
        try {
            ret = (String) event.getParameters().get(key);
        } catch (ClassCastException e) {
            log.warn("Event: " + event + " Key: " + key + " is not a String");
        }

        return ret;
    }

    public static Integer getIntegerValue(MotechEvent event, String key)
    {
        Integer ret = null;
        try {
            ret = (Integer)event.getParameters().get(key);
        } catch (ClassCastException e) {
            log.warn("Event: " + event + " Key: " + key + " is not an Integer");
        }
        return ret;
    }
	
}
