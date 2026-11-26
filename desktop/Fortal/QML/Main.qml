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

    signal loginRequested(string login, string password)
    signal registerRequested(string username, string email, string password)
    signal logoutRequested()

    readonly property bool busy: auth.busy

    function showAuthenticated(username) {
        currentUsername = username
        loginPage.reset()
        registerPage.reset()
        switchTo(Main.Screen.SignedIn)
    }

    function showError(code, message) {
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

    ApiClient {
        id: api
        baseUrl: "http://localhost:8080"
    }

    AuthClient {
        id: auth
        api: api
        onLoggedIn: window.showAuthenticated(auth.username)
        onFailed: (code, message) => window.showError(code, message)
        onSessionExpired: {
            window.currentUsername = ""
            window.switchTo(Main.Screen.Login)
            loginPage.setFormError(qsTr("Сессия истекла, войдите снова"))
        }
    }

    onLoginRequested: (login, password) => auth.login(login, password)
    onRegisterRequested: (username, email, password) => auth.registerUser(username, email, password)
    onLogoutRequested: auth.logout()

    Rectangle {
        anchors.fill: parent
        gradient: Gradient {
            GradientStop { position: 0.0; color: Theme.backgroundGlow }
            GradientStop { position: 0.6; color: Theme.background }
        }
    }

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
                        window.registerRequested(username, email, password)
                    }
                    onSwitchToLogin: window.switchTo(Main.Screen.Login)
                }

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

    GatewayClient {
        id: gateway
        url: "ws://localhost:8081/"
        Component.onCompleted: start()
    }

    Rectangle {
        id: connectionBadge

        readonly property bool pending: gateway.status === GatewayClient.Connecting
                                        || gateway.status === GatewayClient.Reconnecting

        readonly property color dotColor: {
            switch (gateway.status) {
            case GatewayClient.Connected: return Theme.success
            case GatewayClient.Disconnected: return Theme.danger
            default: return Theme.warning
            }
        }

        readonly property string label: {
            switch (gateway.status) {
            case GatewayClient.Connected: return qsTr("В сети")
            case GatewayClient.Connecting: return qsTr("Подключение…")
            case GatewayClient.Reconnecting: return qsTr("Переподключение…")
            default: return qsTr("Нет соединения")
            }
        }

        anchors.top: parent.top
        anchors.left: parent.left
        anchors.margins: 16
        width: badgeRow.implicitWidth + 24
        height: badgeRow.implicitHeight + 12
        radius: Theme.radius
        color: "transparent"
        border.width: 1
        border.color: Theme.cardBorder

        RowLayout {
            id: badgeRow
            anchors.centerIn: parent
            spacing: 8

            Rectangle {
                id: dot
                implicitWidth: 8
                implicitHeight: 8
                radius: 4
                color: connectionBadge.dotColor

                SequentialAnimation on opacity {
                    running: connectionBadge.pending
                    loops: Animation.Infinite
                    alwaysRunToEnd: true
                    NumberAnimation { to: 0.3; duration: 600; easing.type: Easing.InOutQuad }
                    NumberAnimation { to: 1.0; duration: 600; easing.type: Easing.InOutQuad }
                }
            }

            Label {
                text: connectionBadge.label
                color: Theme.textMuted
                font.pixelSize: 13
            }
        }
    }

    Component.onCompleted: loginPage.focusFirstField()
}
