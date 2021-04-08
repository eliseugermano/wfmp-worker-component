# wfmp-worker

### Current Release ###
* Workflow orchestration engine (Petri Net model)
* Queue-manager

### Requirements for execution ###

* [Docker](https://docs.docker.com/) | [Docker-compose](https://docs.docker.com/compose/) | [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) | [Maven](https://maven.apache.org/)

### Setting Environment Variables ###
* Database
	- spring.datasource.username
	- spring.datasource.password
* RabbitMQ
	- spring.rabbitmq.host
	- spring.rabbitmq.port
	- spring.rabbitmq.password
	- spring.rabbitmq.username
	- spring.rabbitmq.virtual-host
	- spring.rabbitmq.default-exchange
* Server Host
	- server.host.manager
	- server.host.worker

### API documentation ###
* {basePath}/swagger-ui.html