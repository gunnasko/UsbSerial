#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQmlContext>
#include "usbserial.h"
#include <QDebug>

int main(int argc, char *argv[])
{
    QGuiApplication app(argc, argv);

    QQmlApplicationEngine engine;
    engine.load(QUrl(QStringLiteral("qrc:/main.qml")));

    auto usb = new UsbSerial();
    usb->searchDrivers();
    qDebug()<<usb->getCurrentVendorId();

    engine.rootContext()->setContextProperty("usbSerial", usb);
    return app.exec();
}
