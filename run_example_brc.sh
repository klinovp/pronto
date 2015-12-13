#!/bin/sh

java -Xss4m -Xms30m -Xmx200m -Djava.util.logging.config.file= -jar lib/pronto.jar file:examples/brc/ontologies/cancer_cc.owl s file:examples/brc/benchmarksuite.ppq