#G-SRS

Global Substance Registration System assists agencies in 
registering and documenting information about substances 
found in medicines. It provides a 
common identifier for all of the substances 
used in medicinal products, utilizing a 
consistent definition of substances globally, 
including active substances under clinical 
investigation, consistent with the **ISO 11238** standard.

##Check out the code

The code can be checked out of this git repository.  There are several important branches:
* GSRS_DEV - this is the main bleeding edge development branch.
* additionaldata - This is an experimental branch that is being used to incorporate additional data sources to provide even more information on the status of each substance.

## Config File
Configuration is controlled by a Play ConfigFile.  
The default G-SRS conf file is located in 
`modules/ginas/conf/ginas.conf` 
This file can be extended to provide custom configuration.

For more information on how Play ConfigFiles work see [The Playframework Documentation](https://www.playframework.com/documentation/2.5.x/ConfigFile)

##How To Build and Run
To build a particular app, simply use ```sbt``` or the provided
```activator``` script. Here is an example of running a local instance
of the GInAS app:

```
sbt -Dconfig.file=modules/ginas/conf/ginas.conf ginas/run
```

(Instead of ```sbt```, you can also use ```./activator``` instead.)
Now simply point your browser to [http://localhost:9000/ginas](http://localhost:9000/ginas).



### Build a Self-Contained Distribution
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

### How To Clean The Code
To clean up, simply issue:

```
sbt clean
sbt ginas/clean
```

##Authors
The software tools created by the this project are developed, maintained, and distributed to ginas and other interested parties by the National Center for Advancing Translational Sciences (NCATS) at the National Institutes of Health (NIH), in close collaboration with the Food and Drug Administration (FDA). 