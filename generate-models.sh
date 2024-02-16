#!/bin/sh

docker pull ghcr.io/kubernetes-client/java/crd-model-gen:v1.0.6

docker run \
  --rm \
  --mount type=bind,src=/var/run/docker.sock,dst=/var/run/docker.sock \
  --mount type=bind,src="$(pwd)",dst="$(pwd)" \
  -ti \
  --network host \
  ghcr.io/kubernetes-client/java/crd-model-gen:v1.0.6 \
  /generate.sh -o "$(pwd)/monoptic-kubernetes-models" -n "" -p "io.monoptic.kubernetes" \
  -u "$(pwd)/monoptic-kubernetes-models/src/main/resources/database.crd.yaml" \
  -u "$(pwd)/monoptic-kubernetes-models/src/main/resources/engine.crd.yaml" \
  -u "$(pwd)/monoptic-kubernetes-models/src/main/resources/view.crd.yaml" \
  && echo "done."
