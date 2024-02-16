package io.monoptic.jdbc;

import io.monoptic.kubernetes.models.*;

import io.kubernetes.client.apimachinery.GroupVersion;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.generic.KubernetesApiResponse;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesListObject;
import io.kubernetes.client.util.generic.dynamic.Dynamics;
import io.kubernetes.client.openapi.models.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.io.InputStream;
import java.io.IOException;

public class KubernetesResource<T extends KubernetesObject, U extends KubernetesListObject> {

  public static final KubernetesResource<V1Secret, V1SecretList> SECRETS = new KubernetesResource<>(
      "Secret", "", "v1", "secrets", V1Secret.class, V1SecretList.class);
  public static final KubernetesResource<V1Pod, V1PodList> PODS = new KubernetesResource<>(
      "Pod", "", "v1", "pods", V1Pod.class, V1PodList.class);
  public static final KubernetesResource<V1Deployment, V1DeploymentList> DEPLOYMENTS = new KubernetesResource<>(
      "Deployment", "app", "v1", "deployments", V1Deployment.class, V1DeploymentList.class);
  public static final KubernetesResource<V1alpha1Database, V1alpha1DatabaseList> DATABASES = new KubernetesResource<>(
      "Database", "monoptic.io", "v1alpha1", "databases", V1alpha1Database.class, V1alpha1DatabaseList.class);
  public static final KubernetesResource<V1alpha1Engine, V1alpha1EngineList> ENGINES = new KubernetesResource<>(
      "Engine", "monoptic.io", "v1alpha1", "engines", V1alpha1Engine.class, V1alpha1EngineList.class);
  public static final KubernetesResource<V1alpha1View, V1alpha1ViewList> VIEWS = new KubernetesResource<>(
      "View", "monoptic.io", "v1alpha1", "views", V1alpha1View.class, V1alpha1ViewList.class);

  private final String kind;
  private final String group;
  private final String version;
  private final String plural;
  private final String apiVersion;
  private final Class<T> t;
  private final Class<U> u;

  public KubernetesResource(String kind, String group, String version, String plural, Class<T> t, Class<U> u) {
    this.kind = kind;
    this.group = group;
    this.version = version;
    this.plural = plural;
    this.apiVersion = String.join("/", group, version);
    this.t = t;
    this.u = u;
  }

  public String kind() {
    return kind;
  }

  public String group() {
    return group;
  }

  public String version() {
    return version;
  }

  public String apiVersion() {
    return apiVersion;
  }

  public String plural() {
    return plural;
  }

  public Class elementType() {
    return t;
  }

  public Class listType() {
    return u;
  }

  @SuppressWarnings("unchecked")
  public List<T> list(KubernetesConnection conn) throws ApiException {
    KubernetesApiResponse<U> resp = conn.generic(t, u, group, version, plural).list(conn.namespace());
    if (resp.getHttpStatusCode() == 404) {
      return Collections.emptyList();
    }
    resp.throwsApiException();
    return (List<T>) resp.getObject().getItems();
  }

  @SuppressWarnings("unchecked")
  public T get(KubernetesConnection conn, String name) throws ApiException {
    KubernetesApiResponse<T> resp = conn.generic(t, u, group, version, plural).get(conn.namespace(), name);
    resp.throwsApiException();
    return (T) resp.getObject();
  }

  public void create(KubernetesConnection conn, T obj) throws ApiException {
    obj.getMetadata().namespace(conn.namespace());
    KubernetesApiResponse<T> resp = conn.generic(t, u, group, version, plural).create(obj);
    resp.throwsApiException();
  }
}
