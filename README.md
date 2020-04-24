# Basilisk
A Continuous Benchmarking Platform for Tentris, Virtuoso & Apache Jena Fuseki triple stores using Iguana (https://github.com/dice-group/IGUANA).

* Tentris : https://github.com/dice-group/tentris
* Virtuoso : https://github.com/openlink/virtuoso-opensource
* Apache Jena Fuseki : https://github.com/apache/jena

### iguanaConfig.ftl
This is the configuration file(Basilisk/src/main/resources/iguanaConfig.ftl) which is used as a template for running benchmarking using Iguana. Make modifications here in case you want to make changes like the number of clients used for benchmarking, etc.

### Metadata.json
The current file(Basilisk/continuousBM/Metadata.json) contains entry just for Tentris. The entry for Virtuoso and Fuseki should be added in future using the below json:

```json
[{
  "name": "Tentris",
  "command": "curl https://api.github.com/repos/dice-group/tentris/tags",
  "port": "9080",
  "dataset": "sp2b.nt",
  "queriesFilePath": "sp2b.txt"
},
{
  "name": "Virtuoso",
  "command": "curl https://api.github.com/repos/openlink/virtuoso-opensource/tags",
  "port": "8890",
  "dataset": "",
  "queriesFilePath": ""
},
{
  "name": "Fuseki",
  "command": "curl https://api.github.com/repos/apache/jena/tags",
  "port": "",
  "dataset": "",
  "queriesFilePath": ""
}]
```

### BenchmarkingAttempted.json
This file(Basilisk/continuousBM/BenchmarkingAttempted.json) contains the versions of the triple stores on which benchmarking has been attempted once. The entry of a version in this file means that benchmarking will be skipped for it in subsequent runs. If you want to run benchmarking for a particular version of a triple store, remove that version number/name from this file.
