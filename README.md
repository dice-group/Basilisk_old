# Basilisk

<img src = "https://raw.githubusercontent.com/ranjithmasthikatte/BasiliskLogo/master/10.png" alt = "Basilisk Logo" width = "400" align = "center">

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

## How to build and run Basilisk?

Download the Basilisk maven project from the git repository.

```shell script
git clone https://github.com/ranjithmasthikatte/Basilisk.git
```

Build the maven project.

```shell script
mvn clean install
```

Run Basilisk using maven command.

```shell script
mvn exec:java -Dexec.args="--admin-user-name <admin user name> --admin-pass <admin password>"
```

If you want to run Basilisk using the jar, use the below command.

```shell script
java -jar basilisk-1.0-SNAPSHOT.jar --admin-user-name <admin user name> --admin-pass <admin password>
```

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
    ├── results                             # Benchmarked result file as nt file
    ├── runStat                             # Basilisk statistics file of a single run
    └── logs                                # Basilisk log

### Things to remember

- **iguana/**

Please place all the test SPARQL queries(txt file) in this directory. The remaining 
files like Iguana jar file and related files will be downloaded by the Basilisk into this folder.

- **testDataSet/**

Please place all the test data set(nt, n3 etc.) to be loaded into the triple store for benchmarking process
into this directory.

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
GitMetaData:
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
created by Basilisk to avoid exception.

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
created by Basilisk to avoid exception.

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

### iguanaConfig.ftl
This is the configuration file(Basilisk/src/main/resources/iguanaConfig.ftl) which is used as a template for running benchmarking using Iguana. Make modifications here in case you want to make changes like the number of clients used for benchmarking, etc.
