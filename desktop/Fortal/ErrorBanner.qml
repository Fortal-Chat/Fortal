import QtQuick
import QtQuick.Layouts
import QtQuick.Controls.Basic

// Красная плашка с общей ошибкой формы. Пустой text = плашка скрыта.
Rectangle {
    id: root

    property alias text: message.text

    visible: text !== ""
    Layout.fillWidth: true
    implicitHeight: message.implicitHeight + 20
    radius: Theme.radius
    color: Theme.dangerBg
    border.width: 1
    border.color: Theme.danger

    Label {
        id: message
        anchors.fill: parent
        anchors.margins: 10
        wrapMode: Text.WordWrap
        verticalAlignment: Text.AlignVCenter
        color: Theme.dangerText
        font.pixelSize: 13
    }
}
