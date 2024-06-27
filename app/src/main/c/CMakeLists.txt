cmake_minimum_required(VERSION 3.4.1)
set(can_use_assembler TRUE)
enable_language(ASM)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fvisibility=hidden")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fvisibility=hidden")

include("/Users/jan/gs/dexguard/DexGuard-NDK-4_5_0_stable-macos/plugins/cmake/bitguard.cmake")
set(BITGUARD_CONFIG "bitguard.yml")

#add_library( # Sets the name of the library.
#             native-lib
#
#             # Sets the library as a shared library.
#             SHARED
#
#             # Provides a relative path to your source file(s).
#             native-lib.c )


#add_compile_options(-g)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

add_library(
        core-lib

        SHARED

        Core/base64/buffer.c
        Core/base64/encode.c
        Core/base64/decode.c
        Core/FileManager/FileManager.c
        Core/FileManager/FileEncryptor.c
        Core/TcpClient/TcpParser.c
        Core/TcpClient/TcpClient.c
        Core/TcpClient/TlsClient.c
        Core/cJSON.c
        Core/cJSON_Utils.c
        CoreWrapper.c
        )

target_include_directories(core-lib PRIVATE
        Core/include
        Core)

target_link_libraries( # Specifies the target library.
        core-lib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} )

add_library( c++_shared SHARED IMPORTED )
set_target_properties(c++_shared PROPERTIES IMPORTED_LOCATION /Users/jan/Programowanie/PhoneDrive/android/project/app/libs/arm64-v8a/libc++_shared.so)

target_link_libraries(core-lib
        stdc++
        c++_shared
        ${CMAKE_CURRENT_SOURCE_DIR}/Core/libs/android-bc/libssl_${ANDROID_ABI}.a
        ${CMAKE_CURRENT_SOURCE_DIR}/Core/libs/android-bc/libcrypto_${ANDROID_ABI}.a
        )

apply_bitguard(core-lib)