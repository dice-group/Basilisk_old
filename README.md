# Basilisk

![B logo](BasiliskLogo.png)
## What is Basilisk?

Basilisk is a web application to run the continuous benchmarking
process on triple stores like 
[Tentris](https://github.com/dice-group/tentris), 
[Virtuoso](https://hub.docker.com/r/openlink/virtuoso-opensource-7/) 
and 
[Fuseki](https://github.com/apache/jena). Current implementation
of Basilisk runs CBP on Git and Docker hook.
In Git hook, Basilisk runs CBP on Tentris and Fuseki triple stores and
in Docker hook, runs CBP on Tentris and Virtuoso triple stores.

## Fuseki server setup

First we need to set up the Fuseki server to store the results of 
benchmarking process. Download the Fuseki server using the below command.

```shell script
wget https://downloads.apache.org/jena/binaries/apache-jena-fuseki-3.15.0.zip
```

Unzip the downloaded file.

```shell script
unzip apache-jena-fuseki-3.15.0.zip
```

Place the settings file for Fuseki server

```shell script
cd apache-jena-fuseki-3.15.0
mkdir run
cd run
touch shiro.ini

# Write the below content into the shiro.ini file

[main]
# Development
ssl.enabled = false

plainMatcher=org.apache.shiro.authc.credential.SimpleCredentialsMatcher
#iniRealm=org.apache.shiro.realm.text.IniRealm
iniRealm.credentialsMatcher = $plainMatcher

[users]
# Implicitly adds "iniRealm =  org.apache.shiro.realm.text.IniRealm"
admin=pw

[roles]

[urls]
## Control functions open to anyone
/$/status  = anon
/$/ping    = anon
/$/metrics = anon
/$/** = anon
/**=anon 
```


Run the Fuseki server

```shell script
cd apache-jena-fuseki-3.15.0
nohup ./fuseki-server --update -mem /ds2 &
```

Create below three datasets in the Fuseki server.
- fuseki
- tentris
- virtuoso

```shell script
curl -X POST "http://0.0.0.0:3030/$/datasets?dbType=tdb2&dbName=fuseki"
curl -X POST "http://0.0.0.0:3030/$/datasets?dbType=tdb2&dbName=tentris"
curl -X POST "http://0.0.0.0:3030/$/datasets?dbType=tdb2&dbName=virtuoso"
```

## How to build and run Basilisk?

Download the Basilisk maven project from the git repository.

```shell script
git clone https://github.com/dice-group/Basilisk.git
```

Build the maven project.

```shell script
mvn clean install
```

Run Basilisk using maven command. The options --admin-user-name and --admin-pass
are mandatory options, these options will be used by Basilisk and sets up the
admin account. These credentials are necessary, later to kick off 
the benchmarking process.

```shell script
mvn exec:java -Dexec.args="--admin-user-name <admin user name> --admin-pass <admin password>"
```

If you want to run Basilisk using the jar, use the below command. The below command
runs in background with the nohup command, because if the Basilisk CBP is kicked off then it might take more than 
2 weeks to complete the run if there are more versions to benchmark. This is
because Iguana runs benchmark for 1 hour for each version and for each version Basilisk
runs benchmarking process with 5 different workers settings, therefore Basilisk
runs almost more than 5 hours for each version. If you do not want Iguana to run 
benchmarking process for 1 hour then check [iguanaonfig](#iguanaonfig).

```shell script
nohup java -jar basilisk-1.0-SNAPSHOT.jar --admin-user-name <admin user name> --admin-pass <admin password> &
```

To monitor the Basilisk application that started in background, use the below command.
The above command creates a file called nohup.out in the current directory.

```shell script
tail -f nohup.out
```

To kickoff the continuous benchmarking process, use the below command or open the link in a browser.

```shell script
curl "http://localhost:8080/runbenchmark?userName=<admin user name>&password=<admin password>&hook=<hook value>"
```

\<admin user name> and \<admin password> are the one that is used in the 
--admin-user-name and --admin-pass options while running the Basilisk application.

The hook key is an optional, and it can have below three integer values.
By default, hook value is 3.

- 1 - kickoff benchmark on Git hook
- 2 - kickoff benchmark on Docker hook
- 3 - kickoff benchmark on both Git and Docker  hook

Once the benchmarking process is done by the Basilisk, results will be uploaded in 
to Fuseki server and results in nt file will be stored in results directory.


## Understanding the Basilisk runtime environment

Once the Basilisk application is up and running, it creates a below file 
structure in the directory where the application is ran. 

    ├── basilisk-1.0-SNAPSHOT.jar           # Basilisk jar file
    └── continuousBM/                       # Workspace for CBP
        ├── bmWorkSpace/                    # Building docker image process happens here
        ├── iguana/                         # Iguana jar and related files including the test query files
        ├── testDataSet/                    # Test dataset for benchmarking process
        ├── GitMetaData.yml                 # Information of the triple stores in Git hook
        ├── DockerMetaData.yml              # Information of the triple stores in Docker hook
        ├── DockerBenchmarkedAttempted.yml  # List of all the triple stores in Docker hook and its version that already benchmarked
        └── GitBenchmarkedAttempted.yml     # List of all the triple stores in Git hook and its version that already benchmarked
    ├── results/                            # Benchmarked result file as nt file
    ├── runStat/                            # Basilisk statistics file of a single run
    └── logs/                               # Basilisk log

### Things to remember

- **iguana/**

Please place all the test SPARQL queries(txt file) in this directory. 
If the test query files are not present in this directory, Basilisk will throw an exception.
The remaining 
files like Iguana jar file and related files will be downloaded by the Basilisk into this folder.

- **testDataSet/**

Please place all the test data set(nt, n3 etc.) to be loaded into the triple store for benchmarking process
into this directory.
If the test dataset are not present in this directory, then docker might fail
to run the container.

- **GitMetaData.yml**

This file contains the information of triple stores in Git hook. Basilisk
uses this configuration file to run CBP on Git hook. 

For example, To change the test dataset on tentris, change the 
value of dataset key in this file and place that test data set in testDataSet directory.

```yaml
GitMetaData:
- name: tentris
  command: curl https://api.github.com/repos/dice-group/tentris/tags
  port: '9080'
  dataset: sp2b.nt
  queriesFilePath: sp2b.txt
- name: fuseki
  command: curl https://api.github.com/repos/apache/jena/tags
  port: '9999'
  dataset: sp2b.nt
  queriesFilePath: sp2b.txt
```

- **DockerMetaData.yml**

This file contains the information of triple stores in Docker hook. Basilisk
uses this configuration file to run CBP on Docker hook.

For example, To change the port on tentris, you must change the value 
of port key in this file.

```yaml
DockerMetaData:
- name: tentris
  repositoryName: dicegroup/tentris_server
  command: curl https://registry.hub.docker.com/v2/repositories/dicegroup/tentris_server/tags
  port: '9080'
  dataset: sp2b.nt
  queriesFilePath: sp2b.txt
- name: virtuoso
  repositoryName: openlink/virtuoso-opensource-7
  command: curl https://registry.hub.docker.com/v2/repositories/openlink/virtuoso-opensource-7/tags
  port: '8890'
  dataset: sp2b.nt
  queriesFilePath: sp2b.txt
```

- **DockerBenchmarkedAttempted.yml**

This file contains all the triple stores in Docker hook, and the hash of all its
version on which benchmarking has been attempted once. 
The entry of a version in this file means that the benchmarking will be 
skipped for it in subsequent runs. If you want to run benchmarking for 
a particular version of a triple store again, remove that version's hash
from this file.

Example file looks like below. Please neglect initialTempKickOffData, this is
created by Basilisk to avoid yaml exception.

```yaml
DockerBenchmarkedAttempted:
- tentris:
  - initialTempKickOffData
  - d76469e06dfac68e000714a9a3ffa732cc04cf3e1e860c02a46049d82c37fa5e
  - 33073b98df2a5def586e7c861aa0024ea685f04b859855b45a6ddd9fba2f2703
  - 33fa40bef558a6dfcfa3693cf139488e0fbec8b1d2aa14160d9673e300df45a4
  - 553d1bda86a25a4a2abc09a334bdf6cb8dc6ca57b33de715d60b60501cd5e221
- virtuoso:
  - initialTempKickOffData
  - 25f597486c3f817954658abbd9ccb65b2e862fbb3562916e78f4c1c714f1650d
  - ef3b593ea58164ae604d5d9b51884cfbcb2df8d335b71f2f7a798b3cbb4d9979
  - a5e420c3556335641718ed1f79ccac55a8a8f9148cf26a27383289dbc6d55c35
```

- **GitBenchmarkedAttempted.yml**

This file is similar to DockerBenchmarkedAttempted.yml, but this file is used for
Git hook. If you want to run benchmarking for 
a particular version of a triple store again in Git hook, remove that version's hash
from this file.

Example file looks like below. Please neglect initialTempKickOffData, this is
created by Basilisk to avoid yaml exception.

```yaml
GitBenchmarkedAttempted:
- tentris:
  - initialTempKickOffData
  - 96d06599a51aef6c4d8bff371533624b6613cd24
  - 3530ecd67c498deb82cd89bddca1c6e739383518
  - 1f31c3b8e7d7b40f6fb9cdd236feadd4e7987290
- fuseki:
  - initialTempKickOffData
  - 2100d70ac9e151b7e6087ba09302cca56178a359
  - 91d8671f130edce708a5af7d9fd5dfdb5462b4b5
  - bce91836c86a7eb61a198de606f1265441bfbac6
  - db794b97df9a28a021d994132dd9bbf929e59057
```

# Basilisk run's statistics file

Once the Basilisk completes a benchmark process kicked off by the user,
it creates a statistics file in runStat directory. This file contains the 
informations like counts and list of all triple stores, version,
its hash and status.

Status can be :
- Success
- Fail
- Already ran benchmark on this version

# Iguana configuration template: iguanaConfig.ftl
The file iguanaConfig.ftl in the Basilisk's resource is a template used by the Basilisk during runtime to 
create an Iguana configuration file for each version.
If you want to modify the Iguana configuration file then please update this template.

For example, If you want to run the Iguana for 1 minute, then modify the value of timeLimit in this file as shown below.
 
```shell script
task0x.timeLimit = 60000   # Time in miliseconds
```