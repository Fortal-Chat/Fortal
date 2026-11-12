cmake_minimum_required(VERSION 3.16)

project(Fortal VERSION 0.1 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(Qt6 REQUIRED COMPONENTS Quick)

qt_standard_project_setup(REQUIRES 6.10)


qt_add_executable(appFortal
    main.cpp
)

set_source_files_properties(Theme.qml PROPERTIES QT_QML_SINGLETON_TYPE TRUE)

qt_add_qml_module(appFortal
    URI Fortal
    QML_FILES
        Main.qml
        Theme.qml
        FormField.qml
        PrimaryButton.qml
        ErrorBanner.qml
        TextLink.qml
        LoginPage.qml
        RegisterPage.qml
)

set_target_properties(appFortal PROPERTIES
    MACOSX_BUNDLE_BUNDLE_VERSION ${PROJECT_VERSION}
    MACOSX_BUNDLE_SHORT_VERSION_STRING ${PROJECT_VERSION_MAJOR}.${PROJECT_VERSION_MINOR}
    MACOSX_BUNDLE TRUE
    WIN32_EXECUTABLE TRUE
)

target_link_libraries(appFortal
    PRIVATE Qt6::Quick
)

install(TARGETS appFortal
    BUNDLE DESTINATION .
    LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
    RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR}
)
