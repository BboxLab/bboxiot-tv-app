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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.HashSet;
import java.util.Set;

import fr.bmartel.android.dotti.R;
import fr.bouyguestelecom.tv.bboxiot.IBboxIotService;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.IBluetoothEventListener;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.EventBuilder;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.EventRegistration;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.IGenericEvent;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.IotEvent;
import fr.bouyguestelecom.tv.bboxiot.protocol.bluetooth.events.impl.BluetoothStateEvent;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bboxiot_activity);

        //create intent
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("fr.bouyguestelecom.tv.bboxiot.main",
                "fr.bouyguestelecom.tv.bboxiot.main.IotService"));

        btStateOnBtn = (Button) findViewById(R.id.bluetooth_state_on);
        btStateOffBtn = (Button) findViewById(R.id.bluetooth_state_off);

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

        ServiceConnection mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {

                Log.i(TAG, "Service IoT has connected");

                //get api wrapper
                bboxIotService = IBboxIotService.Stub.asInterface(service);

                try {
                    Log.i(TAG, "bluetooth state : " + bboxIotService.getBluetoothManager().getBluetoothState());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                try {
                    if (bboxIotService.getBluetoothManager().getBluetoothState()) {
                        btStateOnBtn.setEnabled(false);
                        btStateOffBtn.setEnabled(true);
                        btStateOffBtn.requestFocus();
                    } else {
                        btStateOnBtn.setEnabled(true);
                        btStateOffBtn.setEnabled(false);
                        btStateOnBtn.requestFocus();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                try {
                    Set<EventRegistration> registrationSet = new HashSet<>();
                    registrationSet.add(EventRegistration.REGISTRATION_BLUETOOTH_STATE);

                    bboxIotService.getBluetoothManager().registerEvents(EventBuilder.buildRegistration(registrationSet).toJsonString(), new IBluetoothEventListener.Stub() {

                        public void onEventReceived(int type, int topic, String event) {

                            Log.i(TAG, "event received => type : " + type + " | topic : " + topic + " | event content : " + event);

                            IGenericEvent genericEvent = IotEvent.parse(event);

                            if (genericEvent != null) {

                                if (genericEvent instanceof BluetoothStateEvent) {

                                    System.out.println("???");
                                    BluetoothStateEvent btEvent = (BluetoothStateEvent) genericEvent;

                                    System.out.println("???=> " + btEvent.getBluetoothState());

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

    @Override
    protected void onDestroy() {
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
