# jlibiqrf

Library JLibIQRF provides functionality and especially united interface for communication with IQRF devices via several different communication protocols. The interface of library is based on simplicity and it's using for communication basic data types, so the main logic system can be created by user. Library isn't realted with DPA protocol, so library can be used for communication with modules without DPA.
The supported communication protocols are CDC, SPI, UDP and Serial.


The itself library is placed in modules project as jlibiqrf.
The basic example, how to use this library can be found in modules project as jlibiqrf-examples and there is shown how to send data to IQRF network and how to receive response.
The last part of modules project is next modules project with demos. Actually there is only one demo and this demo is providing bridge between IQRF network and MQTT broker.