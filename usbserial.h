#ifndef USBSERIAL_H
#define USBSERIAL_H

#include <QObject>
#include <QtCore>

#define USB_SERIAL_CLASS_PATH "org/qtproject/qt5/android/bindings/QtUsbManager"

#define WAIT_TIME_SHORT 500
#define WAIT_TIME_LONG  5000

#include <jni.h>

class UsbSerial : public QObject
{
    Q_OBJECT
public:
    explicit UsbSerial(QObject *parent = 0);
    ~UsbSerial();
    Q_INVOKABLE int searchDrivers();
    Q_INVOKABLE int getCurrentVendorId();

    Q_INVOKABLE bool setParameters(int baudRate);
    qint64 write(const char *data, qint64 maxSize);
    qint64 write(const char *data);
    inline qint64 write(const QByteArray &data)
    { return write(data.constData(), data.size()); }
    bool open();
    bool close();
    QByteArray readAll();

    Q_INVOKABLE QString writeWaitRead();

    void addChecksum(QByteArray *frame);

    QByteArray waitForReadyRead(int msecs);
private:
    jobject usbSerialObject;
    JNIEnv* attachJniToThread();

};

#endif // USBSERIAL_H


