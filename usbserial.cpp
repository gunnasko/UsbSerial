#include "usbserial.h"

static JavaVM* s_javaVM = 0;
static jclass s_qtActivityClass = 0;
static jmethodID s_createUsbSerialMethod = 0;
static jmethodID s_searchDrivers = 0;
static jmethodID s_getCurrentVendorId = 0;
static jmethodID s_setParameters = 0;
static jmethodID s_write = 0;
static jmethodID s_read = 0;
static jmethodID s_open = 0;
static jmethodID s_close = 0;

UsbSerial::UsbSerial(QObject *parent) : QObject(parent)
{
    JNIEnv* env = attachJniToThread();
    usbSerialObject = env->NewGlobalRef(env->CallStaticObjectMethod(s_qtActivityClass, s_createUsbSerialMethod));
    s_javaVM->DetachCurrentThread();
}

UsbSerial::~UsbSerial()
{
    JNIEnv* env = attachJniToThread();
    env->DeleteGlobalRef(usbSerialObject);
    s_javaVM->DetachCurrentThread();
}

int UsbSerial::searchDrivers()
{
    JNIEnv* env = attachJniToThread();
    int deviceCount = env->CallIntMethod(usbSerialObject, s_searchDrivers);
    s_javaVM->DetachCurrentThread();
    return deviceCount;
}

int UsbSerial::getCurrentVendorId()
{
    JNIEnv* env = attachJniToThread();
    int vendorId = env->CallIntMethod(usbSerialObject, s_getCurrentVendorId);
    s_javaVM->DetachCurrentThread();
    return vendorId;
}

bool UsbSerial::searchAndConnect()
{
    if(searchDrivers() <= 0)
        return false;

    if(!open())
        return false;

    if(!setParameters(115200))
        return false;

    return true;
}

bool UsbSerial::setParameters(int baudRate)
{
    JNIEnv* env = attachJniToThread();
    bool success = env->CallBooleanMethod(usbSerialObject, s_setParameters, baudRate);
    s_javaVM->DetachCurrentThread();
    return success;
}

qint64 UsbSerial::write(const char *data, qint64 maxSize)
{
    Q_UNUSED(maxSize);
    qint64 byteCount;
    JNIEnv* env = attachJniToThread();
    jbyteArray jdata = env->NewByteArray(maxSize);
    env->SetByteArrayRegion(jdata, 0, maxSize, (jbyte*)(data));
    byteCount = env->CallIntMethod(usbSerialObject, s_write, jdata);
    s_javaVM->DetachCurrentThread();
    return byteCount;
}

qint64 UsbSerial::write(const char *data)
{
    return write(data, strlen(data));
}

bool UsbSerial::open()
{
    JNIEnv* env = attachJniToThread();
    bool success = env->CallBooleanMethod(usbSerialObject, s_open);
    s_javaVM->DetachCurrentThread();
    return success;
}

bool UsbSerial::close()
{
    JNIEnv* env = attachJniToThread();
    bool success = env->CallBooleanMethod(usbSerialObject, s_close);
    s_javaVM->DetachCurrentThread();
    return success;
}

QByteArray UsbSerial::waitForReadyRead(int msecs)
{
    QByteArray ret;
    JNIEnv* env = attachJniToThread();
    jbyteArray jresults = (jbyteArray) env->CallObjectMethod(usbSerialObject, s_read, msecs);

    int count = env->GetArrayLength(jresults);
    jbyte* bytes = env->GetByteArrayElements(jresults, NULL);
    for(int i = 0; i < count; i++) {
        ret.append(bytes[i]);
    }
    env->ReleaseByteArrayElements(jresults, bytes, 0);
    s_javaVM->DetachCurrentThread();
    return ret;
}

QByteArray UsbSerial::readAll()
{
    return waitForReadyRead(0);
}

QString UsbSerial::writeWaitRead()
{
    if(!open()) {
        return QString("Failed to open");
    }

    QByteArray frame;
    frame.append(0xfe); //Start of frame;
    frame.append((char)0x00); //Length of general fram
    frame.append(0x21); //SREQ and SYS
    frame.append(0x02); //SYS_VERSION
    addChecksum(&frame);

    if(!write(frame, frame.length())) {
        close();
        return QString("Failed to write");
    }

    QByteArray ret = waitForReadyRead(WAIT_TIME_LONG);
    return QString(ret.toHex());
}

void UsbSerial::addChecksum(QByteArray *frame)
{
    uint8_t checksum;
    checksum = 0x00;
    for(int i=1; i < frame->length(); i++)
        checksum ^= frame->at(i);
    frame->append(checksum);
}

JNIEnv* UsbSerial::attachJniToThread()
{
    JNIEnv *env;
    if (s_javaVM->AttachCurrentThread(&env, NULL)<0)
    {
        qCritical()<<"AttachCurrentThread failed";
        return NULL;
    }
    return env;
}

//All marshalling must be done in JNI_OnLoad.
//Get class/method fails in ActivityContext.
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void*)
{
    s_javaVM = vm;
    JNIEnv* env;
    vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);

    jclass tmpActivityClass = env->FindClass(USB_SERIAL_CLASS_PATH);
    s_qtActivityClass = (jclass)env->NewGlobalRef(tmpActivityClass);
    s_createUsbSerialMethod = env->GetStaticMethodID(s_qtActivityClass,"createQtUsbManager", "()Lorg/qtproject/qt5/android/bindings/QtUsbManager;");

    //This is the sample, add similar code to marshall other methods.
    jclass s_sbManagerClass = env->FindClass(USB_SERIAL_CLASS_PATH);
    s_searchDrivers = env->GetMethodID(s_sbManagerClass, "searchDrivers", "()I");
    s_getCurrentVendorId = env->GetMethodID(s_sbManagerClass, "getCurrentVendorId", "()I");
    s_write = env->GetMethodID(s_sbManagerClass, "write", "([B)I");
    s_read = env->GetMethodID(s_sbManagerClass, "read", "(I)[B");
    s_setParameters = env->GetMethodID(s_sbManagerClass, "setParameters", "(I)Z");
    s_open = env->GetMethodID(s_sbManagerClass, "open", "()Z");
    s_close = env->GetMethodID(s_sbManagerClass, "close", "()Z");

    return JNI_VERSION_1_6;
}
