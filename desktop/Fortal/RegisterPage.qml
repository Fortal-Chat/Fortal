import QtQuick
import QtQuick.Layouts
import QtQuick.Controls.Basic

// Форма регистрации. Проверяет поля по правилам из docs/api.md
// и испускает submitted(). Отправкой занимается Main.qml.
ColumnLayout {
    id: root

    property bool busy: false

    signal submitted(string username, string email, string password)
    signal switchToLogin()

    // ---- Управление снаружи (из Main.qml) ----
    function setFormError(message) { formError.text = message }

    function setFieldError(field, message) {
        const fields = {
            "username": usernameField,
            "email": emailField,
            "password": passwordField
        }
        if (fields[field]) {
            fields[field].errorText = message
            fields[field].focusInput()
        } else {
            formError.text = message
        }
    }

    function clearErrors() {
        formError.text = ""
        usernameField.errorText = ""
        emailField.errorText = ""
        passwordField.errorText = ""
        confirmField.errorText = ""
    }

    function reset() {
        clearErrors()
        usernameField.text = ""
        emailField.text = ""
        passwordField.text = ""
        confirmField.text = ""
    }

    function focusFirstField() { usernameField.focusInput() }

    // ---- Внутреннее ----
    function submit() {
        if (root.busy)
            return
        clearErrors()

        const username = usernameField.text
        const email = emailField.text.trim()
        let firstInvalid = null

        // Правила из договора: username 3–32 символа [a-zA-Z0-9_], пароль ≥ 8
        if (!/^[a-zA-Z0-9_]{3,32}$/.test(username)) {
            usernameField.errorText = qsTr("От 3 до 32 символов: латинские буквы, цифры и _")
            firstInvalid = usernameField
        }
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            emailField.errorText = qsTr("Введите корректный email")
            firstInvalid = firstInvalid || emailField
        }
        if (passwordField.text.length < 8) {
            passwordField.errorText = qsTr("Пароль должен быть не короче 8 символов")
            firstInvalid = firstInvalid || passwordField
        }
        if (confirmField.text !== passwordField.text) {
            confirmField.errorText = qsTr("Пароли не совпадают")
            firstInvalid = firstInvalid || confirmField
        }

        if (firstInvalid) {
            firstInvalid.focusInput()
            return
        }
        root.submitted(username, email, passwordField.text)
    }

    spacing: 16

    ColumnLayout {
        Layout.fillWidth: true
        spacing: 4

        Label {
            Layout.alignment: Qt.AlignHCenter
            text: qsTr("Создать аккаунт")
            color: Theme.text
            font.pixelSize: 24
            font.bold: true
        }
        Label {
            Layout.alignment: Qt.AlignHCenter
            text: qsTr("Присоединяйтесь к Fortal")
            color: Theme.textMuted
            font.pixelSize: 14
        }
    }

    ErrorBanner { id: formError }

    FormField {
        id: usernameField
        label: qsTr("Имя пользователя")
        hint: qsTr("3–32 символа: латиница, цифры и _")
        maximumLength: 32
        validator: RegularExpressionValidator { regularExpression: /[a-zA-Z0-9_]*/ }
        enabled: !root.busy
        onAccepted: emailField.focusInput()
    }

    FormField {
        id: emailField
        label: qsTr("Email")
        inputMethodHints: Qt.ImhEmailCharactersOnly | Qt.ImhNoAutoUppercase
        enabled: !root.busy
        onAccepted: passwordField.focusInput()
    }

    FormField {
        id: passwordField
        label: qsTr("Пароль")
        hint: qsTr("Минимум 8 символов")
        password: true
        enabled: !root.busy
        onAccepted: confirmField.focusInput()
    }

    FormField {
        id: confirmField
        label: qsTr("Повторите пароль")
        password: true
        enabled: !root.busy
        onAccepted: root.submit()
    }

    PrimaryButton {
        Layout.topMargin: 8
        text: qsTr("Зарегистрироваться")
        busy: root.busy
        onClicked: root.submit()
    }

    RowLayout {
        Layout.alignment: Qt.AlignHCenter
        spacing: 6

        Label {
            text: qsTr("Уже есть аккаунт?")
            color: Theme.textMuted
            font.pixelSize: 14
        }
        TextLink {
            text: qsTr("Войти")
            font.pixelSize: 14
            enabled: !root.busy
            onClicked: root.switchToLogin()
        }
    }
}
