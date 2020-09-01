#!/bin/sh
###################################################################

#Script Name	:   evolutions.sh                                                                                           
#Description	:   runs evolutions to create the schema needed for GSRS                                                                 
#Args           :   $1 is the config file path, where the connections to
#         			database are established                                                   
###################################################################

java -cp "lib/*" -Dconfig.file=$1 ix.ginas.utils.Evolution
