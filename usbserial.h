#ifndef USBSERIAL_H
#define USBSERIAL_H

#include <QObject>
#include <QtCore>

#define USB_SERIAL_CLASS_PATH "org/qtproject/qt5/android/bindings/QtUsbManager"

#include <jni.h>

class UsbSerial : public QObject
{
    Q_OBJECT
public:
    explicit UsbSerial(QObject *parent = 0);
    ~UsbSerial();
    Q_INVOKABLE int searchDrivers();
    Q_INVOKABLE int getCurrentVendorId();

private:
    jobject usbSerialObject;
    JNIEnv* attachJniToThread();

};

#endif // USBSERIAL_H


