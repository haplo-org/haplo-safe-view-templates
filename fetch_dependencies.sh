#!/bin/sh

DOWNLOADS_DIR=target/downloads
OUTPUT_DIR=target/lib
TEMPORARY_DOWNLOAD_FILE=$DOWNLOADS_DIR/_tmp_download

# ---------------------------------------------------------------------------

set -e

download_dependencies() {
    get_file \
        JRuby \
        https://search.maven.org/remotecontent?filepath=org/jruby/jruby-complete/9.0.4.0/jruby-complete-9.0.4.0.jar \
        $OUTPUT_DIR/jruby-complete.jar \
        4f094b4b7915def9d1cd35ce69ee12c1f102c8b2
    get_file \
        Rhino \
        https://github.com/mozilla/rhino/releases/download/Rhino1_7_7_RELEASE/rhino1.7.7.zip \
        $DOWNLOADS_DIR/rhino1.7.7.zip \
        01209a126a0b27d37f923a5408298e3072b9433c
    unzip -p $DOWNLOADS_DIR/rhino1.7.7.zip rhino1.7.7/js.jar > $OUTPUT_DIR/rhino.jar
}

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
        curl -fL $GET_URL > $TEMPORARY_DOWNLOAD_FILE
        DOWNLOAD_DIGEST=`openssl sha1 < $TEMPORARY_DOWNLOAD_FILE`
        if [ "$GET_DIGEST" = "$DOWNLOAD_DIGEST" -o "(stdin)= $GET_DIGEST" = "$DOWNLOAD_DIGEST" ]; then
            mv $TEMPORARY_DOWNLOAD_FILE $GET_FILE
        else
            rm $TEMPORARY_DOWNLOAD_FILE
            echo "Digest of ${GET_NAME} download was incorrect, expected ${GET_DIGEST}, got ${DOWNLOAD_DIGEST}"
            exit 1
        fi
    fi
}

mkdir -p $DOWNLOADS_DIR
mkdir -p $OUTPUT_DIR
download_dependencies
