cmake_minimum_required(VERSION 3.10)
project(yocto-cache-test C)

# Get git commit + dirty flag
execute_process(
    COMMAND git describe --always --dirty
    WORKING_DIRECTORY ${CMAKE_SOURCE_DIR}
    OUTPUT_VARIABLE GIT_COMMIT
    OUTPUT_STRIP_TRAILING_WHITESPACE
    ERROR_QUIET
)
if(NOT GIT_COMMIT)
    set(GIT_COMMIT "unknown")
endif()

# Compile timestamp
string(TIMESTAMP COMPILE_DATE "%Y-%m-%d %H:%M:%S")

# Generate version.h from template
configure_file(
    ${CMAKE_SOURCE_DIR}/src/version.h.in
    ${CMAKE_BINARY_DIR}/version.h
)

add_executable(yocto-cache-test src/main.c)
target_include_directories(yocto-cache-test PRIVATE ${CMAKE_BINARY_DIR})

install(TARGETS yocto-cache-test DESTINATION bin)
