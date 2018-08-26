@echo off
rem Brain4it server startup

start java -cp ../lib/* org.brain4it.server.standalone.Runner ../conf/server.properties
