#!/bin/bash

EXT_DIR="/tmp/build/modules/extensions"
SRS_DIR="/opt/g-srs"
TMP_DIR="/tmp"

if [ -z "$XLINT" ]; then
    XLINT="none"
fi

if [ ! -z $1 ]; then
    EXT_DIR=$1
fi

if [ ! -d $EXT_DIR ] || [ -z "$(ls -A $EXT_DIR )" ]; then
    exit
fi

if [ ! -d $EXT_DIR/app ] || [ -z "$(ls -A $EXT_DIR/app )" ]; then
    exit
fi

if [ ! -z $2 ]; then
    SRS_DIR=$2
fi

if [ ! -d $SRS_DIR ] || [ -z "$(ls -A $SRS_DIR )" ]; then
    exit
fi

if [ ! -z $3 ]; then
    TMP_DIR=$3
fi

if [ ! -d $TMP_DIR ]; then
    exit
fi

SRS_VER=`ls $SRS_DIR/lib/core.core-*.jar | sed -e "s/.*\/core\.core-\(.*-[0-9]*-[0-9]*-[0-9a-f]*\)\.jar/\1/g"`

if [ -f $EXT_DIR/lib/extensions.extensions-$SRS_VER.jar ]; then
    exit
fi

mkdir -p $TMP_DIR/extensions/META-INF
echo "Manifest-Version: 1.0" > $TMP_DIR/extensions/META-INF/MANIFEST.MF
echo "Implementation-Title: extensions" >> $TMP_DIR/extensions/META-INF/MANIFEST.MF
echo "Implementation-Version: $SRS_VER" >> $TMP_DIR/extensions/META-INF/MANIFEST.MF
echo "Specification-Vendor: extensions" >> $TMP_DIR/extensions/META-INF/MANIFEST.MF
echo "Specification-Title: extensions" >> $TMP_DIR/extensions/META-INF/MANIFEST.MF
echo "Implementation-Vendor-Id: extensions" >> $TMP_DIR/extensions/META-INF/MANIFEST.MF
echo "Specification-Version: $SRS_VER" >> $TMP_DIR/extensions/META-INF/MANIFEST.MF
echo "Implementation-Vendor: extensions" >> $TMP_DIR/extensions/META-INF/MANIFEST.MF
echo "Main-Class: play.core.server.NettyServer" >> $TMP_DIR/extensions/META-INF/MANIFEST.MF

for fn in $(find $EXT_DIR/app -type f -name \*.java); do
   echo "Building: ${fn}"
   javac -g -cp $SRS_DIR/lib/*:$EXT_DIR/lib/*:. -sourcepath $EXT_DIR/app -d $TMP_DIR/extensions -Xlint:$XLINT -source 1.8 -target 1.8 -encoding UTF-8 $fn
done

if [ -d $EXT_DIR/conf ]; then
    cp -f $EXT_DIR/conf/* $SRS_DIR/conf/
fi
if [ -d $EXT_DIR/cv ]; then
    cp -f $EXT_DIR/cv/* $SRS_DIR/cv/
fi
cd $TMP_DIR/extensions
jar cfm $EXT_DIR/lib/extensions.extensions-$SRS_VER.jar $TMP_DIR/extensions/META-INF/MANIFEST.MF *
rm -rf $TMP_DIR/extensions
sed -i "s/app_classpath=\".lib_dir/app_classpath=\"\$lib_dir\/extensions.extensions-$SRS_VER.jar:\$lib_dir/g" /opt/g-srs/bin/ginas
