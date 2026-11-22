qt_add_executable(tst_backoff tst_backoff.cpp)
target_link_libraries(tst_backoff PRIVATE fortal_core Qt6::Test)
add_test(NAME tst_backoff COMMAND tst_backoff)

qt_add_executable(tst_protocol tst_protocol.cpp)
target_link_libraries(tst_protocol PRIVATE fortal_core Qt6::Test)
add_test(NAME tst_protocol COMMAND tst_protocol)
