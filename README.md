# Batch Logger

A simple web-server made using `java` and `micronaut`. The server writes logs in batchs in intervals
to a post endpoint. You need to configure `Batch Size`, `Interval Time` and `Post endpoint` via
environment variables.

## Steps to run

- run: `docker build . -t batch-logger`
- create an environment file as described in `sample.env` or pass env variables from command line
- run: `docker run -it --rm -p 8080:800 --env-file .env batch-logger`

## Endpoints

- `GET: /healthz` - Returns health of the server
- `POST: /log` - Send your logs here
