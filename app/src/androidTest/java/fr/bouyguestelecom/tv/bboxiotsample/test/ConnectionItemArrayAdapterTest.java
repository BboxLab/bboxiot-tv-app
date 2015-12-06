package fr.bouyguestelecom.tv.bboxiotsample.test;

import android.test.AndroidTestCase;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.bmartel.android.dotti.R;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.BluetoothSmartDevice;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.config.GenericDevice;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.connection.BtConnection;
import fr.bouyguestelecom.tv.bboxiotsample.ConnectionItemArrayAdapter;

/**
 * @author Bertrand Martel
 */
public class ConnectionItemArrayAdapterTest extends AndroidTestCase {

    private ConnectionItemArrayAdapter mAdapter;

    private BtConnection connection1;
    private BtConnection connection2;

    public ConnectionItemArrayAdapterTest() {
        super();
    }

    protected void setUp() throws Exception {
        super.setUp();

        ArrayList<BtConnection> btDeviceList = new ArrayList<BtConnection>();

        String address = "address";
        String deviceUid = "deviceUid";
        List<String> deviceName = new ArrayList<>();
        deviceName.add("device1");
        deviceName.add("device2");
        byte[] manufacturerData = new byte[]{1, 2, 3, 4, 5};
        long time = 0;
        String deviceMode = "deviceMode";

        String protocol = "bluetooth";
        String manufacturerName1 = "manufacturer1";
        String productName1 = "productName1";
        String manufacturerName2 = "manufacturer2";
        String productName2 = "productName2";
        String smartBuilderClassName = "fr.bouyguestelecom.tv.bboxiot.fake";
        List<String> smartFunctionsList = new ArrayList<>();

        GenericDevice genericDevice1 = new GenericDevice(protocol, manufacturerName1, productName1, smartBuilderClassName, smartFunctionsList);
        GenericDevice genericDevice2 = new GenericDevice(protocol, manufacturerName2, productName2, smartBuilderClassName, smartFunctionsList);

        BluetoothSmartDevice smartDevice1 = new BluetoothSmartDevice(address, deviceUid, deviceName, manufacturerData, time, genericDevice1, deviceMode);
        BluetoothSmartDevice smartDevice2 = new BluetoothSmartDevice(address, deviceUid, deviceName, manufacturerData, time, genericDevice2, deviceMode);

        connection1 = new BtConnection(smartDevice1.getDeviceUuid(), false, false, false, smartDevice1);
        connection2 = new BtConnection(smartDevice2.getDeviceUuid(), false, false, false, smartDevice2);

        btDeviceList.add(connection1);
        btDeviceList.add(connection2);

        mAdapter = new ConnectionItemArrayAdapter(getContext(), android.R.layout.simple_list_item_1, btDeviceList);
    }


    public void testGetItem() {

        for (int i = 0; i < 2; i++) {

            BtConnection connection;

            if (i == 0) {
                connection = connection1;
            } else {
                connection = connection2;
            }
            assertEquals("Connection " + i + " | " + "Device address does not match", connection.getBluetoothDevice().getDeviceAddress(),
                    mAdapter.getItem(i).getBluetoothDevice().getDeviceAddress());
            assertEquals("Connection " + i + " | " + "Device uid does not match", connection.getDeviceUuid(),
                    mAdapter.getItem(i).getDeviceUuid());
            assertEquals("Connection " + i + " | " + "Device name list size does not match", connection.getBluetoothDevice().getDeviceNameList().size(),
                    mAdapter.getItem(i).getBluetoothDevice().getDeviceNameList().size());
            assertEquals("Connection " + i + " | " + "Device manufacturer data filter size does not match", connection.getBluetoothDevice().getManufacturerData().length,
                    mAdapter.getItem(i).getBluetoothDevice().getManufacturerData().length);
            assertEquals("Connection " + i + " | " + "Device last activity time does not match", connection.getBluetoothDevice().getLastActivityTime(),
                    mAdapter.getItem(i).getBluetoothDevice().getLastActivityTime());
            assertEquals("Connection " + i + " | " + "Device mode does not match", connection.getBluetoothDevice().getDeviceMode(),
                    mAdapter.getItem(i).getBluetoothDevice().getDeviceMode());

            assertNotNull("Connection " + i + " | " + "Generic device is null", mAdapter.getItem(i).getBluetoothDevice().getGenericDevice());

            assertEquals("Connection " + i + " | " + "Manufacturer name does not match", connection.getBluetoothDevice().getGenericDevice().getManufacturerName(),
                    mAdapter.getItem(i).getBluetoothDevice().getGenericDevice().getManufacturerName());
            assertEquals("Connection " + i + " | " + "ProductName name does not match", connection.getBluetoothDevice().getGenericDevice().getProductName(),
                    mAdapter.getItem(i).getBluetoothDevice().getGenericDevice().getProductName());

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

        TextView manufacturer = (TextView) view
                .findViewById(R.id.text2);

        TextView productName = (TextView) view
                .findViewById(R.id.text3);

        ImageView connectionStatus = (ImageView) view.findViewById(R.id.connection_state);

        assertNotNull("View is null. ", view);
        assertNotNull("deviceUid TextView is null. ", deviceUid);
        assertNotNull("manufacturer TextView is null. ", manufacturer);
        assertNotNull("productName TextView is null. ", productName);
        assertNotNull("connection status ImageView is null. ", connectionStatus);

        assertEquals("deviceUid doesn't match.", connection1.getDeviceUuid(), deviceUid.getText());
        assertEquals("manufacturer doesn't match.", connection1.getBluetoothDevice().getGenericDevice().getManufacturerName(),
                manufacturer.getText());
        assertEquals("productName doesn't match.", connection1.getBluetoothDevice().getGenericDevice().getProductName(),
                productName.getText());
    }
}
