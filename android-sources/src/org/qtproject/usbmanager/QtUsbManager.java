package org.qtproject.qt5.android.bindings;

import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbDevice;
import java.io.IOException;

import android.util.Log;


import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;


public class QtUsbManager extends QtActivity {

    private static QtUsbManager _instance;
    private static Activity _activity;
    private static UsbManager _usbManager;
    private static UsbSerialDriver _currentDriver;
    private static UsbDeviceConnection _currentConnection;
    private static UsbSerialPort _currentPort;

    private static List<UsbSerialDriver> _availableDrivers;

    private static final int TIMEOUT_IN_MS = 5000;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String TAG = "QtUsbManager";


    private final BroadcastReceiver _usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            connectToDevice(device);
                       }
                    }
                    else {
                        //Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    public static QtUsbManager createQtUsbManager()
    {
        _usbManager = (UsbManager) _instance.getSystemService(Context.USB_SERVICE);
        Log.w(TAG, "Created QtUsbManager!");
        return _instance;
    }

    public QtUsbManager()
    {
        _activity = this;
        _instance = this;
    }

    public int searchDrivers()
    {
        //ProbeTable customTable = new ProbeTable();
        //customTable.addProduct(0x0451, 0x16a8, CdcAcmSerialDriver.class);
        UsbSerialProber prober = UsbSerialProber.getDefaultProber();

        _availableDrivers = prober.findAllDrivers(_usbManager);
        if(!_availableDrivers.isEmpty()) {
            _currentDriver = _availableDrivers.get(0);
            if (!_usbManager.hasPermission(_currentDriver.getDevice())) {
                    PendingIntent pi = PendingIntent.getBroadcast(_instance, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    _usbManager.requestPermission(_currentDriver.getDevice(), pi);
                    _instance.registerReceiver(_usbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
                    return -1;
            }
            else {
                  connectToDevice(_currentDriver.getDevice());
            }
        }
        return _availableDrivers.size();
    }

    public int getCurrentVendorId()
    {
        if(_currentDriver != null)
            return _currentDriver.getDevice().getVendorId();
        else
            return -1;
    }

    public boolean setParameters(int baud)
    {
        if(_currentConnection == null || _currentPort == null)
            return false;

        try {
            _currentPort.setParameters(baud, _currentPort.DATABITS_8, _currentPort.STOPBITS_1, _currentPort.PARITY_NONE);
        } catch (IOException e) {
            Log.e(TAG, "Error while setting parameters", e);
            return false;
        }
        return true;
    }

    public boolean open() {
        if(_currentConnection == null || _currentPort == null) {
            return false;
        }

        try {
            _currentPort.open(_currentConnection);
        } catch (IOException e) {
            Log.e(TAG, "Error while opening", e);
            return false;
        }
        return true;
    }

    public boolean close() {
        if(_currentPort == null)
            return false;
        try {
            _currentPort.close();
        } catch (IOException e) {
            Log.e(TAG, "Error while closing", e);
            return false;
        }
        return true;
    }

    public byte[] read(int msecs) {
        byte[] buffer = new byte[16];
        if(_currentConnection == null || _currentPort == null)
            return null;

        try {
            _currentPort.read(buffer, msecs);
        } catch (IOException e) {
            Log.e(TAG, "Error while reading", e);
            return null;
        }
        return buffer;
    }

    public int write(byte[] src) {
        int writeCount = 0;
        if(_currentConnection == null || _currentPort == null)
            return writeCount;

        for (byte b: src) {
            Log.w(TAG, String.format("0x%20x", b));
        }

        try {
            writeCount = _currentPort.write(src, TIMEOUT_IN_MS);
        } catch (IOException e) {
            Log.e(TAG, "Exception caught when writing to port!", e);
            return writeCount;
        }
        return writeCount;
    }

    private void connectToDevice(UsbDevice device)
    {
        _currentConnection = _usbManager.openDevice(device);
        List<UsbSerialPort> ports = _currentDriver.getPorts();
        if(!ports.isEmpty()) {
            _currentPort = ports.get(0);
        }
    }
}
