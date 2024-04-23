#!make
include .env
export

COMPOSE = docker compose

compose-stop:
	$(COMPOSE) stop

compose-rm:
	$(COMPOSE) rm

compose-start:
	$(COMPOSE) up -d

sbt-start:
	sbt -jvm-debug 5005 ~reStart

compose-sbt:
	make compose-stop && make compose-start
	sbt -jvm-debug 5005 ~reStart
	make compose-stop