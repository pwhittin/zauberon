#!/usr/bin/env bash

LAST_EXIT_CODE=0

format_files () {
    echo -n "Formatting Files ... "
     lein cljfmt fix 2>/dev/null >/dev/null
    LAST_EXIT_CODE=$?
    echo "Done"
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
    echo '== make format'
    echo '========================================'
}

################################################################################
## Main
################################################################################
render_header
format_files ; handle_error "Formatting Files"
exit 0