import QtQuick
import QtQuick.Layouts
import QtQuick.Controls.Basic

// Поле формы: подпись сверху, поле ввода, подсказка или ошибка снизу.
ColumnLayout {
    id: root

    property alias label: caption.text
    property alias text: input.text
    property alias placeholderText: input.placeholderText
    property alias validator: input.validator
    property alias maximumLength: input.maximumLength
    property alias inputMethodHints: input.inputMethodHints
    property bool password: false      // скрывать ввод + кнопка «Показать»
    property string hint: ""           // серая подсказка под полем
    property string errorText: ""      // красная ошибка под полем (важнее подсказки)

    signal accepted()                  // нажат Enter

    function focusInput() { input.forceActiveFocus() }

    spacing: 6
    Layout.fillWidth: true
    opacity: enabled ? 1.0 : 0.6

    Label {
        id: caption
        color: root.errorText !== "" ? Theme.danger : Theme.textMuted
        font.pixelSize: 12
        font.bold: true
        font.capitalization: Font.AllUppercase
        font.letterSpacing: 0.4
    }

    TextField {
        id: input
        Layout.fillWidth: true
        implicitHeight: 42
        leftPadding: 12
        rightPadding: root.password ? reveal.width + 8 : 12
        font.pixelSize: 15
        color: Theme.text
        placeholderTextColor: Theme.textFaint
        selectionColor: Theme.accent
        selectedTextColor: Theme.textOnAccent
        hoverEnabled: true
        echoMode: root.password && !reveal.checked ? TextInput.Password : TextInput.Normal

        onTextEdited: root.errorText = ""
        onAccepted: root.accepted()

        background: Rectangle {
            radius: Theme.radius
            color: input.hovered && !input.activeFocus ? Theme.fieldHover : Theme.field
            border.width: input.activeFocus || root.errorText !== "" ? 2 : 1
            border.color: root.errorText !== "" ? Theme.danger
                        : input.activeFocus     ? Theme.accent
                                                : Theme.border
            Behavior on border.color { ColorAnimation { duration: 120 } }
        }

        Button {
            id: reveal
            visible: root.password
            checkable: true
            focusPolicy: Qt.NoFocus
            anchors.right: parent.right
            anchors.rightMargin: 6
            anchors.verticalCenter: parent.verticalCenter
            leftPadding: 8
            rightPadding: 8
            topPadding: 4
            bottomPadding: 4
            text: checked ? qsTr("Скрыть") : qsTr("Показать")
            font.pixelSize: 12

            contentItem: Label {
                text: reveal.text
                font: reveal.font
                color: reveal.hovered ? Theme.text : Theme.textMuted
                horizontalAlignment: Text.AlignHCenter
                verticalAlignment: Text.AlignVCenter
            }
            background: Rectangle {
                radius: 6
                color: reveal.hovered ? Theme.fieldHover : "transparent"
            }
        }
    }

    Label {
        Layout.fillWidth: true
        visible: text !== ""
        text: root.errorText !== "" ? root.errorText : root.hint
        color: root.errorText !== "" ? Theme.danger : Theme.textFaint
        font.pixelSize: 12
        wrapMode: Text.WordWrap
    }
}
