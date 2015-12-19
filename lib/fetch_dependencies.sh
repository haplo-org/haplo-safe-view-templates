#!/bin/sh

RHINO_FILENAME=rhino1.7.7.zip
RHINO_JAR_DIR=rhino1.7.7
RHINO_URL=https://github.com/mozilla/rhino/releases/download/Rhino1_7_7_RELEASE/${RHINO_FILENAME}
RHINO_DIGEST=01209a126a0b27d37f923a5408298e3072b9433c

cd lib

# ---------------------------------------------------------------------------

set -e

RHINO_JAR_IN_ZIP=${RHINO_JAR_DIR}/js.jar

if ! which curl; then
    echo curl is not available, cannot fetch archives
    exit 1
fi
if ! which openssl; then
    echo openssl is not available, cannot verify archives
    exit 1
fi

get_file() {
    GET_NAME=$1
    GET_URL=$2
    GET_FILE=$3
    GET_DIGEST=$4
    if [ -f $GET_FILE ]; then
        echo "${GET_NAME} already downloaded."
    else
        echo "Downloading ${GET_NAME}..."
        curl -L $GET_URL > _tmp_download
        DOWNLOAD_DIGEST=`openssl sha1 < _tmp_download`
        if [ "$GET_DIGEST" = "$DOWNLOAD_DIGEST" -o "(stdin)= $GET_DIGEST" = "$DOWNLOAD_DIGEST" ]; then
            mv _tmp_download $GET_FILE
        else
            rm _tmp_download
            echo "Digest of ${GET_NAME} download was incorrect, expected ${GET_DIGEST}, got ${DOWNLOAD_DIGEST}"
            exit 1
        fi
    fi
}

# ---------------------------------------------------------------------------

mkdir -p downloads
get_file Rhino $RHINO_URL downloads/$RHINO_FILENAME $RHINO_DIGEST
unzip downloads/$RHINO_FILENAME $RHINO_JAR_IN_ZIP
mv $RHINO_JAR_IN_ZIP js.jar
rmdir $RHINO_JAR_DIR
