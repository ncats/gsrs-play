#!/bin/sh
#
# Run tests
#


export command="./activator -Dconfig.file=modules/ginas/conf/ginas-dev.conf \"ginas/testOnly {0}\" $@"
./activator -Dconfig.file=modules/ginas/conf/ginas-dev.conf  "ginas/testOnly ix.test.RunAllGinasTests" "$@"
