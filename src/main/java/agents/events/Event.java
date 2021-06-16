package agents.events;

import jade.core.AID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Event {

    public static List<Event> eventList = new ArrayList<>();

    private EventType event;

    private AID who;

    private String where;

    private String additional_data;

    private String when;

    public Event(){ }

    public Event(EventType event, AID agent, String container, String additional_data){
        this.event = event;
        this.who = agent;
        this.where = container;
        this.additional_data = additional_data;
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyy HH:mm:ss");
        this.when = currentTime.format(format);
    }

    public static void createEvent(Event newEvent){
        eventList.add(newEvent);
    }

    public void setEvent(EventType event) { this.event = event; }
    public void setWho(AID who) { this.who = who; }
    public void setWhere(String where) { this.where = where; }
    public void setAdditional_data(String additional_data) { this.additional_data = additional_data; }
    public void setWhen(String when) { this.when = when; }

    public EventType getEvent() { return event; }
    public AID getWho(){ return who; }
    public String getWhere() { return where; }
    public String getAdditional_data() { return additional_data; }
    public String getWhen() { return when; }
}
