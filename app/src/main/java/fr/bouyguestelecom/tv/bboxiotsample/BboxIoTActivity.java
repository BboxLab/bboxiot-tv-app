/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2015 Bertrand Martel
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bouyguestelecom.tv.bboxiotsample;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import fr.bmartel.android.dotti.R;
import fr.bouyguestelecom.tv.bboxiot.IBboxIotService;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.BluetoothSmartDevice;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.IBluetoothEventListener;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.connection.BtConnection;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.connection.ConnectionStatus;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.EventBuilder;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.IGenericEvent;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.IotEvent;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.enums.EventRegistration;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.enums.ScanningAction;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.impl.AssociationEvent;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.impl.BluetoothStateEvent;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.impl.ScanItemEvent;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.impl.ScanStatusChangeEvent;

/**
 * Dotti device management main activity
 *
 * @author Bertrand Martel
 */
public class BboxIoTActivity extends Activity {

    /**
     * debug tag
     */
    private String TAG = this.getClass().getName();

    private IBboxIotService bboxIotService = null;

    private Button btStateOnBtn = null;

    private Button btStateOffBtn = null;

    private Button btScanContinuous = null;

    private Button btScanPermanent = null;

    private Button btScanPeriodic = null;

    private Button btScanStop = null;

    private Button btDisassociateAll = null;

    private Button btClearScanList = null;

    private ListView scanningListview = null;

    private ListView associationListview = null;

    private ListView connectionEventListView = null;

    private AssociationEventAdapter connectionEventListAdapter = null;

    private ScanItemArrayAdapter scanningListAdapter = null;

    private ConnectionItemArrayAdapter associationListAdapter = null;

    private Map<String, BluetoothSmartDevice> scanningList = new HashMap<>();

    private Map<String, BtConnection> associationList = new HashMap<>();

