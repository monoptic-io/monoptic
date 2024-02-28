
build:
	./gradlew build installDist

docker-build: build
	docker build . -f Dockerfile -t ghcr.io/monoptic-io/monoptic:testing
	docker build . -f monoptic-sql-gateway.Dockerfile -t ghcr.io/monoptic-io/monoptic-sql-gateway:testing
	./gradlew :monoptic-flink-runtime:shadow
	docker build . -f monoptic-flink-runtime.Dockerfile -t ghcr.io/monoptic-io/monoptic-flink-runtime:testing

docker-push: docker-build
	docker push ghcr.io/monoptic-io/monoptic:testing
	docker push ghcr.io/monoptic-io/monoptic-sql-gateway:testing
	docker push ghcr.io/monoptic-io/monoptic-flink-runtime:testing

.PHONY: build docker-build docker-push integration-tests

