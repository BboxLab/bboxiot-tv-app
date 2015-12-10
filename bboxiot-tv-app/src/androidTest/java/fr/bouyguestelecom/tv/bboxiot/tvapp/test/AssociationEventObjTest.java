package fr.bouyguestelecom.tv.bboxiot.tvapp.test;

import android.test.AndroidTestCase;

import java.util.Date;

import fr.bouyguestelecom.tv.bboxiot.tvapp.AssociationEventObj;

/**
 * @author Bertrand Martel
 */
public class AssociationEventObjTest extends AndroidTestCase {

    private AssociationEventObj eventObject = null;
    private String deviceUid = "deviceUidTest";
    private String eventStr = "eventTeset";

    public AssociationEventObjTest() {
        eventObject = new AssociationEventObj(deviceUid, eventStr);
        assertNotNull("AssociationEventObj is null", eventObject);
    }

    public void getDateTest() {
        assertNotNull("association event date is null", eventObject.getDate());
        assertTrue("date is invalid (too old)", eventObject.getDate().getTime() >= (new Date().getTime()));
    }

    public void getDeviceUidTest() {
        assertNotNull("deice uid is null", eventObject.getDeviceUid());
        assertTrue("device uid invalid", eventObject.getDeviceUid().equals(deviceUid));
    }

    public void getEventStrTest() {
        assertNotNull("event content is null", eventObject.getEventStr());
        assertTrue("event content invalid", eventObject.getEventStr().equals(eventStr));
    }
}
