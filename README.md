NCATS InXight Platform
======================

Please consult the wiki for additional details. The InXight plaform
consists of two core components ```core``` and ```ncats```. The ```core```
component comprises of only models and core logic controllers. The
```ncats``` component provides a common layer for controllers and views
to build an app. All existing apps developed within the InXight platform
are available in the ```modules``` directory.

To build a particular app, simply use ```sbt``` or the provided
```activator``` script. Here is an example of running a local instance
of the GInAS app:

```
sbt -Dconfig.file=modules/ginas/conf/ginas.conf ginas/run
```

(Instead of ```sbt```, you can also use ```./activator``` instead.)
Now simply point your browser to [http://localhost:9000/ginas](http://localhost:9000/ginas).

To build a self-contained distribution for production use, simply run
the following command:

```
sbt -Dconfig.file=modules/ginas/conf/ginas.conf ginas/dist
```

If all goes well, this should create a zip file under
```modules/ginas/target/universal/``` of the form

```
{app}-{branch}-{commit}-{date}-{time}.zip
```

where ```{app}``` is the app name (e.g., ```ginas```), ```{branch}``` is
the current git branch, ```{commit}``` is the 7-character git commit hash,
```{date}``` is the current date, and ```{time}``` is the current time.
Now this self-contained zip file can be deployed in production, e.g.,

```
unzip ginas-alpha_v0-0f75de1-20150618-002011.zip
cd ginas-alpha_v0-0f75de1-20150618-002011
./bin/ginas -Dconfig.resource=ginas.conf -Dhttp.port=9000 -Djava.awt.headless=true
```

To clean up, simply issue:

```
sbt clean
sbt ginas/clean
```