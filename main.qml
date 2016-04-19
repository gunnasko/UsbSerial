import QtQuick 2.3
import QtQuick.Window 2.2
import QtQuick.Controls 1.2

Window {
    visible: true

    Button {
        anchors.centerIn: parent
        id: searchButton
        text: "Search devices"
        onClicked: {
            numberLabel.text = usbSerial.searchAndConnect()
        }
    }
    Text {
        id: numberLabel
        text: "Not searched"
        anchors.left: searchButton.right
        anchors.top: searchButton.top
    }

    Button {
        anchors.left: numberLabel.right
        anchors.top: numberLabel.top
        anchors.leftMargin: 5
        id: idButton
        text: "GetId"
        onClicked: {
            idButton.text = usbSerial.getCurrentVendorId()
        }
    }

    Button {
        anchors.bottom: messageLabel.top
        anchors.left: messageLabel.left
        id: writeReadButton
        text: "Send message"
        onClicked: {
            messageValue.text = usbSerial.writeWaitRead()
        }
    }
    Text {
        id: messageLabel
        text: "Message recieved:"
        anchors.bottom: parent.bottom
        anchors.left: parent.left
        anchors.leftMargin: 5

    }

    Text {
        id: messageValue
        text: "Not searched"
        anchors.left: messageLabel.right
        anchors.top: messageLabel.top
    }



    Button {
        anchors.top: idButton.bottom
        anchors.topMargin: 5
        text: "Exit"
        onClicked: {
            Qt.quit()
        }
    }
}
