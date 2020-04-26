#!/usr/bin/env bash

SCRIPT_NAME="zauberon.sh"

echo
echo -n "Building '$SCRIPT_NAME' JAR File ... "
lein uberjar 2>/dev/null >/dev/null
echo "Done"
echo -n "Building '$SCRIPT_NAME' ... "
cat "uberscript-stub.sh" target/uberjar/*-standalone.jar > $SCRIPT_NAME && chmod +x $SCRIPT_NAME
rm -rf target
echo "Done"
echo

cd $PWD
exit 0
