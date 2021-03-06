package fr.univlyon1.tiw.tiw1.calendar.tp2.metier.modele;

import fr.univlyon1.tiw.tiw1.calendar.tp2.config.Config;
import fr.univlyon1.tiw.tiw1.calendar.tp2.metier.dao.ICalendarDAO;
import fr.univlyon1.tiw.tiw1.calendar.tp2.metier.dto.EventDTO;
import fr.univlyon1.tiw.tiw1.calendar.tp2.metier.util.Command;
import fr.univlyon1.tiw.tiw1.calendar.tp2.server.annuaire.Annuaire;
import fr.univlyon1.tiw.tiw1.calendar.tp2.server.annuaire.RegistryVariable;
import fr.univlyon1.tiw.tiw1.calendar.tp2.server.context.CalendarContext;
import fr.univlyon1.tiw.tiw1.calendar.tp2.server.context.ContextVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InvalidClassException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

public abstract class CalendarImpl implements Calendar {
    private static final Logger LOG = LoggerFactory.getLogger(CalendarImpl.class);

    private CalendarEntity entity;
    protected ICalendarDAO dao;
    private Config config;
    private EventContainer eventContainer;

    public CalendarImpl(Config config, Annuaire annuaire, EventContainer eventContainer) {
        this.config = config;
        this.eventContainer = eventContainer;
        try {
            this.entity = (CalendarEntity) ((CalendarContext)annuaire.getRegistry(RegistryVariable.CONTEXT_BUSINESS))
                    .getContextVariable(ContextVariable.ENTITY);
            this.dao = (ICalendarDAO) ((CalendarContext)annuaire.getRegistry(RegistryVariable.CONTEXT_PERSISTENCE))
                    .getContextVariable(ContextVariable.DAO);
        } catch (InvalidClassException e) {
            LOG.error(e.getMessage());
        }

        if (entity.getName() == null)
            this.entity.setName(config.getProperty(Config.CALENDAR_NAME));
    }

    @Override
    public String getName() {
        return this.entity.getName();
    }

    @Override
    public Object process(Command command, EventDTO eventDTO) throws ObjectNotFoundException {
        Object reponse = null;

        switch (command) {
            case SYNC_EVENTS:
                synchronizeEvents();
                break;
            case ADD_EVENT:
                reponse = addEvent(eventDTO);
                break;
            case REMOVE_EVENT:
                removeEvent(eventDTO);
                break;
            case LIST_EVENTS:
                reponse = getInfo();
                break;
            case FIND_EVENT:
                reponse = findEvent(eventDTO);
                break;
            default:
                break;

        }

        /* We add this line for keep the consistence between the container and the mapping class */
//        entity.sync(getEventContainer().list());
        return reponse;
    }

    public Collection<Event> getEvents() {
        return this.entity.getEvent();
    }

    protected abstract Event addEvent(EventDTO eventDTO);

    protected abstract Event findEvent(EventDTO eventDTO) throws ObjectNotFoundException;

    protected abstract void removeEvent(EventDTO eventDTO) throws ObjectNotFoundException;

    protected abstract void synchronizeEvents();

    protected void setEvents(Collection<Event> events) {
        this.entity.setEvent(events);
    }

    /**
     * Méthode destinée à l'affichage
     *
     * @return une string formattée contentant toutes les infos des événements de l'calendar
     */
    protected abstract String getInfo();

    public String formatDate(Date d) {
        return new SimpleDateFormat(this.config.getProperty(Config.DATE_FORMAT)).format(d);
    }

    public Date parseDate(String s) {
        try {
            return new SimpleDateFormat(this.config.getProperty(Config.DATE_FORMAT)).parse(s);
        } catch (ParseException e) {
            LOG.error(e.getMessage());
        }
        return new Date();
    }

    public CalendarEntity getEntity() {
        return entity;
    }

    @Override
    public void start() {
        //CalendarImpl démarré. Objet d'accès aux données : fr.univlyon1.tiw.tiw1.calendar.dao.XMLCalendarDAO@95c083
        System.out.println("LIFE CYCLE: CalendarImpl démarré. Objet d'accès aux données : ".concat(this.toString()));
    }

    @Override
    public void stop() {
        System.out.println("LIFE CYCLE: CalendarImpl detenu. Objet d'accès aux données : ".concat(this.toString()));
    }

//    public void update(Observable o, Object arg) {
//        try {
//            this.entity = (CalendarEntity) ((CalendarContext) arg).getContextVariable(ContextVariable.ENTITY);
//            this.dao = (ICalendarDAO) ((CalendarContext) arg).getContextVariable(ContextVariable.DAO);
//        } catch (InvalidClassException e) {
//            LOG.error(e.getMessage());
//        }
//    }

    protected EventContainer getEventContainer() {
        return eventContainer;
    }
}