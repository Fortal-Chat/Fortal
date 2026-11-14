pragma Singleton
import QtQuick

// Цвета и размеры всего приложения в одном месте.
// Использование в любом QML-файле модуля: Theme.text, Theme.accent и т.д.
QtObject {
    // Тёмная тема по умолчанию, если система не просит светлую.
    // Можно переключить вручную: Theme.dark = !Theme.dark
    property bool dark: Application.styleHints.colorScheme !== Qt.Light

    // Фон
    readonly property color background:     dark ? "#111318" : "#EEF1F6"
    readonly property color backgroundGlow: dark ? "#1A2340" : "#DCE6FA"

    // Карточка с формой
    readonly property color card:       dark ? "#1B1E25" : "#FFFFFF"
    readonly property color cardBorder: dark ? "#2A2E37" : "#DDE1E8"

    // Поля ввода
    readonly property color field:      dark ? "#111318" : "#F5F7FA"
    readonly property color fieldHover: dark ? "#262A33" : "#E9EDF3"
    readonly property color border:     dark ? "#323743" : "#CDD3DC"

    // Текст
    readonly property color text:      dark ? "#ECEEF2" : "#1A1D23"
    readonly property color textMuted: dark ? "#A0A7B4" : "#5B6372"
    readonly property color textFaint: dark ? "#6B7280" : "#9AA1AD"

    // Акцент (кнопки, ссылки, фокус)
    readonly property color accent:         "#2F6FEB"
    readonly property color accentHover:    "#3D7CF5"
    readonly property color accentPressed:  "#2559C4"
    readonly property color accentDisabled: dark ? "#2A3A5E" : "#A9C1F3"
    readonly property color accentText:     dark ? "#7AA7FF" : "#2459D1"
    readonly property color textOnAccent:   "#FFFFFF"

    // Ошибки и успех
    readonly property color danger:     dark ? "#F0646A" : "#D3343A"
    readonly property color dangerBg:   dark ? "#3A1E22" : "#FDECEC"
    readonly property color dangerText: dark ? "#FFB4B7" : "#9F1D22"
    readonly property color success:    dark ? "#4CC38A" : "#1F8A57"

    // Размеры
    readonly property int radius: 8
    readonly property int radiusLarge: 16
}
