#!/usr/bin/env bash

SCRIPT_NAME="zauberon.sh"
LAST_EXIT_CODE=0

cleanup () {
  rm uberjar-errors.txt
  rm -rf target
}

build_uberjar () {
    echo -n "Building Uberjar ... "
    lein uberjar 2>uberjar-errors.txt 1>/dev/null
    LAST_EXIT_CODE=$?
    if [ "$LAST_EXIT_CODE" != "0" ]; then
        echo "Errors"
        echo
        cat uberjar-errors.txt
        echo
        return $LAST_EXIT_CODE
    fi
    echo "Done"
}

build_executable_uberjar () {
    echo -n "Building Executable Jar ... "
    cat "scripts/uberscript-stub.sh" target/uberjar/*-standalone.jar > $SCRIPT_NAME
    LAST_EXIT_CODE=$?
    if [ "$LAST_EXIT_CODE" != "0" ]; then
        echo "Error"
        rm $SCRIPT_NAME 2>/dev/null >/dev/null
        return $LAST_EXIT_CODE
    fi
    echo "Done"

    echo -n "Making It Executable ... "
    chmod +x $SCRIPT_NAME
    LAST_EXIT_CODE=$?
    if [ "$LAST_EXIT_CODE" != "0" ]; then
      echo "Error"
      return $LAST_EXIT_CODE
    fi
    echo "Done"
}

handle_error () { # <message>
    if [ "$LAST_EXIT_CODE" != "0" ]; then
        echo "***** Error: Could Not $1"
        exit_build_directory
        exit 1
    fi
}

render_header () {
    echo
    echo '========================================'
    echo '== make build'
    echo '========================================'
}

################################################################################
## Main
################################################################################
render_header
build_uberjar ; handle_error "Build Uberjar"
build_executable_uberjar ; handle_error "Build Executable Uberjar"
cleanup ; handle_error "Cleanup"
exit 0