# G-SRS

Global Substance Registration System assists agencies in 
registering and documenting information about substances 
found in medicines. It provides a 
common identifier for all of the substances 
used in medicinal products, utilizing a 
consistent definition of substances globally, 
including active substances under clinical 
investigation, consistent with the **ISO 11238** standard.

## Check out the code

The code can be checked out of this git repository.  There are several important branches:
* GSRS_DEV - this is the main bleeding edge development branch.

## Config File
Configuration is controlled by a Play ConfigFile.  
The default G-SRS conf file is located in 
`modules/ginas/conf/ginas.conf` 
This file can be extended to provide custom configuration. For development and testing,
the simple extension of this file enabling evolutions is typically used:
`modules/ginas/conf/ginas-dev.conf` 


For more information on how Play ConfigFiles work see [The Playframework Documentation](https://www.playframework.com/documentation/2.5.x/ConfigFile)

## How To Build and Run
To build a particular app, simply use ```sbt``` or the provided
```activator``` script. Here is an example of running a local instance
of the GInAS app:

```
sbt -Dconfig.file=modules/ginas/conf/ginas-dev.conf ginas/run
```

(Instead of ```sbt```, you can also use ```./activator``` instead.)
Now simply point your browser to [http://localhost:9000/ginas](http://localhost:9000/ginas).
Once there, Click the 'Apply SQL script now' button under the header. You may 
have to apply several SQL scripts as part of the evolutions step, depending on
how many datasources are used. After the page has loaded again, change the 
address to [http://localhost:9000/ginas/app](http://localhost:9000/ginas/app).



## How to Choose Cheminformatics Library Toolkit Dependency
G-SRS uses [Molwitch](https://github.com/ncats/molwitch) to allow switching the underlying 
cheminformatics library Toolkit being used for various computations.  By Default, G-SRS
uses [CDK](https://cdk.github.io/) but a stripped down old version of [Chemaxon's JChem](https://chemaxon.com/) is also 
included for evalutation purposes.  If you choose to use JChem as your Cheminformatics Library Toolkit, contact 
Chemaxon about obtaining a license.

To enable molwitch-jchem3 add the property `-Dmolwitch=jchem3` to your activator or sbt invocation:

```
sbt -Dconfig.file=modules/ginas/conf/ginas-dev.conf -Dmolwitch=jchem3 ginas/run
```

To explicitly, enable CDK add the property `-Dmolwitch=cdk` to your activator or sbt invocation:

```
sbt -Dconfig.file=modules/ginas/conf/ginas-dev.conf -Dmolwitch=cdk ginas/run
```
Not setting a molwitch property will use CDK as default.


###Changing the Structure Standardizer and Structure Hasher
G-SRS comes with more than one Structure Standardizer and Structure Hasher implementations that are used
for fuzzy structure matching.  By default standardizers and hash implementations based on InChI are used
but it is also possible to use  [LyChI](https://github.com/ncats/lychi) instead by setting
the properties:   `ix.structure-standardizer="ix.core.chem.LychiStandardizer"` and  `ix.structure-hasher="ix.core.chem.LychiStructureHasher"`
As of this writing LyChI requires jchem so you need to enable molwitch-jchem3 as well.

this can be done by adding these properties to the command line invocation as seen below, or adding them to the conf file.
                       ```

```
$ ./activator -Dconfig.file=modules/ginas/conf/ginas-dev.conf ginas/run -Dmolwitch=jchem3 -Dix.structure-standardizer="ix.core.chem.LychiStandardizer" -Dix.structure-hasher="ix.core.chem.LychiStructureHasher"
```

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


##How to Run Automated Tests
This software contains over one thousand automated tests including unit and end to end tests
that take a long time to run.


###Running one Test Class Only
The Play framework can run a single JUnit test class using `ginas/testOnly $fullyqualitiedTestClass`

So for example to run all the tests in `ix.test.EditingWorkflowTest` class this would be the invocation

```
./activator -Dconfig.file=modules/ginas/conf/ginas-dev.conf ginas/clean "ginas/testOnly ix.test.EditingWorkflowTest"
```


###Running all Tests

**Do Not Use `ginas/test`**

Earlier versions of the Play framework had memory leaks which caused running all the tests
to run out of memory when running all the tests using `ginas/test` so a custom JUnit test runner was written to invoke each test class serially
in a separate JVM.  This special test runner can be invoked by running the `ix.test.RunAllGinasTests`
class.

To make it easier to run and avoid having to escape quotes or spaces on the commandline,
this class reads an enviornment variable which contains the invocation.  the `{0}` part
will be replaced with the test to be run.  Keep the `{0}` in the variable. the TestRunner will know
to replace it.

```
export command="./activator -Dconfig.file=modules/ginas/conf/ginas-dev.conf \"ginas/testOnly {0}\""

./activator -Dconfig.file=modules/ginas/conf/ginas-dev.conf  "ginas/testOnly ix.test.RunAllGinasTests"

```

Warning running this program could take a few hours to run.  This is recommended to only be invoked by a continuous integration system.



## Authors
The software tools created by the this project are developed, maintained, and distributed to ginas and other interested parties by the National Center for Advancing Translational Sciences (NCATS) at the National Institutes of Health (NIH), in close collaboration with the Food and Drug Administration (FDA). 
