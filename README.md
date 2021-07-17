# Batch Logger

A simple web-hook service made using `java` and [micronaut](https://micronaut.io/). It accepts logs
at `/log` endpoint and then writes logs in batches in intervals to a post endpoint. You need to
configure `Batch Size`, `Interval Time`
and `Post endpoint` via environment variables.

# About the codebase design

- Using reactive programming with the help of RxJava's PublishSubject
- Incoming logs are added to publish subject in non-blocking manner so that requests do not get
  blocked
- Consuming of logs and sending them to target endpoint happens in sequential manner

## Steps to run

- run: `docker build . -t batch-logger`
- create an environment file as described in `sample.env` or pass env variables from command line
- run: `docker run -it --rm -p 8080:8080 --env-file [env-file-name] batch-logger`

## Endpoints

- `GET: /healthz` - Returns health of the server
- `POST: /log` - Send your logs here
