package io.monoptic.jdbc;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;

import java.util.Collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class KubernetesCollection<T extends KubernetesObject, U extends KubernetesListObject, V>
    extends ArrayList<V> {

  private final KubernetesConnection connection;
  private final KubernetesResource<T, U> resource;
  private final KubernetesObjectMapper<T, V> mapper;

  public KubernetesCollection(KubernetesConnection connection, KubernetesResource<T, U> resource,
      KubernetesObjectMapper<T, V> mapper) throws ApiException {
    this.connection = connection;
    this.resource = resource;
    this.mapper = mapper;
    resource.list(connection).forEach(x -> super.add(mapper.toRow(x)));
  }

  @Override
  public boolean add(V v) {
    try {
      resource.create(connection, mapper.fromRow(v));
      return super.add(v);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
