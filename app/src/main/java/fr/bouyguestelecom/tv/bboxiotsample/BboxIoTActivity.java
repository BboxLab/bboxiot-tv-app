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
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.EventBuilder;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.IGenericEvent;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.IotEvent;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.enums.EventRegistration;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.enums.ScanningAction;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.impl.BluetoothStateEvent;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.impl.ScanItemEvent;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.impl.ScanStatusChangeEvent;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.inter.IScanListItemEvent;

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

    private Button btClearScanList = null;

    private ListView listview = null;

    private StableArrayAdapter scanningListAdapter = null;

    private Map<String, BluetoothSmartDevice> scanningList = new HashMap<>();

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

        ServiceConnection mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {

                Log.i(TAG, "Service IoT has connected");

                //get api wrapper
                bboxIotService = IBboxIotService.Stub.asInterface(service);

                try {

                    IScanListItemEvent event = IotEvent.parseScanningList(bboxIotService.getBluetoothManager().getScanningList());
                    scanningList = event.getList();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (scanningListAdapter != null) {

                                Iterator it = scanningList.entrySet().iterator();

                                while (it.hasNext()) {
                                    Map.Entry<String, BluetoothSmartDevice> pair = (Map.Entry) it.next();
                                    scanningListAdapter.add(pair.getValue());
                                }
                                scanningListAdapter.notifyDataSetChanged();
                            }
                        }
                    });

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
                                                btScanStop.setEnabled(true);
                                            }
                                        });

                                    } else if (btEvent.getAction() == ScanningAction.SCANNING_ACTION_STOP) {

                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {

                                                btScanContinuous.setEnabled(true);
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
                                }

                            }
                        }

                    });
                } catch (RemoteException e) {
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

    private void initializeScanningList() {

        listview = (ListView) findViewById(R.id.scanning_list_view);

        scanningListAdapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, new ArrayList<BluetoothSmartDevice>());

        listview.setAdapter(scanningListAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                System.out.println("before");

                final BluetoothSmartDevice item = (BluetoothSmartDevice) parent.getItemAtPosition(position);

                final Dialog dialog = new Dialog(BboxIoTActivity.this);
                dialog.setContentView(R.layout.scanning_item);
                dialog.setTitle("Device " + item.getDeviceUuid());

                Button buttonBack = (Button) dialog.findViewById(R.id.button_back);
                Button buttonConnect = (Button) dialog.findViewById(R.id.button_connect);

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

                System.out.println("after");

                buttonBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
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
