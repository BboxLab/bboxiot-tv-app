package fr.bouyguestelecom.tv.bboxiot.tvapp.test;

import android.test.AndroidTestCase;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.bouyguestelecom.tv.bboxiot.config.GenericDevice;
import fr.bouyguestelecom.tv.bboxiot.config.Protocols;
import fr.bouyguestelecom.tv.bboxiot.config.SupportedDevices;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.BluetoothSmartDevice;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.connection.ConnectionMode;
import fr.bouyguestelecom.tv.bboxiot.tvapp.R;
import fr.bouyguestelecom.tv.bboxiot.tvapp.ScanItemArrayAdapter;

/**
 * Test case for ScanItemArrayAdapter class
 *
 * @author Bertrand Martel
 */
public class ScanItemArrayAdapterTest extends AndroidTestCase {

    private ScanItemArrayAdapter mAdapter;

    private BluetoothSmartDevice device1;
    private BluetoothSmartDevice device2;

    public ScanItemArrayAdapterTest() {
        super();
    }

    protected void setUp() throws Exception {
        super.setUp();

        ArrayList<BluetoothSmartDevice> btDeviceList = new ArrayList<BluetoothSmartDevice>();

        String address = "address";
        String deviceUid = "deviceUid";
        List<String> deviceName = new ArrayList<>();
        deviceName.add("device1");
        deviceName.add("device2");
        byte[] manufacturerData = new byte[]{1, 2, 3, 4, 5};
        long time = 0;

        GenericDevice device = new GenericDevice(Protocols.UNDEFINED, SupportedDevices.UNDEFINED);

        device1 = new BluetoothSmartDevice(address, deviceUid, deviceName, manufacturerData, time, device, ConnectionMode.MODE_NONE);
        device2 = new BluetoothSmartDevice(address, deviceUid, deviceName, manufacturerData, time, device, ConnectionMode.MODE_NONE);

        btDeviceList.add(device1);
        btDeviceList.add(device2);

        mAdapter = new ScanItemArrayAdapter(getContext(), android.R.layout.simple_list_item_1, btDeviceList);
    }


    public void testGetItem() {

        for (int i = 0; i < 2; i++) {

            assertEquals("Device address does not match", device1.getDeviceAddress(),
                    mAdapter.getItem(i).getDeviceAddress());
            assertEquals("Device uid does not match", device1.getDeviceUuid(),
                    mAdapter.getItem(i).getDeviceUuid());
            assertEquals("Device name list size does not match", device1.getDeviceNameList().size(),
                    mAdapter.getItem(i).getDeviceNameList().size());
            assertEquals("Device manufacturer data filter size does not match", device1.getManufacturerData().length,
                    mAdapter.getItem(i).getManufacturerData().length);
            assertEquals("Device last activity time does not match", device1.getLastActivityTime(),
                    mAdapter.getItem(i).getLastActivityTime());
            assertEquals("Device mode does not match", device1.getDeviceMode(),
                    mAdapter.getItem(i).getDeviceMode());

            assertNotNull("Generic device is null", mAdapter.getItem(i).getGenericDevice());

            assertEquals("Supported device name does not match", device1.getGenericDevice().getSupportedDevice().ordinal(),
                    mAdapter.getItem(i).getGenericDevice().getSupportedDevice().ordinal());
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

        assertNotNull("View is null. ", view);
        assertNotNull("deviceUid TextView is null. ", deviceUid);
        assertNotNull("supported device TextView is null. ", supportedDevice);

        assertEquals("deviceUid doesn't match.", device1.getDeviceUuid(), deviceUid.getText());
        assertEquals("supported device name doesn't match.", device1.getGenericDevice().getSupportedDevice().toString(),
                supportedDevice.getText());
    }
}