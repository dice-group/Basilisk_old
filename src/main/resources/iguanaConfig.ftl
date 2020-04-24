##### TRIPLE STORE CONFIG ####
iguana.rp.storages=blazegraph
blazegraph.class=org.aksw.iguana.rp.storage.impl.TriplestoreStorage
blazegraph.constructorArgs=http://localhost:9999/sparql,http://localhost:9999/sparql

# The Readable Name you want to give the triple store
connection1.name=${connName}
# The sparql Endpoint of the triple store
connection1.service=http://0.0.0.0:${port}/sparql
# The update Endpoint of the triple store (this is optional, if not provided Iguana will use the service address)
#connection1.update.service=http://localhost:9999/blazegraph/sparql
#connection1.user =
#connection1.password =

iguana.cc.connections=connection1


##### DATASET CONFIG #####
# The Readable Name you want to give your Dataset
dataset1.name=${datasetName}

iguana.cc.datasets=dataset1

##### TASK CONFIG #####


stresstest.class=org.aksw.iguana.tp.tasks.impl.stresstest.Stresstest
stresstest2.class=org.aksw.iguana.tp.tasks.impl.stresstest.Stresstest
stresstest3.class=org.aksw.iguana.tp.tasks.impl.stresstest.Stresstest
stresstest4.class=org.aksw.iguana.tp.tasks.impl.stresstest.Stresstest
stresstest5.class=org.aksw.iguana.tp.tasks.impl.stresstest.Stresstest

#end restricition
stresstestArg.timeLimit=360000
#stresstestArg.noOfQueryMixes=1000

#warmup, We do not add a warmup here, but you should get the drill, time is in MS
stresstestArg.warmupTime=0
stresstestArg.warmupQueries=/dev/null
stresstestArg.warmupUpdates=/dev/null



#sparqlConfig1=#OF SIMULATED USERS, CLASS, TIMEOUT, QUERIES FILE, SIMUALTED FIXED NETWORK LATENCY, SIMULATED GAUSSIAN RANDOM NETWORK LATENCY

sparqlConfig1=1,org.aksw.iguana.tp.tasks.impl.stresstest.worker.impl.SPARQLWorker, 180000, ${testData}, 0, 0
sparqlConfig2=4,org.aksw.iguana.tp.tasks.impl.stresstest.worker.impl.SPARQLWorker, 180000, ${testData}, 0, 0
sparqlConfig3=8,org.aksw.iguana.tp.tasks.impl.stresstest.worker.impl.SPARQLWorker, 180000, ${testData}, 0, 0
sparqlConfig4=16,org.aksw.iguana.tp.tasks.impl.stresstest.worker.impl.SPARQLWorker, 180000, ${testData}, 0, 0
sparqlConfig5=32,org.aksw.iguana.tp.tasks.impl.stresstest.worker.impl.SPARQLWorker, 180000, ${testData}, 0, 0

stresstestArg.workers=sparqlConfig1
stresstestArg.queryHandler=org.aksw.iguana.tp.query.impl.InstancesQueryHandler
# or if your Query handler has Constructor Arguments
# stresstestArg.queryHandler=org.example.com.MyQueryHandler, arg1, arg2,...

#e.g. if you want to use query patterns using the dbpedia dataset as a background
#stresstestArg.queryHandler=org.aksw.iguana.tp.query.impl.PatternQueryHandler, http://dbpedia.org/sparql

stresstestArg2.workers=sparqlConfig2
stresstestArg3.workers=sparqlConfig3
stresstestArg4.workers=sparqlConfig4
stresstestArg5.workers=sparqlConfig5

stresstest.constructorArgs=stresstestArg.timeLimit,stresstestArg.queryHandler,stresstestArg.workers,stresstestArg.warmupTime,stresstestArg.warmupQueries,stresstestArg.warmupUpdates
stresstest2.constructorArgs=stresstestArg.timeLimit,stresstestArg.queryHandler,stresstestArg2.workers,stresstestArg.warmupTime,stresstestArg.warmupQueries,stresstestArg.warmupUpdates
stresstest3.constructorArgs=stresstestArg.timeLimit,stresstestArg.queryHandler,stresstestArg3.workers,stresstestArg.warmupTime,stresstestArg.warmupQueries,stresstestArg.warmupUpdates
stresstest4.constructorArgs=stresstestArg.timeLimit,stresstestArg.queryHandler,stresstestArg4.workers,stresstestArg.warmupTime,stresstestArg.warmupQueries,stresstestArg.warmupUpdates
stresstest5.constructorArgs=stresstestArg.timeLimit,stresstestArg.queryHandler,stresstestArg5.workers,stresstestArg.warmupTime,stresstestArg.warmupQueries,stresstestArg.warmupUpdates

iguana.rp.metrics=qps, qmph, eqe
qps.class=org.aksw.iguana.rp.metrics.impl.QPSMetric
qmph.class=org.aksw.iguana.rp.metrics.impl.QMPHMetric
eqe.class=org.aksw.iguana.rp.metrics.impl.EachQueryMetric
iguana.cc.tasks = stresstest,stresstest2,stresstest3,stresstest4,stresstest5
