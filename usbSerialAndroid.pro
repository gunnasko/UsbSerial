TEMPLATE = app

QT += qml quick
CONFIG += c++11

SOURCES += main.cpp \
    usbserial.cpp

HEADERS += \
    usbserial.h

RESOURCES += qml.qrc

android {
    QT += androidextras
}

# Additional import path used to resolve QML modules in Qt Creator's code model
QML_IMPORT_PATH =

# Default rules for deployment.
include(deployment.pri)

android {
    ANDROID_PACKAGE_SOURCE_DIR = $$PWD/android-sources
    OTHER_FILES += android-sources/src/org/qtproject/usbmanager/QtUsbManager.java android-sources/AndroidManifest.xml
}

DISTFILES += \
    android-sources/AndroidManifest.xml \
    android-sources/device_filter.xml \
    android-sources/src/main/java/com/hoho/android/usbserial/driver/CdcAcmSerialDriver.java \
    android-sources/src/main/java/com/hoho/android/usbserial/driver/Ch34xSerialDriver.java \
    android-sources/src/main/java/com/hoho/android/usbserial/driver/CommonUsbSerialPort.java \
    android-sources/src/main/java/com/hoho/android/usbserial/driver/Cp21xxSerialDriver.java \
    android-sources/src/main/java/com/hoho/android/usbserial/driver/FtdiSerialDriver.java \
    android-sources/src/main/java/com/hoho/android/usbserial/driver/ProbeTable.java \
    android-sources/src/main/java/com/hoho/android/usbserial/driver/ProlificSerialDriver.java \
    android-sources/src/main/java/com/hoho/android/usbserial/driver/UsbId.java \
    android-sources/src/main/java/com/hoho/android/usbserial/driver/UsbSerialDriver.java \
    android-sources/src/main/java/com/hoho/android/usbserial/driver/UsbSerialPort.java \
    android-sources/src/main/java/com/hoho/android/usbserial/driver/UsbSerialProber.java \
    android-sources/src/main/java/com/hoho/android/usbserial/driver/UsbSerialRuntimeException.java \
    android-sources/src/main/java/com/hoho/android/usbserial/util/HexDump.java \
    android-sources/src/main/java/com/hoho/android/usbserial/util/SerialInputOutputManager.java \
    android-sources/res/xml/device_filter.xml \
    android-sources/src/org/qtproject/usbmanager/QtUsbManager.java

