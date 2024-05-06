# github-metrics ![github actions](https://github.com/broilogabriel/github-metrics/actions/workflows/scala.yml/badge.svg)

Project for collecting and exposing metrics of github repositories

## Setup

The required versions for this project are available in the [.sdkmanrc](.sdkmanrc) file and can be installed using
[sdkman](https://sdkman.io) with the following command:
```shell
sdk env install
```

The database system selected for the project is postgresql and for managing migrations flyway was the tool selected.

The [docker-compose.yaml](docker-compose.yaml) file contains the configuration for starting these tools. It makes use
of an `.env` file that can be created based on [.env.dist](.env.dist). The `.env` file should contain the environment
variables required for the project to run.

### Code style

This project makes use of [scalafmt](https://scalameta.org/scalafmt/) and the customised settings are available in
[.scalafmt.conf](.scalafmt.conf).

## Running

Some helper commands are available in the [Makefile](Makefile) to allow to run the project in the local environment.

Below a list of the commands:
```shell
# stop the containers
make compose-stop
# delete the containers
make compose-rm
# start the containers
make compose-start
# starts sbt and runs the project in hot reload mode
make sbt-start
# starts the containers and sbt in hot reload mode
make compose-sbt
```

## API

Additional endpoints were created in order to allow to manage the repositories and force synchronization in addition
to the ones requested in the spec to list metrics of contributors and repositories.

### Track repository
```shell
curl --request POST \
  --url http://localhost:8080/api/github/monitor \
  --header 'Content-Type: application/json' \
  --data '{
	"id": 29986727,
	"name": "cats",
	"owner": {
		"id": 3360080,
		"login": "typelevel",
		"type": "user"
	}
}'
```

### Force synchronization
```shell
curl --request GET \
  --url http://localhost:8080/api/github
```

### Webhook
```shell
curl --request POST \
  --url http://localhost:8080/api/github \
  --header 'Content-Type: application/json' \
  --header 'X-GitHub-Delivery: <DELIVERY_UUID>' \
  --header 'X-GitHub-Event: <EVENT_TYPE>' \
  --data '{ ... }'
```

### Metrics
```shell
# contributors
curl --request GET \
  --url http://localhost:8080/api/contributors/10137/metrics

# projects
curl --request GET \
  --url http://localhost:8080/api/projects/29986727/metrics
```

## Current Issues (Potential Future Improvements)

 - No authentication/authorization in the project
 - No throttling for ensuring rate limits are not exceeded
 - It is possible to start multiple synchronizations at the same time, a semaphore like approach is needed to ensure no
concurrent syncs are happening
 - No integration/e2e tests, also limited code coverage from unit tests
 - Not packaging the application, `sbt-native-packager` could be used for creating a docker image for example