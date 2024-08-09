cmake_minimum_required(VERSION 3.6.0)

#
# Argon2 original native library (argon2native)
#

project(argon2native C CXX)
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -Os")

set(argon2native_DIR ${CMAKE_CURRENT_LIST_DIR}/src/main/cpp/argon2)
file(GLOB argon2native_SOURCES
        ${argon2native_DIR}/blake2/blake2b.c
        ${argon2native_DIR}/argon2.c
        ${argon2native_DIR}/core.c
        ${argon2native_DIR}/encoding.c
        ${argon2native_DIR}/ref.c
        ${argon2native_DIR}/thread.c
)

add_library(argon2native SHARED ${argon2native_SOURCES})
target_compile_options(argon2native PRIVATE)
target_include_directories(argon2native PUBLIC ${argon2native_DIR})

# Add support for 16kB page size for Android 15.
target_link_options(${CMAKE_PROJECT_NAME} PRIVATE "-Wl,-z,max-page-size=16384")

#
# JNI integration (argon2jni)
#

project(argon2jni CXX)
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -Os")

set(argon2jni_DIR ${CMAKE_CURRENT_LIST_DIR}/src/main/cpp/jni)
file(GLOB argon2jni_SOURCES
        ${argon2jni_DIR}/*.cpp
)

add_library(argon2jni SHARED ${argon2jni_SOURCES})
target_compile_options(argon2jni PRIVATE)
target_include_directories(argon2jni PUBLIC ${argon2jni_DIR})

target_link_libraries(argon2jni argon2native)

# Add support for 16kB page size for Android 15.
target_link_options(${CMAKE_PROJECT_NAME} PRIVATE "-Wl,-z,max-page-size=16384")
