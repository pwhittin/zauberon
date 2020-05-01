#!/usr/bin/env bash

LAST_EXIT_CODE=0

check_dependencies() {
  lein ancient
  LAST_EXIT_CODE=$?
}

handle_error() { # <message>
  if [ "$LAST_EXIT_CODE" != "0" ]; then
    echo "***** Error: Could Not $1"
    exit 1
  fi
}

render_header() {
  echo
  echo '========================================'
  echo '== make check'
  echo '========================================'
}

################################################################################
## Main
################################################################################
render_header
check_dependencies
handle_error "Checking Dependencies"
exit 0
