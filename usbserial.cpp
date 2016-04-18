#include "usbserial.h"

static JavaVM* s_javaVM = 0;
static jclass s_qtActivityClass = 0;
static jmethodID s_createUsbSerialMethod = 0;
static jmethodID s_searchDrivers = 0;
static jmethodID s_getCurrentVendorId = 0;

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

    return JNI_VERSION_1_6;
}
