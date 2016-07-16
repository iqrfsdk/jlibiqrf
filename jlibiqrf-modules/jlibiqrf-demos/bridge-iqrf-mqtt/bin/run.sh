/usr/bin/java -Djava.library.path=natives/armv6hf \
	-Dlogback.configurationFile=config/logback.xml \
	-cp bridge-iqrf-mqtt-1.1.0-jar-with-dependencies.jar: \
	com.microrisc.jlibiqrf.demos.Main \
	config/config.xml
