sudo /usr/bin/java -Djava.library.path=natives/armv7l \
	-Dlogback.configurationFile=config/logback.xml \
	-cp bridge-iqrf-mqtt-1.1.0-jar-with-dependencies.jar: \
	com.microrisc.jlibiqrf.bridge.Boot
