# Monoptic

Monoptic sees Kubernetes as one big database.

## Quickstart

```
$ ./gradlew build installDist
$ ./monoptic
monoptic> !intro
monoptic> !quit
```

## Getting Started

Monoptic requires a Kubernetes cluster and associated `kubectl` configs. For testing purposes, you may want to use `kind`, `docker-desktop`, `minikube`, or `microk8s`.

```
$ kind create cluster
$ kubectl config current-context
```

By default, monoptic will connect to Kubernetes using your `kubectl` configs.

```
$ ./monoptic
monoptic> !tables
monoptic> SELECT * FROM KUBERNETES.DATABASES;
monoptic> !quit
```

It's also possible to connect over HTTP via `monoptic-sql-gateway`.

```
$ helm repo add monoptic https://charts.monoptic.io
$ helm install monoptic-platform monoptic/monoptic-platform
$ kubectl port-forward svc/monoptic-sql-gateway:8765 &
$ ./monoptic --gateway http://localhost:8765
```

The SQL gateway can be useful for accessing resources within your cluster (e.g. databases) that aren't otherwise exposed.

## Adding Databases and Engines

Monoptic can leverage `databases` to store data and `engines` to process data. These can be installed via `helm` or `kubectl`.

```
$ helm install monoptic-flink-engine monoptic/monoptic-flink-engine
$ kubectl apply -f my-database.yaml
$ kubectl get databases
$ kubectl get engines
$ ./monoptic
monoptic> SELECT * FROM KUBERNETES.DATABASES;
monoptic> SELECT * FROM KUBERNETES.ENGINES;
```

In case you are missing the required CRDs, you can install them with the `!install` command within the `monoptic` CLI. Helm charts will typically install the CRDs for you.

## OLAP Queries

Monoptic supports OLAP queries on `databases`. This includes joins across different types of database, whether in Kubernetes or not.

```
monoptic> SELECT DISTINCT city."name" AS city, "population" FROM "mysql"."customers" customer, "postgres"."city" city, "mysql"."orders" "order", "mysql"."addresses" address WHERE "order"."purchaser" = customer."id" AND address."customer_id" = customer."id" AND address."state" LIKE city."district" LIMIT 5;
```

## Views

```
monoptic> CREATE VIEW "customer-cities" AS SELECT DISTINCT city."name" AS city, "population" FROM "mysql"."customers" customer, "postgres"."city" city, "mysql"."orders" "order", "mysql"."addresses" address WHERE "order"."purchaser" = customer."id" AND address."customer_id" = customer."id" AND address."state" LIKE city."district";
monoptic> SELECT * FROM "customer-cities" ORDER BY "population" DESC LIMIT 5;
```