    private Dialog currentDialog = null;
    private String currentDeviceUid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bboxiot_activity);

        //create intent
        final Intent intent = new Intent();
        intent.setComponent(new ComponentName("fr.bouyguestelecom.tv.bboxiot.main",
                "fr.bouyguestelecom.tv.bboxiot.main.IotService"));

        btStateOnBtn = (Button) findViewById(R.id.bluetooth_state_on);
        btStateOffBtn = (Button) findViewById(R.id.bluetooth_state_off);

        btScanContinuous = (Button) findViewById(R.id.bluetooth_continuous_scanning_start);
        btScanStop = (Button) findViewById(R.id.bluetooth_scan_stop);
        btClearScanList = (Button) findViewById(R.id.bluetooth_clear_scan_list);
        btScanPermanent = (Button) findViewById(R.id.bluetooth_permanent_scanning_start);
        btScanPeriodic = (Button) findViewById(R.id.bluetooth_periodic_scanning_start);

        btDisassociateAll = (Button) findViewById(R.id.bluetooth_disassociate_all);

        btDisassociateAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (bboxIotService != null) {

                    try {
                        boolean status = bboxIotService.getBluetoothManager().disassociateAll();

                        if (status) {
                            Log.i(TAG, "Disassociate all request success");
                            refreshAssociationList();
                        } else {
                            Log.i(TAG, "Disassociate all request failure");
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btClearScanList.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (bboxIotService != null) {

                    try {
                        bboxIotService.getBluetoothManager().clearScanningList();

                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              scanningListAdapter.clear();
                                              scanningListAdapter.notifyDataSetChanged();
                                          }
                                      }
                        );
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btScanPermanent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (bboxIotService != null) {

                        String request = EventBuilder.buildPermanentScan().toJsonString();

                        boolean status = bboxIotService.getBluetoothManager().setScanStatus(request);

                        if (status) {
                            Log.i(TAG, "Permanent scan request successfully engaged");
                        } else {
                            Log.i(TAG, "Permanent scan request failed");
                        }
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        btScanPeriodic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (bboxIotService != null) {

                        String request = EventBuilder.buildPeriodicScan(10, 50).toJsonString();

                        boolean status = bboxIotService.getBluetoothManager().setScanStatus(request);

                        if (status) {
                            Log.i(TAG, "Periodic scan request successfully engaged");
                        } else {
                            Log.i(TAG, "Periodic scan request failed");
                        }
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        btScanContinuous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (bboxIotService != null) {

                        String request = EventBuilder.buildContinuousScan(10).toJsonString();

                        boolean status = bboxIotService.getBluetoothManager().setScanStatus(request);

                        if (status) {
                            Log.i(TAG, "Continuous scan request successfully engaged");
                        } else {
                            Log.i(TAG, "Continuous scan request failed");
                        }
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        btScanStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (bboxIotService != null) {

                        String request = EventBuilder.buildStopScan().toJsonString();

                        boolean status = bboxIotService.getBluetoothManager().setScanStatus(request);

                        if (status) {
                            Log.i(TAG, "Stop scan request successfully engaged");
                        } else {
                            Log.i(TAG, "Stop scan request failed");
                        }

                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        btStateOnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Log.i(TAG, "bluetooth state : " + bboxIotService.getBluetoothManager().getBluetoothState());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                try {
                    if (bboxIotService != null && !bboxIotService.getBluetoothManager().getBluetoothState()) {

                        boolean status = bboxIotService.getBluetoothManager().setBluetoothState(true);

                        if (status) {
                            Log.i(TAG, "Bluetooth set ON request has been sent successfully");
                        } else {
                            Log.i(TAG, "Bluetooth set ON request has failed");
                        }

                    } else {
                        Log.i(TAG, "Error service is not defined or bluetooth state is already ON");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        btStateOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    try {
                        Log.i(TAG, "bluetooth state : " + bboxIotService.getBluetoothManager().getBluetoothState());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    if (bboxIotService != null && bboxIotService.getBluetoothManager().getBluetoothState()) {

                        boolean status = bboxIotService.getBluetoothManager().setBluetoothState(false);

                        if (status) {
                            Log.i(TAG, "Bluetooth set OFF request has been sent successfully");
                        } else {
                            Log.i(TAG, "Bluetooth set OFF request has failed");
                        }

                    } else {
                        Log.i(TAG, "Error service is not defined or bluetooth state is already OFF");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        btStateOnBtn.setEnabled(false);
        btStateOffBtn.setEnabled(false);

        initializeScanningList();
        initializeAssociationList();
        initializeConnectionEventList();

        ServiceConnection mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {

                Log.i(TAG, "Service IoT has connected");

                //get api wrapper
                bboxIotService = IBboxIotService.Stub.asInterface(service);

                try {

                    refreshScanningList();

                    refreshAssociationList();

                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                try {
                    try {
                        Log.i(TAG, "bluetooth state : " + bboxIotService.getBluetoothManager().getBluetoothState());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    if (bboxIotService.getBluetoothManager().getBluetoothState()) {

                        btStateOnBtn.setEnabled(false);
                        btStateOffBtn.setEnabled(true);
                        btStateOffBtn.requestFocus();

                    } else {

                        btStateOnBtn.setEnabled(true);
                        btStateOffBtn.setEnabled(false);
                        btStateOnBtn.requestFocus();

                    }

                    if (bboxIotService.getBluetoothManager().isScanning()) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btScanContinuous.setEnabled(false);
                                btScanStop.setEnabled(true);
                            }
                        });

                    } else {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btScanContinuous.setEnabled(true);
                                btScanStop.setEnabled(false);
                            }
                        });
                    }

                    Set<EventRegistration> registrationSet = new HashSet<>();
                    registrationSet.add(EventRegistration.REGISTRATION_BLUETOOTH_STATE);
                    registrationSet.add(EventRegistration.REGISTRATION_SCANNING);
                    registrationSet.add(EventRegistration.REGISTRATION_CONNECTION);

                    bboxIotService.getBluetoothManager().registerEvents(EventBuilder.buildRegistration(registrationSet).toJsonString(), new IBluetoothEventListener.Stub() {

                        public void onEventReceived(int type, int topic, String event) {

                            Log.i(TAG, "event received => type : " + type + " | topic : " + topic + " | event content : " + event);

                            IGenericEvent genericEvent = IotEvent.parse(event);

                            if (genericEvent != null) {

                                if (genericEvent instanceof BluetoothStateEvent) {

                                    BluetoothStateEvent btEvent = (BluetoothStateEvent) genericEvent;

                                    if (btEvent.getBluetoothState()) {

                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {

                                                btStateOnBtn.setEnabled(false);
                                                btStateOffBtn.setEnabled(true);
                                                btStateOffBtn.requestFocus();
                                            }
                                        });

                                    } else {

                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {

                                                btStateOnBtn.setEnabled(true);
                                                btStateOffBtn.setEnabled(false);
                                                btStateOnBtn.requestFocus();
                                            }
                                        });

                                    }
                                } else if (genericEvent instanceof ScanStatusChangeEvent) {

                                    ScanStatusChangeEvent btEvent = (ScanStatusChangeEvent) genericEvent;

                                    System.out.println(btEvent.getAction().toString());

                                    if (btEvent.getAction() == ScanningAction.SCANNING_ACTION_START) {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                btScanContinuous.setEnabled(false);
                                                btScanPermanent.setEnabled(false);
                                                btScanPeriodic.setEnabled(false);
                                                btScanStop.setEnabled(true);
                                            }
                                        });

                                    } else if (btEvent.getAction() == ScanningAction.SCANNING_ACTION_STOP) {

                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {

                                                btScanContinuous.setEnabled(true);
                                                btScanPermanent.setEnabled(true);
                                                btScanPeriodic.setEnabled(true);
                                                btScanStop.setEnabled(false);
                                            }
                                        });
                                    }

                                } else if (genericEvent instanceof ScanItemEvent) {

                                    final ScanItemEvent btEvent = (ScanItemEvent) genericEvent;

                                    System.out.println(btEvent.getItem().toJson().toString());

                                    System.out.println("updating adapter");

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            if (scanningListAdapter != null) {
                                                scanningListAdapter.add(btEvent.getItem());
                                                scanningListAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                                } else if (genericEvent instanceof AssociationEvent) {

                                    final AssociationEvent btEvent = (AssociationEvent) genericEvent;

                                    System.out.println("received association event : " + btEvent.getState().toString());

                                    if (connectionEventListAdapter != null) {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                if (connectionEventListAdapter.getCount() > 10) {

                                                    for (int i = connectionEventListAdapter.getCount() - 1; i >= 10; i--) {
                                                        connectionEventListAdapter.getDeviceList().remove(i);
                                                    }
                                                }
                                                connectionEventListAdapter.insert(new AssociationEventObj(btEvent.getConnection().getDeviceUuid(), btEvent.getState().toString()), 0);
                                                connectionEventListAdapter.notifyDataSetChanged();

                                            }
                                        });
                                    }

                                    try {
                                        switch (btEvent.getState()) {

                                            case ASSOCIATION_COMPLETE: {

                                                if (btEvent.getConnection().getDeviceUuid().equals(currentDeviceUid) && currentDialog != null) {
                                                    currentDialog.dismiss();
                                                    currentDialog = null;
                                                }

                                                refreshAssociationList();
                                                //refresh scanning list because device entry is no longer present
                                                refreshScanningList();

                                                break;
                                            }
                                            case CONNECTED: {

                                                refreshAssociationList();

                                                if (currentDeviceUid.equals(btEvent.getConnection().getDeviceUuid()) && currentDialog != null) {

                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            System.out.println("modif1");
                                                            ImageView view = (ImageView) currentDialog.findViewById(R.id.device_connected_value);
                                                            if (view != null)
                                                                view.setImageResource(R.drawable.green_circle);

                                                            TextView buttonConnect = (TextView) currentDialog.findViewById(R.id.button_connect);
                                                            if (buttonConnect != null)
                                                                buttonConnect.setEnabled(false);

                                                            TextView buttonDisconnect = (TextView) currentDialog.findViewById(R.id.button_disconnect);
                                                            if (buttonDisconnect != null)
                                                                buttonDisconnect.setEnabled(true);
                                                        }
                                                    });

                                                }
                                                break;
                                            }
                                            case DISCONNECTED: {

                                                refreshAssociationList();

                                                if (currentDeviceUid.equals(btEvent.getConnection().getDeviceUuid()) && currentDialog != null) {

                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            System.out.println("modif2");
                                                            ImageView view = (ImageView) currentDialog.findViewById(R.id.device_connected_value);
                                                            if (view != null)
                                                                view.setImageResource(R.drawable.red_circle);

                                                            TextView buttonConnect = (TextView) currentDialog.findViewById(R.id.button_connect);
                                                            if (buttonConnect != null)
                                                                buttonConnect.setEnabled(true);

                                                            TextView buttonDisconnect = (TextView) currentDialog.findViewById(R.id.button_disconnect);
                                                            if (buttonDisconnect != null)
                                                                buttonDisconnect.setEnabled(false);
                                                        }
                                                    });
                                                }

                                                break;
                                            }
                                            case CONNECTION_ERROR: {

                                                break;
                                            }
                                        }
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        }

                    });
                } catch (
                        RemoteException e
                        )

                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

                Log.i(TAG, "Service IoT has disconnected");

            }
        };

        boolean isBound = getApplicationContext().bindService(intent,
                mServiceConnection,
                Context.BIND_AUTO_CREATE);

    }

    private void refreshAssociationList() throws RemoteException {

        Log.i(TAG, "refresh association list");

        associationList = IotEvent.parseAssociationList(bboxIotService.getBluetoothManager().getAssociationList());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (associationListAdapter != null) {

                    associationListAdapter.clear();

                    Iterator it = associationList.entrySet().iterator();

                    while (it.hasNext()) {
                        Map.Entry<String, BtConnection> pair = (Map.Entry) it.next();
                        associationListAdapter.add(pair.getValue());
                    }
                    associationListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void refreshScanningList() throws RemoteException {

        scanningList = IotEvent.parseScanningList(bboxIotService.getBluetoothManager().getScanningList());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (scanningListAdapter != null) {

                    scanningListAdapter.clear();

                    Iterator it = scanningList.entrySet().iterator();

                    while (it.hasNext()) {
                        Map.Entry<String, BluetoothSmartDevice> pair = (Map.Entry) it.next();
                        scanningListAdapter.add(pair.getValue());
                    }
                    scanningListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void initializeAssociationList() {

        associationListview = (ListView) findViewById(R.id.connection_list_view);

        associationListAdapter = new ConnectionItemArrayAdapter(this,
                android.R.layout.simple_list_item_1, new ArrayList<BtConnection>());

        associationListview.setAdapter(associationListAdapter);

        associationListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                final BtConnection item = (BtConnection) parent.getItemAtPosition(position);


                final Dialog dialog = new Dialog(BboxIoTActivity.this);

                currentDialog = dialog;
                currentDeviceUid = item.getDeviceUuid();

                dialog.setContentView(R.layout.connection_item);
                dialog.setTitle("Device " + item.getDeviceUuid());

                Button buttonBack = (Button) dialog.findViewById(R.id.button_back);
                Button buttonConnect = (Button) dialog.findViewById(R.id.button_connect);
                Button buttonDisconnect = (Button) dialog.findViewById(R.id.button_disconnect);
                Button buttonDisassociate = (Button) dialog.findViewById(R.id.button_disassociate);

                TextView productName = (TextView) dialog.findViewById(R.id.product_name_value);
                TextView manufacturerName = (TextView) dialog.findViewById(R.id.manufacturer_name_value);

                productName.setText(item.getBluetoothDevice().getGenericDevice().getProductName());
                manufacturerName.setText(item.getBluetoothDevice().getGenericDevice().getManufacturerName());

                ImageView connectedValue = (ImageView) dialog.findViewById(R.id.device_connected_value);

                TextView firstConnection = (TextView) dialog.findViewById(R.id.device_is_first_connected_value);
                firstConnection.setText("" + item.isFirstTimeConnected());

                TextView busy = (TextView) dialog.findViewById(R.id.device_is_busy_value);
                busy.setText("" + item.isBusy());

                TextView deviceUidVal = (TextView) dialog.findViewById(R.id.device_uid_value);
                deviceUidVal.setText(item.getDeviceUuid());

                TextView deviceNameList = (TextView) dialog.findViewById(R.id.device_name_list_value);

                TextView deviceUp = (TextView) dialog.findViewById(R.id.device_up_value);
                deviceUp.setText("" + item.getBluetoothDevice().isUp());

                TextView manufacturerDataFilter = (TextView) dialog.findViewById(R.id.manufacturer_data_filter_value);

                String manufacturerDataFilterVals = "[";
                for (int i = 0; i < item.getBluetoothDevice().getManufacturerData().length; i++) {
                    manufacturerDataFilterVals += item.getBluetoothDevice().getManufacturerData()[i] + " , ";
                }
                if (!manufacturerDataFilterVals.equals("[")) {
                    manufacturerDataFilterVals = manufacturerDataFilterVals.substring(0, manufacturerDataFilterVals.length() - 3) +
                            " ]";
                    manufacturerDataFilter.setText(manufacturerDataFilterVals);
                } else {
                    manufacturerDataFilter.setText("[ ]");
                }

                TextView lastActivityDate = (TextView) dialog.findViewById(R.id.last_activity_date_value);
                Date lastDate = new Date(item.getBluetoothDevice().getLastActivityTime());
                DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                lastActivityDate.setText(df.format(lastDate).toString());

                TextView deviceMode = (TextView) dialog.findViewById(R.id.device_mode_value);
                deviceMode.setText(item.getBluetoothDevice().getDeviceMode());

                TextView deviceAddress = (TextView) dialog.findViewById(R.id.device_address_value);
                deviceAddress.setText(item.getBluetoothDevice().getDeviceAddress());

                String deviceNames = "";
                for (int i = 0; i < item.getBluetoothDevice().getDeviceNameList().size(); i++) {
                    deviceNames += "\"" + item.getBluetoothDevice().getDeviceNameList().get(i) + "\"" + ",";
                }
                if (!deviceNames.equals("")) {
                    deviceNameList.setText(deviceNames.substring(0, deviceNames.length() - 1));
                }

                if (item.isConnected()) {
                    connectedValue.setImageResource(R.drawable.green_circle);
                    buttonConnect.setEnabled(false);
                    buttonDisconnect.setEnabled(true);
                } else {
                    connectedValue.setImageResource(R.drawable.red_circle);
                    buttonConnect.setEnabled(true);
                    buttonDisconnect.setEnabled(false);
                }

                buttonBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                buttonDisassociate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Log.i(TAG, "disassociating device " + item.getDeviceUuid());

                        if (bboxIotService != null) {
                            try {
                                boolean status = bboxIotService.getBluetoothManager().disassociateDevice(item.getDeviceUuid());

                                if (status) {
                                    Log.i(TAG, "Disassociate request successfully initiated");
                                    refreshAssociationList();
                                    dialog.dismiss();
                                } else
                                    Log.i(TAG, "Disassociate request failure");

                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                buttonConnect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Log.i(TAG, "connecting device " + item.getDeviceUuid());

                        if (bboxIotService != null) {
                            try {
                                int status = bboxIotService.getBluetoothManager().connect(item.getDeviceUuid(), 2, true);

                                switch (ConnectionStatus.getStatus(status)) {
                                    case CONNECTION_SUCCESS: {
                                        Log.i(TAG, "Connection initiated");
                                        break;
                                    }
                                    case CONNECTION_FAILURE: {
                                        Log.i(TAG, "Connection failure");
                                        break;
                                    }
                                    case CONNECTION_WAITING: {
                                        Log.i(TAG, "Connection waiting");
                                        break;
                                    }
                                }

                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                buttonDisconnect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Log.i(TAG, "disconnecting device " + item.getDeviceUuid());

                        if (bboxIotService != null) {

                            try {

                                boolean status = bboxIotService.getBluetoothManager().disconnect(item.getDeviceUuid());

                                if (status)
                                    Log.i(TAG, "Disconnection successfully initiated");
                                else
                                    Log.i(TAG, "Disconnection request failure");

                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                dialog.show();
            }

        });
    }

    private void initializeConnectionEventList() {

        connectionEventListView = (ListView) findViewById(R.id.connection_event_list_view);

        connectionEventListAdapter = new AssociationEventAdapter(this,
                android.R.layout.simple_list_item_1, new ArrayList<AssociationEventObj>());

        connectionEventListView.setAdapter(connectionEventListAdapter);
    }

    private void initializeScanningList() {

        scanningListview = (ListView) findViewById(R.id.scanning_list_view);

        scanningListAdapter = new ScanItemArrayAdapter(this,
                android.R.layout.simple_list_item_1, new ArrayList<BluetoothSmartDevice>());

        scanningListview.setAdapter(scanningListAdapter);

        scanningListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                final BluetoothSmartDevice item = (BluetoothSmartDevice) parent.getItemAtPosition(position);

                final Dialog dialog = new Dialog(BboxIoTActivity.this);
                dialog.setContentView(R.layout.scanning_item);
                dialog.setTitle("Device " + item.getDeviceUuid());

                Button buttonBack = (Button) dialog.findViewById(R.id.button_back);
                Button buttonAssociate = (Button) dialog.findViewById(R.id.button_associate);

                TextView productName = (TextView) dialog.findViewById(R.id.product_name_value);
                TextView manufacturerName = (TextView) dialog.findViewById(R.id.manufacturer_name_value);

                productName.setText(item.getGenericDevice().getProductName());
                manufacturerName.setText(item.getGenericDevice().getManufacturerName());

                TextView deviceUidVal = (TextView) dialog.findViewById(R.id.device_uid_value);
                deviceUidVal.setText(item.getDeviceUuid());

                TextView deviceNameList = (TextView) dialog.findViewById(R.id.device_name_list_value);

                TextView deviceUp = (TextView) dialog.findViewById(R.id.device_up_value);
                deviceUp.setText("" + item.isUp());

                TextView manufacturerDataFilter = (TextView) dialog.findViewById(R.id.manufacturer_data_filter_value);

                String manufacturerDataFilterVals = "[";
                for (int i = 0; i < item.getManufacturerData().length; i++) {
                    manufacturerDataFilterVals += item.getManufacturerData()[i] + " , ";
                }
                if (!manufacturerDataFilterVals.equals("[")) {
                    manufacturerDataFilterVals = manufacturerDataFilterVals.substring(0, manufacturerDataFilterVals.length() - 3) +
                            " ]";
                    manufacturerDataFilter.setText(manufacturerDataFilterVals);
                } else {
                    manufacturerDataFilter.setText("[ ]");
                }

                TextView lastActivityDate = (TextView) dialog.findViewById(R.id.last_activity_date_value);
                Date lastDate = new Date(item.getLastActivityTime());
                DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                lastActivityDate.setText(df.format(lastDate).toString());

                TextView deviceMode = (TextView) dialog.findViewById(R.id.device_mode_value);
                deviceMode.setText(item.getDeviceMode());

                TextView deviceAddress = (TextView) dialog.findViewById(R.id.device_address_value);
                deviceAddress.setText(item.getDeviceAddress());

                String deviceNames = "";
                for (int i = 0; i < item.getDeviceNameList().size(); i++) {
                    deviceNames += "\"" + item.getDeviceNameList().get(i) + "\"" + ",";
                }
                if (!deviceNames.equals("")) {
                    deviceNameList.setText(deviceNames.substring(0, deviceNames.length() - 1));
                }

                buttonBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                buttonAssociate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Log.i(TAG, "associating device " + item.getDeviceUuid());

                        if (bboxIotService != null) {
                            try {

                                currentDialog = dialog;
                                currentDeviceUid = item.getDeviceUuid();

                                boolean status = bboxIotService.getBluetoothManager().associateDevice(item.getDeviceUuid());

                                if (status) {
                                    Log.i(TAG, "Association successfully initiated");
                                } else
                                    Log.i(TAG, "Association request failure");

                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                dialog.show();
            }

        });
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
