#!/bin/sh

for dir_name in conf/evolutions/default conf/sql/init conf/sql/load conf/sql/post conf/sql/test exports ginas.ix logs
do
    if [ ! -d /data/$dir_name ]; then
        mkdir -p /data/$dir_name
    fi
done

for dir_name in cache h2 payload sequence structure text
do
    if [ -d /data/$dir_name ]; then
        mv /data/$dir_name /data/ginas.ix/$dir_name
    fi
done

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
