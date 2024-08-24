cmake_minimum_required(VERSION 3.22)
project(heap-assignment-tracing-agent)

set(JAVA_AWT_LIBRARY NotNeeded)
set(JAVA_AWT_INCLUDE_PATH NotNeeded)
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

set(CMAKE_CXX_STANDARD 20)

add_library(heap-assignment-tracing-agent SHARED main.cpp settings.h JvmtiWrapper.h)

target_link_libraries(heap-assignment-tracing-agent -static-libgcc -static-libstdc++)
