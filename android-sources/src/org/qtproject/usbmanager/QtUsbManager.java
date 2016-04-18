package org.qtproject.qt5.android.bindings;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;


public class QtUsbManager extends QtActivity {

    private static QtUsbManager _instance;
    private static Activity _activity;
    private static UsbManager _usbManager;
    private static UsbSerialDriver _currentDriver;
    private static UsbDeviceConnection _currentConnection;
    private static List<UsbSerialDriver> _availableDrivers;

    public static QtUsbManager createQtUsbManager()
    {
        _usbManager = (UsbManager) _instance.getSystemService(Context.USB_SERVICE);
        return _instance;
    }

    public QtUsbManager() {
        _activity = this;
        _instance = this;
    }

    public int searchDrivers()
    {
        _availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(_usbManager);
        if(!_availableDrivers.isEmpty()) {
            _currentDriver = _availableDrivers.get(0);
            _currentConnection = _usbManager.openDevice(_currentDriver.getDevice());
            if (_currentConnection == null) {
              // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
              return -1;
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

}
