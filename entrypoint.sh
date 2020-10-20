#!/bin/sh
if [ "x$GSRS_CACHE_RESET" = "xtrue" -o "x$GSRS_DB_RESET" = "xtrue" ]; then
    echo "rm -rf ginas.ix/*"
fi
if [ "x$GSRS_DB_RESET" = "xtrue" ]; then
    CONFIG_FILE_OPT=`echo $JAVA_OPTS | grep -o "\-Dconfig.file *= *[^ ]*"`
    if [ "x$CONFIG_FILE_OPT" = "x" ]; then
        CONFIG_FILE_OPT="-Dconfig.file=conf/ginas.conf"
    fi
    java -cp "lib/*" $CONFIG_FILE_OPT ix.ginas.utils.Evolution
fi
exec "$@"
