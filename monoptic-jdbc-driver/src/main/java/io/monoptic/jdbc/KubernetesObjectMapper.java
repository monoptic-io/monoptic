package io.monoptic.jdbc;

import io.kubernetes.client.common.KubernetesObject;

public interface KubernetesObjectMapper<T extends KubernetesObject, U> {

  U toRow(T t);

  T fromRow(U u);
}
