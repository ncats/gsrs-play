set SCRIPT_DIR=%~dp0
java -Xms5G -Xmx10G -Xss5G -XX:+CMSClassUnloadingEnabled -jar "%SCRIPT_DIR%sbt-launch.jar" %*