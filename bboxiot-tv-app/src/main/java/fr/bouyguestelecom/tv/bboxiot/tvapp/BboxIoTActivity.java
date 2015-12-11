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
package fr.bouyguestelecom.tv.bboxiot.tvapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import fr.bouyguestelecom.tv.bboxiot.IBboxIotService;
import fr.bouyguestelecom.tv.bboxiot.datamodel.SmartProperty;
import fr.bouyguestelecom.tv.bboxiot.datamodel.enums.Functions;
import fr.bouyguestelecom.tv.bboxiot.datamodel.enums.Properties;
import fr.bouyguestelecom.tv.bboxiot.events.EventBuilder;
import fr.bouyguestelecom.tv.bboxiot.events.IGenericEvent;
import fr.bouyguestelecom.tv.bboxiot.events.IotEvent;
import fr.bouyguestelecom.tv.bboxiot.events.enums.EventSubscription;
import fr.bouyguestelecom.tv.bboxiot.events.enums.ScanningAction;
import fr.bouyguestelecom.tv.bboxiot.events.impl.BluetoothStateEvent;
import fr.bouyguestelecom.tv.bboxiot.events.impl.ConnectionEvent;
import fr.bouyguestelecom.tv.bboxiot.events.impl.ScanItemEvent;
import fr.bouyguestelecom.tv.bboxiot.events.impl.ScanStatusChangeEvent;
import fr.bouyguestelecom.tv.bboxiot.events.inter.IPropertyIncomingEvent;
import fr.bouyguestelecom.tv.bboxiot.events.inter.IPropertyResponseEvent;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.BluetoothSmartDevice;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.IBluetoothEventListener;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.connection.BtConnection;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.connection.ConnectionStatus;

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

    private List<SmartProperty> propertyList = new ArrayList<>();

    private PropertyAdapter propertyAdapter = null;

    private Dialog currentDialog = null;
    private String currentDeviceUid = "";

    /**
     * command task scheduler
     */
    private ScheduledExecutorService commandScheduler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bboxiot_activity);

        commandScheduler = Executors.newSingleThreadScheduledExecutor();

        Intent intent = new Intent();
        intent.setComponent(ComponentName.unflattenFromString("fr.bouyguestelecom.tv.bboxiot.main/.IotService"));

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

                    } else {

                        btStateOnBtn.setEnabled(true);
                        btStateOffBtn.setEnabled(false);

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

                    Set<EventSubscription> registrationSet = new HashSet<>();
                    registrationSet.add(EventSubscription.BLUETOOTH_STATE);
                    registrationSet.add(EventSubscription.SCANNING);
                    registrationSet.add(EventSubscription.CONNECTION);
                    registrationSet.add(EventSubscription.PROPERTIES);

                    bboxIotService.getBluetoothManager().subscribe(EventBuilder.buildSubscription(registrationSet).toJsonString(), new IBluetoothEventListener.Stub() {

                        public void onEventReceived(final int type, final int topic, final String event) {

                            commandScheduler.execute(new Runnable() {
                                @Override
                                public void run() {


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
                                                    }
                                                });

                                            } else {

                                                runOnUiThread(new Runnable() {

                                                    @Override
                                                    public void run() {

                                                        btStateOnBtn.setEnabled(true);
                                                        btStateOffBtn.setEnabled(false);
                                                    }
                                                });

                                            }
                                        } else if (genericEvent instanceof ScanStatusChangeEvent) {

                                            ScanStatusChangeEvent btEvent = (ScanStatusChangeEvent) genericEvent;

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

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    if (scanningListAdapter != null) {
                                                        scanningListAdapter.add(btEvent.getItem());
                                                        scanningListAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                            });

                                        } else if (genericEvent instanceof IPropertyResponseEvent) {

                                            Log.i(TAG, "received property response event");

                                            final IPropertyResponseEvent btEvent = (IPropertyResponseEvent) genericEvent;

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    if (connectionEventListAdapter.getCount() > 10) {

                                                        for (int i = connectionEventListAdapter.getCount() - 1; i >= 10; i--) {
                                                            connectionEventListAdapter.getDeviceList().remove(i);
                                                        }
                                                    }

                                                    connectionEventListAdapter.insert(new AssociationEventObj(btEvent.getDeviceUid(), btEvent.getProperty().getFunction() + " " + btEvent.getActionType().toString() + " " + btEvent.getStatus().toString() + " " + btEvent.getProperty().getValue()), 0);
                                                    connectionEventListAdapter.notifyDataSetChanged();
                                                }
                                            });

                                            associationList.get(btEvent.getDeviceUid()).getDeviceFunctions().get(btEvent.getProperty().getFunction()).put(btEvent.getProperty().getProperty(), btEvent.getProperty());

                                            for (int i = 0; i < propertyList.size(); i++) {

                                                if (propertyList.get(i).getDeviceUid().equals(btEvent.getDeviceUid()) &&
                                                        propertyList.get(i).getFunction() == btEvent.getProperty().getFunction() &&
                                                        propertyList.get(i).getProperty() == btEvent.getProperty().getProperty()) {

                                                    propertyList.set(i, btEvent.getProperty());

                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (propertyAdapter != null) {
                                                                propertyAdapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                    });
                                                }
                                            }

                                        } else if (genericEvent instanceof IPropertyIncomingEvent) {

                                            Log.i(TAG, "received property event");

                                            final IPropertyIncomingEvent btEvent = (IPropertyIncomingEvent) genericEvent;

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    if (connectionEventListAdapter.getCount() > 10) {

                                                        for (int i = connectionEventListAdapter.getCount() - 1; i >= 10; i--) {
                                                            connectionEventListAdapter.getDeviceList().remove(i);
                                                        }
                                                    }

                                                    connectionEventListAdapter.insert(new AssociationEventObj(btEvent.getDeviceUid(), btEvent.getProperty().getFunction().toString() + " " + btEvent.getProperty().getProperty().toString() + " " + btEvent.getProperty().getValue().toString()), 0);
                                                    connectionEventListAdapter.notifyDataSetChanged();
                                                }
                                            });

                                            associationList.get(btEvent.getDeviceUid()).getDeviceFunctions().get(btEvent.getProperty().getFunction()).put(btEvent.getProperty().getProperty(), btEvent.getProperty());

                                            for (int i = 0; i < propertyList.size(); i++) {

                                                if (propertyList.get(i).getDeviceUid().equals(btEvent.getDeviceUid()) &&
                                                        propertyList.get(i).getFunction() == btEvent.getProperty().getFunction() &&
                                                        propertyList.get(i).getProperty() == btEvent.getProperty().getProperty()) {

                                                    propertyList.set(i, btEvent.getProperty());

                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (propertyAdapter != null) {
                                                                propertyAdapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                    });
                                                }
                                            }

                                        } else if (genericEvent instanceof ConnectionEvent) {

                                            final ConnectionEvent btEvent = (ConnectionEvent) genericEvent;

                                            Log.i(TAG, "received association event : " + btEvent.getState().toString());

                                            if (connectionEventListAdapter != null && btEvent.getConnection() != null) {

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
                                                    case DEVICE_NOT_FOUND: {

                                                        refreshAssociationList();

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

        associationList = IotEvent.parseAssociationList(bboxIotService.getBluetoothManager().getAssociationList()).getList();

        Iterator it = associationList.entrySet().iterator();

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

        scanningList = IotEvent.parseScanningList(bboxIotService.getBluetoothManager().getScanningList()).getList();

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

    public static int getScreenWidth(Activity context) {
        Display display = context.getWindowManager().getDefaultDisplay();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            return size.x;
        }
        return display.getWidth();
    }

    public static int getScreenHeight(Activity context) {
        Display display = context.getWindowManager().getDefaultDisplay();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            return size.y;
        }
        return display.getHeight();
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

                dialog.getWindow().setLayout(getScreenWidth(BboxIoTActivity.this) / 2
                        , LinearLayout.LayoutParams.MATCH_PARENT);
                dialog.setTitle("Device " + item.getDeviceUuid());

                Button buttonBack = (Button) dialog.findViewById(R.id.button_back);
                Button buttonConnect = (Button) dialog.findViewById(R.id.button_connect);
                Button buttonDisconnect = (Button) dialog.findViewById(R.id.button_disconnect);
                Button buttonDisassociate = (Button) dialog.findViewById(R.id.button_disassociate);

                TextView supportedDevice = (TextView) dialog.findViewById(R.id.supported_device_name_name_value);

                ListView propertiesList = (ListView) dialog.findViewById(R.id.properties_list_view);

                TableRow switchStateRow = (TableRow) dialog.findViewById(R.id.properties_on_off_row);
                TableRow colorRow = (TableRow) dialog.findViewById(R.id.properties_color_row);
                SeekBar intensityBar = (SeekBar) dialog.findViewById(R.id.intensity_seekbar);
                SeekBar freqMeasurementSeekbar = (SeekBar) dialog.findViewById(R.id.frequency_measurement_seekbar);

                propertyList = new ArrayList<SmartProperty>();

                Iterator it = item.getDeviceFunctions().entrySet().iterator();

                while (it.hasNext()) {

                    Map.Entry<Functions, HashMap<Properties, SmartProperty>> pair = (Map.Entry) it.next();

                    Iterator it2 = pair.getValue().entrySet().iterator();

                    while (it2.hasNext()) {

                        Map.Entry<Properties, SmartProperty> pair2 = (Map.Entry) it2.next();

                        if (pair.getKey() == Functions.SWITCH && pair2.getValue().getProperty() == Properties.ONOFF) {
                            switchStateRow.setVisibility(View.VISIBLE);
                        }
                        if (pair.getKey() == Functions.RGB_LED && pair2.getValue().getProperty() == Properties.COLOR) {
                            colorRow.setVisibility(View.VISIBLE);
                        }
                        if (pair.getKey() == Functions.RGB_LED && pair2.getValue().getProperty() == Properties.INTENSITY) {
                            intensityBar.setVisibility(View.VISIBLE);
                        }
                        if (pair.getKey() == Functions.SMART_METER && pair2.getValue().getProperty() == Properties.FREQUENCY_MEASUREMENT) {
                            freqMeasurementSeekbar.setVisibility(View.VISIBLE);
                        }
                        propertyList.add(pair2.getValue());
                    }
                }

                if (switchStateRow.getVisibility() == View.VISIBLE) {

                    Button onButton = (Button) dialog.findViewById(R.id.switch_state_on);
                    Button offButton = (Button) dialog.findViewById(R.id.switch_state_off);

                    onButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (bboxIotService != null) {
                                try {

                                    String pushRequest = EventBuilder.buildPushRequest(item.getDeviceFunctions().get(Functions.SWITCH).get(Properties.ONOFF), true);

                                    boolean status = bboxIotService.getBluetoothManager().pushValue(pushRequest);

                                    if (!status)
                                        Log.e(TAG, "push request has failed");
                                    else
                                        Log.e(TAG, "push request sent");

                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    offButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (bboxIotService != null) {
                                try {

                                    String pushRequest = EventBuilder.buildPushRequest(item.getDeviceFunctions().get(Functions.SWITCH).get(Properties.ONOFF), false);

                                    boolean status = bboxIotService.getBluetoothManager().pushValue(pushRequest);

                                    if (!status)
                                        Log.e(TAG, "push request has failed");
                                    else
                                        Log.e(TAG, "push request sent");

                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
                if (colorRow.getVisibility() == View.VISIBLE) {

                    Button redButton = (Button) dialog.findViewById(R.id.color_red);
                    Button greenButton = (Button) dialog.findViewById(R.id.color_green);
                    Button blueButton = (Button) dialog.findViewById(R.id.color_blue);
                    Button whiteButton = (Button) dialog.findViewById(R.id.color_white);

                    redButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setColor(item, Color.RED);
                        }
                    });

                    greenButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setColor(item, Color.GREEN);
                        }
                    });

                    blueButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setColor(item, Color.BLUE);
                        }
                    });

                    whiteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setColor(item, Color.WHITE);
                        }
                    });
                }

                if (freqMeasurementSeekbar.getVisibility() == View.VISIBLE) {

                    freqMeasurementSeekbar.setProgress(item.getDeviceFunctions().get(Functions.SMART_METER).get(Properties.FREQUENCY_MEASUREMENT).getIntValue());

                    freqMeasurementSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                            if (bboxIotService != null) {
                                try {

                                    String pushRequest = EventBuilder.buildPushRequest(item.getDeviceFunctions().get(Functions.SMART_METER).get(Properties.FREQUENCY_MEASUREMENT), progress);

                                    boolean status = bboxIotService.getBluetoothManager().pushValue(pushRequest);

                                    if (!status)
                                        Log.e(TAG, "push request has failed");
                                    else
                                        Log.e(TAG, "push request sent");

                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                }
                if (intensityBar.getVisibility() == View.VISIBLE) {

                    intensityBar.setProgress(item.getDeviceFunctions().get(Functions.RGB_LED).get(Properties.INTENSITY).getIntValue());

                    intensityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                            if (bboxIotService != null) {
                                try {

                                    String pushRequest = EventBuilder.buildPushRequest(item.getDeviceFunctions().get(Functions.RGB_LED).get(Properties.INTENSITY), progress);

                                    boolean status = bboxIotService.getBluetoothManager().pushValue(pushRequest);

                                    if (!status)
                                        Log.e(TAG, "push request has failed");
                                    else
                                        Log.e(TAG, "push request sent");

                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                }

                propertyAdapter = new PropertyAdapter(BboxIoTActivity.this,
                        android.R.layout.simple_list_item_1, propertyList);

                propertiesList.setAdapter(propertyAdapter);

                propertiesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view,
                                            int position, long id) {

                        final SmartProperty item = (SmartProperty) parent.getItemAtPosition(position);

                        if (bboxIotService != null) {
                            try {

                                System.out.println(item.getFunction() + " et " + item.getProperty());

                                String pullRequest = EventBuilder.buildPullRequest(item);

                                System.out.println("request : " + pullRequest);

                                boolean status = bboxIotService.getBluetoothManager().pullValue(pullRequest);

                                if (!status)
                                    Log.e(TAG, "pull request has failed");
                                else
                                    Log.e(TAG, "pull request sent");

                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                supportedDevice.setText(item.getBtSmartDevice().getGenericDevice().getSupportedDevice().toString());

                ImageView connectedValue = (ImageView) dialog.findViewById(R.id.device_connected_value);

                TextView deviceUidVal = (TextView) dialog.findViewById(R.id.device_uid_value);
                deviceUidVal.setText(item.getDeviceUuid());

                TextView deviceNameList = (TextView) dialog.findViewById(R.id.device_name_list_value);

                TextView deviceUp = (TextView) dialog.findViewById(R.id.device_up_value);
                deviceUp.setText("" + item.getBtSmartDevice().isUp());

                TextView deviceMode = (TextView) dialog.findViewById(R.id.device_mode_value);
                deviceMode.setText(item.getBtSmartDevice().getDeviceMode().toString());

                TextView deviceAddress = (TextView) dialog.findViewById(R.id.device_address_value);
                deviceAddress.setText(item.getBtSmartDevice().getDeviceAddress());

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
                                int status = bboxIotService.getBluetoothManager().connect(item.getDeviceUuid());

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


    private void setColor(BtConnection connection, int color) {

        if (bboxIotService != null) {
            try {

                if (connection.getDeviceFunctions().containsKey(Functions.RGB_LED) && connection.getDeviceFunctions().get(Functions.RGB_LED).containsKey(Properties.COLOR)) {

                    String pushRequest = EventBuilder.buildPushRequest(connection.getDeviceFunctions().get(Functions.RGB_LED).get(Properties.COLOR), color);

                    boolean status = bboxIotService.getBluetoothManager().pushValue(pushRequest);

                    if (!status)
                        Log.e(TAG, "push request has failed");
                    else
                        Log.e(TAG, "push request sent");
                } else {
                    Log.e(TAG, "error : function RGB_LED or property COLOR not found");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
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

                TextView supportedDeviceName = (TextView) dialog.findViewById(R.id.supported_device_name_value);

                supportedDeviceName.setText(item.getGenericDevice().getSupportedDevice().toString());

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
                deviceMode.setText(item.getDeviceMode().toString());

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
