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

    private static final int TIMEOUT_IN_MS = 100;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

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
        return _instance;
    }

    public QtUsbManager()
    {
        _activity = this;
        _instance = this;
    }

    public int searchDrivers()
    {
        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x0451, 0x16a8, CdcAcmSerialDriver.class);
        UsbSerialProber prober = new UsbSerialProber(customTable);

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
            return false;
        }
        return true;
    }

    public boolean open() {
        if(_currentConnection == null || _currentPort == null)
            return false;

        try {
            _currentPort.open(_currentConnection);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public byte[] read() {
        byte[] buffer = new byte[16];
        if(_currentConnection == null || _currentPort == null)
            return null;

        try {
            _currentPort.read(buffer, TIMEOUT_IN_MS);
        } catch (IOException e) {
            return null;
        }
        return buffer;
    }

    public boolean write(byte[] src) {
        byte[] ret;
        if(_currentConnection == null || _currentPort == null)
            return false;

        try {
            _currentPort.write(src, TIMEOUT_IN_MS);
        } catch (IOException e) {
            return false;
        }
        return true;
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
