#!make
include .env
export

COMPOSE = docker compose

compose-stop:
	$(COMPOSE) stop

compose-rm:
	$(COMPOSE) rm

compose-start:
	$(COMPOSE) up

sbt-start:
	sbt -jvm-debug 5005 ~reStart

compose-sbt:
	make compose-stop && $(COMPOSE) up -d
	sbt -jvm-debug 5005 ~reStart
	make compose-stop