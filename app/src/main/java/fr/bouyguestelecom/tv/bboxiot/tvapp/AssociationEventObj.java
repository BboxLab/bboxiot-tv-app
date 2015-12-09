package fr.bouyguestelecom.tv.bboxiot.tvapp;

import java.util.Date;

/**
 * @author Bertrand Martel
 */
public class AssociationEventObj {

    private String deviceUid = "";

    private String eventStr = "";

    private Date date = new Date();

    public AssociationEventObj(String deviceUid, String eventStr) {
        this.deviceUid = deviceUid;
        this.eventStr = eventStr;
    }

    public Date getDate() {
        return date;
    }

    public String getDeviceUid() {
        return deviceUid;
    }

    public String getEventStr() {
        return eventStr;
    }
}
