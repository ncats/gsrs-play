#!/bin/sh
###################################################################

#Script Name    :   evolutions.sh
#Description    :   runs evolutions to create the schema needed for GSRS
#Args           :   $1 is the config file path (optional), where the
#                   connections to database are established
###################################################################

if [ -z "$1" ]; then
    CONFIG_FILE=`echo $JAVA_OPTS | grep -o "\-Dconfig.file=[^ ]*" | cut -d= -f2`
else
    CONFIG_FILE=$1
fi

if [ -f "$CONFIG_FILE" ]; then
    java -cp "lib/*" -Dconfig.file=$CONFIG_FILE ix.ginas.utils.Evolution
    if [ -d "ginas.ix" ]; then
        rm -rf ginas.ix/*
    fi
fi
