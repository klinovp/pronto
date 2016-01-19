#!/bin/sh

java -Xss4m -Xmx2G -Djava.library.path=/usr/local/lib/jni -Djava.util.logging.config.file= -jar dist/lib/pronto.jar "$@"
