#!/bin/sh
rm runTests.bat
echo Generating runTests.bat
export command="echo \"sbt.bat -Dtestconfig=conf/ginas.conf -Dconfig.file=modules/ginas/conf/ginas.conf %ginas/testOnly {0}%\""
./activator -Dconfig.file=modules/ginas/conf/ginas.conf -Dtestconfig=conf/ginas.conf  "ginas/testOnly ix.test.ix.test.RunAllGinasTests"|grep ^sbt|sed 's/%/\"/g' >>runTests.bat

