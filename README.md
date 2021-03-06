# carbon-proxy

A proxy/relay for the Carbon daemon.

Use this to relay metrics to other systems like Kafka.  
This server includes a full suite of JMX monitoring, including messaging channels and endpoints.   
JMX GUI provided by Hawtio + Jolokia.  

Hawtio -

![Hawtio JMX Monitoring](./examples/images/hawtio.PNG)  
  
  
Settings -  

```
# Web Management GUI
server.port=8800

# The local port and ip for the proxy to listen on
carbonProxy.server-port=2003
carbonProxy.server-address=0.0.0.0

# Where we relay the messages to
carbonProxy.carbon-port=2003
carbonProxy.carbon-host=localhost

# Channel polling frequency in milliseconds
carbonProxy.polling-frequency=500

# Kafka Configuration
kafka.producer.enabled=true
kafka.producer.group-id=carbon-proxy
kafka.producer.topic=metrics

# kafka producer filters 
# attribute = name/metric
kafka.producer.filters.myFilter.attribute=name
# filter = substring/regex
kafka.producer.filters.myFilter.filter=substring
# value = <regex/substring pattern>
kafka.producer.filters.myFilter.value=test

#kafka.producer.filters.anotherOne.attribute=name
#kafka.producer.filters.anotherOne.filter=regex
#kafka.producer.filters.anotherOne.value=test

kafka.consumer.enabled=true
kafka.consumer.group-id=carbon-proxy
kafka.consumer.topic=metrics
kafka.bootstrap-servers=localhost:9092

# Logging levels
logging.level.root=INFO
logging.level.org.springframework.integration=INFO
logging.level.net.savantly=DEBUG
logging.level.kafka=WARN
logging.level.org.apache.zookeeper=WARN

# JMX
endpoints.jmx.domain=net.savantly.metrics.carbonProxy
endpoints.jmx.unique-names=false

# Hawtio
hawtio.authentication-enabled=false

# Security
#security.user.name=admin
#security.user.password=secret

# App management
management.security.enabled=false
# If you want the management interface on a different port #management.port=8081
# management.security.roles=SUPERUSER
# From localhost only #management.address=127.0.0.1
```