package com.example.kirill.ff_one;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener {
    private static final String TAG = "ff_one";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = "00:00:00:00:00:00";
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    TextView serialMsg;
    private boolean state = false;
    private String future_state_str = "ON";
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;
    private SpeedometerView speedometer;
    private BatteryView battery;
    private float xPercent = 0;
    private float yPercent = 0;
    private int boost = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        setContentView(R.layout.activity_main);

        // set joystick transparent. wth.
        JoystickView joystick = (JoystickView)findViewById(R.id.JoystickView);
        joystick.setZOrderOnTop(true);
        SurfaceHolder j = joystick.getHolder();
        j.setFormat(PixelFormat.TRANSPARENT);
        // set speedo transparent
        speedometer = (SpeedometerView)findViewById(R.id.SpeedometerView);
        speedometer.setZOrderOnTop(true);
        SurfaceHolder s = speedometer.getHolder();
        s.setFormat(PixelFormat.TRANSPARENT);
        // set battery transparent
        battery = (BatteryView)findViewById(R.id.BatteryView);
        battery.setZOrderOnTop(true);
        SurfaceHolder b = battery.getHolder();
        b.setFormat(PixelFormat.TRANSPARENT);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...In onResume - Attempting client connect...");
        findAddress();
        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Two things are needed to make a connection:
        //   A MAC address, which we got above.

        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting to Remote...");
        try {
            btSocket.connect();
            Log.d(TAG, "...Connection established and data link opened...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Creating Socket...");

        try {
            outStream = btSocket.getOutputStream();
            inStream = btSocket.getInputStream();
            listenForData();
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
    }


    private void findAddress() {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().contains("HC-05")) {
                address = device.getAddress();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }
        try {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }


    private void errorExit(String title, String message) {
        Toast msg = Toast.makeText(getBaseContext(),
                title + " - " + message, Toast.LENGTH_SHORT);
        msg.show();
        finish();
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on

        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth is enabled...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }
    private String makeMessage(float xPercent, float yPercent, int boost) {
        String message = String.format("<%f, %f, %d>",xPercent, yPercent, boost);
//        String message = "<1,1,1>";
        return message;
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        Log.d(TAG, "...Sending data: " + message + "...");

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {
            String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
            if (address.equals("00:00:00:00:00:00"))
                msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
            msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

            errorExit("Fatal Error", msg);
        }
    }

    void listenForData() {
        final Handler handler = new Handler();
        final byte start = 60;
        final byte end = 62;
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = inStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == end) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    String data = new String(encodedBytes, "US-ASCII");
                                    data = data.replaceAll("(\\r|\\n)","");
                                    String vals[]= data.split(",", 3);
                                    battery.setVoltage(Float.parseFloat(vals[2]));
                                    final double rpm_val = (Float.parseFloat(vals[0])+Float.parseFloat(vals[1]))*0.5;
                                    speedometer.setRpm((int)(rpm_val));
                                    runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              TextView rpm_text = (TextView) findViewById(R.id.rpmCounter);
                                              rpm_text.setText(String.format("%f RPM", rpm_val));
                                          }
                                      });
                                    Log.d(TAG, "Received data: "+data);
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        public void run() {
                                        int i = 0;
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }

    public void onJoystickMoved(float x, float y, int source) {
        xPercent = x;
        yPercent = y;
        String message = makeMessage(xPercent,yPercent, 1);
        Log.d(TAG, "Attempting to send:"+message);
        sendData(message);

    }
}
