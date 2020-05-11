connection0.name = ${connName}
connection0.service = http://localhost:${port}/sparql
connection0.update.service = http://localhost:${port}/sparql
connection0.user =
connection0.password =
iguana.cc.connections = connection0

eqe.class=org.aksw.iguana.rp.metrics.impl.EachQueryMetric
qmph.class=org.aksw.iguana.rp.metrics.impl.QMPHMetric
qps.class=org.aksw.iguana.rp.metrics.impl.QPSMetric
noqph.class=org.aksw.iguana.rp.metrics.impl.NoQPHMetric
iguana.rp.metrics=eqe,qmph,qps,noqph

dataset0.name = ${datasetName}
iguana.cc.datasets = dataset0

sparql0.numberOfWorkers: 1
sparql0.workerClass: org.aksw.iguana.tp.tasks.impl.stresstest.worker.impl.SPARQLWorker
sparql0.timeout: 180000
sparql0.queryFile: ${queryFile}
sparql0.fixedLatency: 0
sparql0.gaussianLatency: 0
task0.class = org.aksw.iguana.tp.tasks.impl.stresstest.Stresstest
task0x.timeLimit = 3600000
task0x.queryHandler = org.aksw.iguana.tp.query.impl.InstancesQueryHandler
task0x.workers = sparql0
task0.constructorArgs = task0x.timeLimit,task0x.queryHandler,task0x.workers

sparql1.numberOfWorkers: 4
sparql1.workerClass: org.aksw.iguana.tp.tasks.impl.stresstest.worker.impl.SPARQLWorker
sparql1.timeout: 180000
sparql1.queryFile: ${queryFile}
sparql1.fixedLatency: 0
sparql1.gaussianLatency: 0
task1.class = org.aksw.iguana.tp.tasks.impl.stresstest.Stresstest
task1x.timeLimit = 3600000
task1x.queryHandler = org.aksw.iguana.tp.query.impl.InstancesQueryHandler
task1x.workers = sparql1
task1.constructorArgs = task1x.timeLimit,task1x.queryHandler,task1x.workers

sparql2.numberOfWorkers: 8
sparql2.workerClass: org.aksw.iguana.tp.tasks.impl.stresstest.worker.impl.SPARQLWorker
sparql2.timeout: 180000
sparql2.queryFile: ${queryFile}
sparql2.fixedLatency: 0
sparql2.gaussianLatency: 0
task2.class = org.aksw.iguana.tp.tasks.impl.stresstest.Stresstest
task2x.timeLimit = 3600000
task2x.queryHandler = org.aksw.iguana.tp.query.impl.InstancesQueryHandler
task2x.workers = sparql2
task2.constructorArgs = task2x.timeLimit,task2x.queryHandler,task2x.workers

sparql3.numberOfWorkers: 16
sparql3.workerClass: org.aksw.iguana.tp.tasks.impl.stresstest.worker.impl.SPARQLWorker
sparql3.timeout: 180000
sparql3.queryFile: ${queryFile}
sparql3.fixedLatency: 0
sparql3.gaussianLatency: 0
task3.class = org.aksw.iguana.tp.tasks.impl.stresstest.Stresstest
task3x.timeLimit = 3600000
task3x.queryHandler = org.aksw.iguana.tp.query.impl.InstancesQueryHandler
task3x.workers = sparql3
task3.constructorArgs = task3x.timeLimit,task3x.queryHandler,task3x.workers


sparql4.numberOfWorkers: 32
sparql4.workerClass: org.aksw.iguana.tp.tasks.impl.stresstest.worker.impl.SPARQLWorker
sparql4.timeout: 180000
sparql4.queryFile: ${queryFile}
sparql4.fixedLatency: 0
sparql4.gaussianLatency: 0
task4.class = org.aksw.iguana.tp.tasks.impl.stresstest.Stresstest
task4x.timeLimit = 3600000
task4x.queryHandler = org.aksw.iguana.tp.query.impl.InstancesQueryHandler
task4x.workers = sparql4
task4.constructorArgs = task4x.timeLimit,task4x.queryHandler,task4x.workers
iguana.cc.tasks = task0, task1, task2, task3, task4