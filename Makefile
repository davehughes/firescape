.PHONY: all build-client run-client build-server run-server

all: build-client build-server

PWD := $(shell pwd)
CLIENT_DIR=firescape-204b
# CLIENT_DIR=firescape-client

build-client:
	cd $(CLIENT_DIR) && mvn compile package

run-client:
	cd $(CLIENT_DIR) && java -jar target/firescape-jar-with-dependencies.jar --server localhost --port 27337

test-client:
	cd $(CLIENT_DIR) && mvn test

view-coverage:
	open file://$(PWD)/firescape-server/target/site/cobertura/index.html

build-server:
	cd firescape-server && mvn compile package

run-server:
	cd firescape-server && java -jar target/firescape-jar-with-dependencies.jar
