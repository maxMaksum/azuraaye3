cmake_minimum_required(VERSION 3.10)
project(azura_face_lib)

set(CMAKE_CXX_STANDARD 17)

# Ensure Release flags
if(NOT CMAKE_BUILD_TYPE)
  set(CMAKE_BUILD_TYPE Release)
endif()

set(CMAKE_C_FLAGS_RELEASE "-O2 -s -fvisibility=hidden")
set(CMAKE_CXX_FLAGS_RELEASE "-O2 -s -fvisibility=hidden")

# Include headers
include_directories(${CMAKE_CURRENT_SOURCE_DIR})
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/external/sha256)

# Add libzip
add_subdirectory(external/libzip)

# Define flags
add_definitions(-DUSE_LIBZIP)

# Create shared library
add_library(azura_face_lib SHARED
    jni_bridge.cpp
    face_recognizer.cpp
    image_processor.c
    native_integrity_generated.cpp
    security_stub.cpp
    external/sha256/sha256.c
)

# Find Android log
find_library(log-lib log)

# Link libraries
target_link_libraries(azura_face_lib
    zip
    ${log-lib}
    atomic
    m
)
