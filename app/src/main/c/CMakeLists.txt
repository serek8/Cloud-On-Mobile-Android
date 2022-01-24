cmake_minimum_required(VERSION 3.4.1)
set(can_use_assembler TRUE)
enable_language(ASM)



add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             native-lib.c )


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


target_link_libraries(core-lib

        ${CMAKE_CURRENT_SOURCE_DIR}/Core/libs/android/libssl_${ANDROID_ABI}.a
        ${CMAKE_CURRENT_SOURCE_DIR}/Core/libs/android/libcrypto_${ANDROID_ABI}.a
        )
