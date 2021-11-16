set SCRIPT_DIR=%~dp0
java -Xms5G -Xmx12G -Xss5G -XX:+CMSClassUnloadingEnabled -jar "%SCRIPT_DIR%sbt-launch.jar" %*