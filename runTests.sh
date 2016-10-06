#!/bin/sh
#
# Run tests
#

export command="./activator -Dconfig.file=modules/ginas/conf/ginas.conf -Dtestconfig=conf/ginas.conf \"ginas/testOnly {0}\""
./activator -Dconfig.file=modules/ginas/conf/ginas.conf -Dtestconfig=conf/ginas.conf  "ginas/testOnly ix.test.RunAllGinasTests"
