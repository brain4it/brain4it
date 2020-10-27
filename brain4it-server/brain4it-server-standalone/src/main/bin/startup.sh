# Brain4it server startup

cd "$(dirname "$0")"
java -cp "../lib/*" -Djava.util.logging.manager=org.brain4it.server.standalone.ServerLogManager -Djava.util.logging.config.file="../conf/logging.properties" org.brain4it.server.standalone.Runner ../conf/server.properties
