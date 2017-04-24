#!/bin/sh
if [ -z "$1" ]
then
    echo "Usage: ./activate.sh <file>|[<URL> <EMAIL>]|<license-code>"
    exit 1
fi
java -jar `dirname $0`/../jrebel.jar -activate $@
