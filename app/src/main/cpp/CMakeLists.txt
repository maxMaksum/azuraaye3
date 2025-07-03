cmake_minimum_required(VERSION 3.10)
project(azura_face_lib)

set(CMAKE_CXX_STANDARD 17)

# Include current directory and external/sha256 for header lookup
include_directories(${CMAKE_CURRENT_SOURCE_DIR})
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/external/sha256)

# Use relative path for libzip
add_subdirectory(external/libzip)

# Enable USE_LIBZIP for zip-based hashing
add_definitions(-DUSE_LIBZIP)

add_library(azura_face_lib SHARED
    jni_bridge.cpp
    face_recognizer.cpp
    image_processor.c
    native_integrity_generated.cpp
    security_stub.cpp
    external/sha256/sha256.c
)

find_library(log-lib log)

# Link with libzip
# You may need to adjust the target name if libzip uses a different one
# (commonly 'zip')
target_link_libraries(azura_face_lib
    zip
    ${log-lib}
    atomic
    m
)
