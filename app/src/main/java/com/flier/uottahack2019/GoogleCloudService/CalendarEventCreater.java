package com.flier.uottahack2019.GoogleCloudService;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

public class CalendarEventCreater {

    public CalendarEventCreater() {

    }

    public Event createEvent(String eventName, DateTime startTime, DateTime endTime) {
        Event event = new Event();
        event.setSummary(eventName);

        EventDateTime start = new EventDateTime()
                .setDateTime(startTime);
        EventDateTime end = new EventDateTime()
                .setDateTime(endTime);

        event.setStart(start);
        event.setEnd(end);
        return event;
    }


}
