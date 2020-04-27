#!/usr/bin/env bash

lint () {
    clj-kondo --lint src
    LAST_EXIT_CODE=$?
    echo
}

handle_error () { # <message>
    if [ "$LAST_EXIT_CODE" != "0" ]; then
        echo "***** Error: Could Not $1"
        exit 1
    fi
}

render_header () {
    echo
    echo '========================================'
    echo '== make lint'
    echo '========================================'
}

################################################################################
## Main
################################################################################
render_header
lint ; handle_error "Lint"
exit 0
