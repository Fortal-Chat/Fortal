import QtQuick
import QtQuick.Layouts
import QtQuick.Controls.Basic

ApplicationWindow {
    id: window

    width: 960
    height: 680
    minimumWidth: 400
    minimumHeight: 560
    visible: true
    title: qsTr("Fortal")
    color: Theme.background

    enum Screen { Login, Register, SignedIn }

    property int screen: Main.Screen.Login
    property string currentUsername: ""

    // =====================================================================
    //  API ДЛЯ БЭКЕНДА (C++)
    //
    //  Окно -> бэкенд (сигналы):
    //    loginRequested(login, password)            — пользователь нажал «Войти»
    //    registerRequested(username, email, password) — нажал «Зарегистрироваться»
    //    logoutRequested()                          — нажал «Выйти»
    //
    //  Бэкенд -> окно (функции):
    //    showAuthenticated(username) — вход/регистрация успешны
    //    showError(code, message)    — ошибка; code из docs/api.md
    //                                  (username_taken, email_taken,
    //                                   invalid_credentials, validation_error)
    //                                  или любой свой, например "network_error"
    //
    //  busy выставляется само при отправке и сбрасывается в showAuthenticated/showError.
    // =====================================================================

    signal loginRequested(string login, string password)
    signal registerRequested(string username, string email, string password)
    signal logoutRequested()

    property bool busy: false

    function showAuthenticated(username) {
        busy = false
        currentUsername = username
        loginPage.reset()
        registerPage.reset()
        switchTo(Main.Screen.SignedIn)
    }

    function showError(code, message) {
        busy = false
        const page = screen === Main.Screen.Register ? registerPage : loginPage

        switch (code) {
        case "username_taken":
            registerPage.setFieldError("username", message || qsTr("Это имя уже занято"))
            break
        case "email_taken":
            registerPage.setFieldError("email", message || qsTr("Этот email уже зарегистрирован"))
            break
        case "invalid_credentials":
            loginPage.clearPassword()
            loginPage.setFormError(message || qsTr("Неверный логин или пароль"))
            break
        case "validation_error":
            page.setFormError(message || qsTr("Проверьте введённые данные"))
            break
        default:
            page.setFormError(message || qsTr("Не удалось связаться с сервером. Попробуйте позже."))
        }
    }

    // ---- Переключение экранов ----
    function switchTo(target) {
        loginPage.clearErrors()
        registerPage.clearErrors()
        screen = target
        Qt.callLater(function() {
            if (screen === Main.Screen.Login)
                loginPage.focusFirstField()
            else if (screen === Main.Screen.Register)
                registerPage.focusFirstField()
        })
    }

    // =====================================================================
    //  ВРЕМЕННАЯ ЗАГЛУШКА СЕРВЕРА — только чтобы посмотреть, как выглядит
    //  загрузка, ошибки и успех. Когда подключишь C++, поставь
    //  useFakeBackend: false или удали этот блок целиком.
    //
    //  Вход:        alice / secret123 (или alice@example.com) — успех,
    //               иначе invalid_credentials.
    //  Регистрация: username "alice" — username_taken,
    //               email "alice@example.com" — email_taken, иначе успех.
    // =====================================================================
    property bool useFakeBackend: true

    Timer {
        id: fakeServer
        interval: 800
        property var reply: null
        onTriggered: reply()
    }

    function fakeRespond(reply) {
        fakeServer.reply = reply
        fakeServer.restart()
    }

    onLoginRequested: (login, password) => {
        if (!useFakeBackend)
            return
        fakeRespond(() => {
            if ((login === "alice" || login === "alice@example.com") && password === "secret123")
                showAuthenticated("alice")
            else
                showError("invalid_credentials", "")
        })
    }

    onRegisterRequested: (username, email, password) => {
        if (!useFakeBackend)
            return
        fakeRespond(() => {
            if (username.toLowerCase() === "alice")
                showError("username_taken", "")
            else if (email.toLowerCase() === "alice@example.com")
                showError("email_taken", "")
            else
                showAuthenticated(username)
        })
    }
    // ===================== конец заглушки =====================

    // ---- Фон ----
    Rectangle {
        anchors.fill: parent
        gradient: Gradient {
            GradientStop { position: 0.0; color: Theme.backgroundGlow }
            GradientStop { position: 0.6; color: Theme.background }
        }
    }

    // ---- Карточка по центру (прокручивается, если окно низкое) ----
    Flickable {
        id: flick
        anchors.fill: parent
        contentWidth: width
        contentHeight: Math.max(height, card.height + 64)
        boundsBehavior: Flickable.StopAtBounds
        clip: true

        ScrollBar.vertical: ScrollBar { }

        Rectangle {
            id: card

            readonly property int padding: width < 380 ? 24 : 36

            anchors.centerIn: parent
            width: Math.min(420, flick.width - 32)
            height: content.implicitHeight + 2 * padding
            radius: Theme.radiusLarge
            color: Theme.card
            border.width: 1
            border.color: Theme.cardBorder

            Behavior on height { NumberAnimation { duration: 180; easing.type: Easing.OutCubic } }

            ColumnLayout {
                id: content
                anchors.left: parent.left
                anchors.right: parent.right
                anchors.top: parent.top
                anchors.margins: card.padding
                spacing: 24

                // Логотип
                RowLayout {
                    Layout.alignment: Qt.AlignHCenter
                    spacing: 10

                    Rectangle {
                        implicitWidth: 36
                        implicitHeight: 36
                        radius: 10
                        color: Theme.accent

                        Label {
                            anchors.centerIn: parent
                            text: "F"
                            color: Theme.textOnAccent
                            font.pixelSize: 20
                            font.bold: true
                        }
                    }
                    Label {
                        text: "Fortal"
                        color: Theme.text
                        font.pixelSize: 22
                        font.bold: true
                    }
                }

                LoginPage {
                    id: loginPage
                    Layout.fillWidth: true
                    visible: window.screen === Main.Screen.Login
                    busy: window.busy

                    onSubmitted: (login, password) => {
                        window.busy = true
                        window.loginRequested(login, password)
                    }
                    onSwitchToRegister: window.switchTo(Main.Screen.Register)
                }

                RegisterPage {
                    id: registerPage
                    Layout.fillWidth: true
                    visible: window.screen === Main.Screen.Register
                    busy: window.busy

                    onSubmitted: (username, email, password) => {
                        window.busy = true
                        window.registerRequested(username, email, password)
                    }
                    onSwitchToLogin: window.switchTo(Main.Screen.Login)
                }

                // Временный экран после входа — пока нет главного окна
                ColumnLayout {
                    Layout.fillWidth: true
                    visible: window.screen === Main.Screen.SignedIn
                    spacing: 16

                    Label {
                        Layout.alignment: Qt.AlignHCenter
                        text: qsTr("Вы вошли как %1").arg(window.currentUsername)
                        color: Theme.text
                        font.pixelSize: 20
                        font.bold: true
                    }
                    Label {
                        Layout.fillWidth: true
                        horizontalAlignment: Text.AlignHCenter
                        wrapMode: Text.WordWrap
                        text: qsTr("Главный экран появится на следующих этапах.")
                        color: Theme.textMuted
                        font.pixelSize: 14
                    }
                    PrimaryButton {
                        text: qsTr("Выйти")
                        onClicked: {
                            window.logoutRequested()
                            window.currentUsername = ""
                            window.switchTo(Main.Screen.Login)
                        }
                    }
                }
            }
        }
    }

    // ---- Переключатель темы ----
    Button {
        id: themeToggle
        anchors.top: parent.top
        anchors.right: parent.right
        anchors.margins: 16
        leftPadding: 12
        rightPadding: 12
        topPadding: 6
        bottomPadding: 6
        focusPolicy: Qt.NoFocus
        hoverEnabled: true
        font.pixelSize: 13
        text: Theme.dark ? qsTr("☼  Светлая тема") : qsTr("☽  Тёмная тема")
        onClicked: Theme.dark = !Theme.dark

        contentItem: Label {
            text: themeToggle.text
            font: themeToggle.font
            color: themeToggle.hovered ? Theme.text : Theme.textMuted
            horizontalAlignment: Text.AlignHCenter
            verticalAlignment: Text.AlignVCenter
        }
        background: Rectangle {
            radius: Theme.radius
            color: themeToggle.hovered ? Theme.fieldHover : "transparent"
            border.width: 1
            border.color: Theme.cardBorder
        }
    }

    Component.onCompleted: loginPage.focusFirstField()
}
