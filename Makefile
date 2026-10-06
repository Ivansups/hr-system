.PHONY: build up test down logs db-shell

build:
	docker compose build

up: build
	docker compose up -d

test:
	docker build --target test -t hr-system-test .
	docker run --rm \
		-v /var/run/docker.sock:/var/run/docker.sock \
		-e TESTCONTAINERS_RYUK_DISABLED=true \
		-e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal \
		hr-system-test mvn test -Dapi.version=1.41

down:
	docker compose down -v

logs:
	docker compose logs -f app

db-shell:
	docker compose exec db psql -U hrsystem -d hrsystem
