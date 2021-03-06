package fr.bouyguestelecom.tv.bboxiot.tvapp.test;

import android.test.AndroidTestCase;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.bouyguestelecom.tv.bboxiot.config.GenericDevice;
import fr.bouyguestelecom.tv.bboxiot.config.Protocols;
import fr.bouyguestelecom.tv.bboxiot.config.SupportedDevices;
import fr.bouyguestelecom.tv.bboxiot.datamodel.SmartProperty;
import fr.bouyguestelecom.tv.bboxiot.datamodel.enums.Functions;
import fr.bouyguestelecom.tv.bboxiot.datamodel.enums.Properties;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.BluetoothSmartDevice;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.connection.BtAssociatedDevice;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.connection.ConnectionMode;
import fr.bouyguestelecom.tv.bboxiot.tvapp.ConnectionItemArrayAdapter;
import fr.bouyguestelecom.tv.bboxiot.tvapp.R;

/**
 * @author Bertrand Martel
 */
public class ConnectionItemArrayAdapterTest extends AndroidTestCase {

    private ConnectionItemArrayAdapter mAdapter;

    private BtAssociatedDevice connection1;
    private BtAssociatedDevice connection2;

    public ConnectionItemArrayAdapterTest() {
        super();
    }

    protected void setUp() throws Exception {
        super.setUp();

        ArrayList<BtAssociatedDevice> btDeviceList = new ArrayList<BtAssociatedDevice>();

        String address = "address";
        String deviceUid = "deviceUid";
        List<String> deviceName = new ArrayList<>();
        deviceName.add("device1");
        deviceName.add("device2");
        byte[] manufacturerData = new byte[]{1, 2, 3, 4, 5};
        long time = 0;
        String deviceMode = "deviceMode";

        GenericDevice genericDevice1 = new GenericDevice(Protocols.UNDEFINED, SupportedDevices.UNDEFINED);
        GenericDevice genericDevice2 = new GenericDevice(Protocols.UNDEFINED, SupportedDevices.UNDEFINED);

        BluetoothSmartDevice smartDevice1 = new BluetoothSmartDevice(address, deviceUid, deviceName, manufacturerData, time, genericDevice1, ConnectionMode.MODE_NONE);
        BluetoothSmartDevice smartDevice2 = new BluetoothSmartDevice(address, deviceUid, deviceName, manufacturerData, time, genericDevice2, ConnectionMode.MODE_NONE);

        connection1 = new BtAssociatedDevice(smartDevice1.getDeviceUuid(), false, false, false, smartDevice1, new HashMap<Functions, HashMap<Properties, SmartProperty>>(),0);
        connection2 = new BtAssociatedDevice(smartDevice2.getDeviceUuid(), false, false, false, smartDevice2, new HashMap<Functions, HashMap<Properties, SmartProperty>>(),0);

        btDeviceList.add(connection1);
        btDeviceList.add(connection2);

        mAdapter = new ConnectionItemArrayAdapter(getContext(), android.R.layout.simple_list_item_1, btDeviceList);
    }


    public void testGetItem() {

        for (int i = 0; i < 2; i++) {

            BtAssociatedDevice connection;

            if (i == 0) {
                connection = connection1;
            } else {
                connection = connection2;
            }
            assertEquals("Connection " + i + " | " + "Device address does not match", connection.getBtSmartDevice().getDeviceAddress(),
                    mAdapter.getItem(i).getBtSmartDevice().getDeviceAddress());
            assertEquals("Connection " + i + " | " + "Device uid does not match", connection.getDeviceUuid(),
                    mAdapter.getItem(i).getDeviceUuid());
            assertEquals("Connection " + i + " | " + "Device name list size does not match", connection.getBtSmartDevice().getDeviceNameList().size(),
                    mAdapter.getItem(i).getBtSmartDevice().getDeviceNameList().size());
            assertEquals("Connection " + i + " | " + "Device manufacturer data filter size does not match", connection.getBtSmartDevice().getManufacturerData().length,
                    mAdapter.getItem(i).getBtSmartDevice().getManufacturerData().length);
            assertEquals("Connection " + i + " | " + "Device last activity time does not match", connection.getBtSmartDevice().getLastActivityTime(),
                    mAdapter.getItem(i).getBtSmartDevice().getLastActivityTime());
            assertEquals("Connection " + i + " | " + "Device mode does not match", connection.getBtSmartDevice().getDeviceMode(),
                    mAdapter.getItem(i).getBtSmartDevice().getDeviceMode());

            assertNotNull("Connection " + i + " | " + "Generic device is null", mAdapter.getItem(i).getBtSmartDevice().getGenericDevice());

            assertEquals("Connection " + i + " | " + "Supported device name does not match", connection.getBtSmartDevice().getGenericDevice().getSupportedDevice().ordinal(),
                    mAdapter.getItem(i).getBtSmartDevice().getGenericDevice().getSupportedDevice().ordinal());
        }

    }

    public void testGetItemId() {
        assertEquals("Device ID dost not match", 0, mAdapter.getItemId(0));
    }

    public void testGetCount() {
        assertEquals("Incorrect device list size", 2, mAdapter.getCount());
    }

    public void testGetView() {
        View view = mAdapter.getView(0, null, null);

        TextView deviceUid = (TextView) view
                .findViewById(R.id.text1);

        TextView supportedDevice = (TextView) view
                .findViewById(R.id.text2);

        ImageView connectionStatus = (ImageView) view.findViewById(R.id.connection_state);

        assertNotNull("View is null. ", view);
        assertNotNull("deviceUid TextView is null. ", deviceUid);
        assertNotNull("supported device TextView is null. ", supportedDevice);
        assertNotNull("connection status ImageView is null. ", connectionStatus);

        assertEquals("deviceUid doesn't match.", connection1.getDeviceUuid(), deviceUid.getText());
        assertEquals("supported device name doesn't match.", connection1.getBtSmartDevice().getGenericDevice().getSupportedDevice().toString(),
                supportedDevice.getText());
    }
}
