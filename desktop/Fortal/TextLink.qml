import QtQuick
import QtQuick.Controls.Basic

// Кликабельный текст-ссылка («Зарегистрироваться», «Войти»).
Label {
    id: root

    signal clicked()

    color: area.containsMouse ? Theme.accentHover : Theme.accentText
    font.underline: area.containsMouse
    opacity: enabled ? 1.0 : 0.6

    MouseArea {
        id: area
        anchors.fill: parent
        hoverEnabled: true
        cursorShape: Qt.PointingHandCursor
        onClicked: root.clicked()
    }
}
