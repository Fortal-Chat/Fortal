import QtQuick
import QtQuick.Layouts
import QtQuick.Controls.Basic

// Форма входа. Сама ничего не отправляет — только проверяет, что поля
// заполнены, и испускает сигнал submitted(). Отправкой занимается Main.qml.
ColumnLayout {
    id: root

    property bool busy: false

    signal submitted(string login, string password)
    signal switchToRegister()

    // ---- Управление снаружи (из Main.qml) ----
    function setFormError(message) { formError.text = message }

    function setFieldError(field, message) {
        if (field === "login")
            loginField.errorText = message
        else if (field === "password")
            passwordField.errorText = message
        else
            formError.text = message
    }

    function clearErrors() {
        formError.text = ""
        loginField.errorText = ""
        passwordField.errorText = ""
    }

    function reset() {
        clearErrors()
        loginField.text = ""
        passwordField.text = ""
    }

    function clearPassword() { passwordField.text = "" }

    function focusFirstField() { loginField.focusInput() }

    // ---- Внутреннее ----
    function submit() {
        if (root.busy)
            return
        clearErrors()

        const login = loginField.text.trim()
        let firstInvalid = null

        if (login === "") {
            loginField.errorText = qsTr("Введите имя пользователя или email")
            firstInvalid = loginField
        }
        if (passwordField.text === "") {
            passwordField.errorText = qsTr("Введите пароль")
            firstInvalid = firstInvalid || passwordField
        }

        if (firstInvalid) {
            firstInvalid.focusInput()
            return
        }
        root.submitted(login, passwordField.text)
    }

    spacing: 16

    ColumnLayout {
        Layout.fillWidth: true
        spacing: 4

        Label {
            Layout.alignment: Qt.AlignHCenter
            text: qsTr("С возвращением!")
            color: Theme.text
            font.pixelSize: 24
            font.bold: true
        }
        Label {
            Layout.alignment: Qt.AlignHCenter
            text: qsTr("Войдите, чтобы продолжить")
            color: Theme.textMuted
            font.pixelSize: 14
        }
    }

    ErrorBanner { id: formError }

    FormField {
        id: loginField
        label: qsTr("Имя пользователя или email")
        enabled: !root.busy
        onAccepted: passwordField.focusInput()
    }

    FormField {
        id: passwordField
        label: qsTr("Пароль")
        password: true
        enabled: !root.busy
        onAccepted: root.submit()
    }

    PrimaryButton {
        Layout.topMargin: 8
        text: qsTr("Войти")
        busy: root.busy
        onClicked: root.submit()
    }

    RowLayout {
        Layout.alignment: Qt.AlignHCenter
        spacing: 6

        Label {
            text: qsTr("Нет аккаунта?")
            color: Theme.textMuted
            font.pixelSize: 14
        }
        TextLink {
            text: qsTr("Зарегистрироваться")
            font.pixelSize: 14
            enabled: !root.busy
            onClicked: root.switchToRegister()
        }
    }
}
