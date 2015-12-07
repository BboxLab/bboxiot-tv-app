package fr.bouyguestelecom.tv.bboxiotsample.test;

import android.test.AndroidTestCase;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import fr.bmartel.android.dotti.R;
import fr.bouyguestelecom.tv.bboxiotsample.AssociationEventAdapter;
import fr.bouyguestelecom.tv.bboxiotsample.AssociationEventObj;

/**
 * @author Bertrand Martel
 */
public class AssociationEventAdapterTest extends AndroidTestCase {

    private AssociationEventAdapter mAdapter;

    private AssociationEventObj eventObject1;
    private AssociationEventObj eventObject2;

    public AssociationEventAdapterTest() {
        super();
    }

    protected void setUp() throws Exception {
        super.setUp();

        ArrayList<AssociationEventObj> associationEventList = new ArrayList<AssociationEventObj>();

        eventObject1 = new AssociationEventObj("deviceUidTest1", "eventContentTest1");
        eventObject2 = new AssociationEventObj("deviceUidTest1", "eventContentTest1");

        associationEventList.add(eventObject1);
        associationEventList.add(eventObject2);

        mAdapter = new AssociationEventAdapter(getContext(), android.R.layout.simple_list_item_1, associationEventList);
    }


    public void testGetItem() {

        assertEquals("Device uid does not match", eventObject1.getDeviceUid(),
                mAdapter.getItem(0).getDeviceUid());
        assertEquals("Event content does not match", eventObject1.getEventStr(),
                mAdapter.getItem(0).getEventStr());
        assertEquals("Event date does not match", eventObject1.getDate().getTime(),
                mAdapter.getItem(0).getDate().getTime());

        assertEquals("Device uid does not match", eventObject1.getDeviceUid(),
                mAdapter.getItem(1).getDeviceUid());
        assertEquals("Event content does not match", eventObject1.getEventStr(),
                mAdapter.getItem(1).getEventStr());
        assertEquals("Event date does not match", eventObject1.getDate().getTime(),
                mAdapter.getItem(1).getDate().getTime());
    }

    public void testGetItemId() {
        assertEquals("Device ID dost not match", 0, mAdapter.getItemId(0));
        assertEquals("Device ID dost not match", 1, mAdapter.getItemId(1));
    }

    public void testGetCount() {
        assertEquals("Incorrect association event list size", 2, mAdapter.getCount());
    }

    public void testGetView() {

        for (int i = 0; i < 2; i++) {

            AssociationEventObj associationObject = null;

            if (i == 0)
                associationObject = eventObject1;
            else
                associationObject = eventObject2;

            View view = mAdapter.getView(i, null, null);

            TextView date = (TextView) view
                    .findViewById(R.id.text1);

            TextView deviceUid = (TextView) view
                    .findViewById(R.id.text2);

            TextView eventStr = (TextView) view
                    .findViewById(R.id.text3);

            assertNotNull("View is null. ", view);
            assertNotNull("Date TextView is null. ", date);
            assertNotNull("deviceUid TextView is null. ", deviceUid);
            assertNotNull("eventStr TextView is null. ", eventStr);

            assertEquals("deviceUid doesn't match.", associationObject.getDeviceUid(), deviceUid.getText());

            assertEquals("event content doesn't match.", associationObject.getEventStr(),
                    eventStr.getText());
        }
    }
}
