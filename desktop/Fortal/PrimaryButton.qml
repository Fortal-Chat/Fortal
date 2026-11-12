import QtQuick
import QtQuick.Layouts
import QtQuick.Controls.Basic

// Главная кнопка формы. busy = true: крутится индикатор, кнопка не нажимается.
Button {
    id: control

    property bool busy: false

    enabled: !busy
    Layout.fillWidth: true
    implicitHeight: 44
    font.pixelSize: 15
    font.bold: true
    hoverEnabled: true

    contentItem: Item {
        implicitHeight: 24

        Label {
            anchors.centerIn: parent
            visible: !control.busy
            text: control.text
            font: control.font
            color: Theme.textOnAccent
        }

        BusyIndicator {
            anchors.centerIn: parent
            visible: control.busy
            running: control.busy
            implicitWidth: 24
            implicitHeight: 24
            palette.dark: Theme.textOnAccent
        }
    }

    background: Rectangle {
        radius: Theme.radius
        color: control.busy       ? Theme.accent
             : !control.enabled   ? Theme.accentDisabled
             : control.down       ? Theme.accentPressed
             : control.hovered    ? Theme.accentHover
                                  : Theme.accent
        Behavior on color { ColorAnimation { duration: 100 } }
    }
}
