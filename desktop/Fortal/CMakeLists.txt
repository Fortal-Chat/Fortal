cmake_minimum_required(VERSION 3.16)

project(Fortal VERSION 0.1 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(Qt6 REQUIRED COMPONENTS Core Quick WebSockets Network Test)

qt_standard_project_setup(REQUIRES 6.10)

qt_add_library(fortal_core STATIC
    include/Backoff.h  src/Backoff.cpp
    include/Protocol.h src/Protocol.cpp
    include/AuthProtocol.h src/AuthProtocol.cpp
    include/ApiError.h src/ApiError.cpp
)

target_include_directories(fortal_core PUBLIC include)

target_link_libraries(fortal_core PUBLIC
    Qt6::Core
)

qt_add_executable(appFortal
    main.cpp
)

set_source_files_properties(QML/Theme.qml PROPERTIES QT_QML_SINGLETON_TYPE TRUE)

qt_add_qml_module(appFortal
    URI Fortal
    QML_FILES
        QML/Main.qml
        QML/Theme.qml
        QML/FormField.qml
        QML/PrimaryButton.qml
        QML/ErrorBanner.qml
        QML/TextLink.qml
        QML/LoginPage.qml
        QML/RegisterPage.qml
    SOURCES
        include/GatewayClient.h src/GatewayClient.cpp
        include/ApiReply.h src/ApiReply.cpp
        include/ApiClient.h src/ApiClient.cpp
        include/AuthClient.h src/AuthClient.cpp
)

set_target_properties(appFortal PROPERTIES
    MACOSX_BUNDLE_BUNDLE_VERSION ${PROJECT_VERSION}
    MACOSX_BUNDLE_SHORT_VERSION_STRING ${PROJECT_VERSION_MAJOR}.${PROJECT_VERSION_MINOR}
    MACOSX_BUNDLE TRUE
    WIN32_EXECUTABLE TRUE
)

target_link_libraries(appFortal PRIVATE
    fortal_core
    Qt6::Quick
    Qt6::WebSockets
    Qt6::Network
)

include(GNUInstallDirs)
install(TARGETS appFortal
    BUNDLE DESTINATION .
    LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
    RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR}
)

enable_testing()
add_subdirectory(tests)
