#!/bin/sh
#
# Run tests
#


export command="./activator -Dconfig.file=modules/ginas/conf/ginas-dev.conf \"ginas/testOnly {0}\" $@"
./activator -Dconfig.file=modules/ginas/conf/ginas-dev.conf  clean ginas/clean "ginas/testOnly ix.test.RunAllGinasTests" "$@"

perl parseTestResults.pl modules/ginas/target/test-reports/