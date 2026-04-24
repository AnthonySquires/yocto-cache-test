SUMMARY = "Yocto cache test dummy application"
DESCRIPTION = "Minimal C program that prints its compile date and git commit. \
Used for testing Artifactory premirror caching behaviour."
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d4d66a8e58b7ea32ef3f79d43883a26a"

SRC_URI = "git://github.com/AnthonySquires/yocto-cache-test.git;protocol=https;branch=main"
SRCREV = "277a31e6ceedef75d0986075dc996393a315ecd7"

S = "${WORKDIR}/git"

inherit cmake

BBCLASSEXTEND = "native"
