#!/bin/sh

cd /opt/g-srs

for dir_name in conf/evolutions/default conf/sql/init conf/sql/load conf/sql/post conf/sql/test exports ginas.ix logs
do
    if [ ! -d /data/$dir_name ]; then
        mkdir -p /data/$dir_name
    fi
done

if [ ! -f conf/evolutions/default/1.sql ]; then
    if [ -f conf/evolutions.save/default/1.sql ]; then
        cp conf/evolutions.save/default/1.sql conf/evolutions/default
    fi
fi

for dir_name in cache h2 payload sequence structure text
do
    if [ -d /data/$dir_name ]; then
        mv /data/$dir_name /data/ginas.ix/$dir_name
    fi
done

if [ "x$GSRS_CACHE_RESET" = "xtrue" ]; then
    rm -rf ginas.ix/*
fi

if [ "x$GSRS_DB_RESET" = "xtrue" ]; then
    bin/evolutions.sh
fi

if [ -f "bin/build_extensions.sh" ]; then
    bin/build_extensions.sh /extensions /opt/g-srs
fi

exec "$@"
