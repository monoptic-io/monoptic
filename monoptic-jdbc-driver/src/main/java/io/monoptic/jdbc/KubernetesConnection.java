package io.monoptic.jdbc;

import io.kubernetes.client.apimachinery.GroupVersion;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.Namespaces;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesApi;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Secret;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;

public class KubernetesConnection {

  private final String namespace;
  private final ApiClient apiClient;

  private KubernetesConnection(String namespace, ApiClient apiClient) {
    this.namespace = namespace;
    this.apiClient = apiClient;
  }

  public ApiClient apiClient() {
    return apiClient;
  }

  public String namespace() {
    return namespace;
  }

  public KubernetesConnection(String namespace) throws IOException {
    this(namespace, Config.defaultClient());
  }

  public KubernetesConnection() throws IOException {
    this(guessNamespace());
  }

  public DynamicKubernetesApi dynamic(String apiVersion, String plural) {
    GroupVersion gv = GroupVersion.parse(apiVersion);
    return dynamic(gv.getGroup(), gv.getVersion(), plural);
  }

  public DynamicKubernetesApi dynamic(String group, String version, String plural) {
    return new DynamicKubernetesApi(group, version, plural, apiClient);
  }

  public DynamicKubernetesApi dynamic(KubernetesResource resource) {
    return dynamic(resource.group(), resource.version(), resource.plural());
  }

  public <T extends KubernetesObject, U extends KubernetesListObject> GenericKubernetesApi<T, U> generic(Class<T> t, Class<U> u,
      String group, String version, String plural) {
    return new GenericKubernetesApi<T, U>(t, u, group, version, plural, apiClient);
  }

  public <T extends KubernetesObject, U extends KubernetesListObject> GenericKubernetesApi<T, U> generic(Class<T> t, Class<U> u,
      KubernetesResource resource) {
    return generic(t, u, resource.group(), resource.version(), resource.plural());
  }

  public Map<String, String> secret(String secretName) throws ApiException {
    if (secretName == null || secretName.length() == 0) {
      return Collections.emptyMap();
    }
    Map<String, String> res = new HashMap<>();
    V1Secret secret = KubernetesResource.SECRETS.get(this, secretName);
    if (secret.getStringData() != null) {
      secret.getStringData().forEach((k, v) -> res.put(k, v));
    }
    if (secret.getData() != null) {
      secret.getData().forEach((k, v) -> res.put(k, new String(v)));
    }
    return res;
  }

  private static String guessNamespace() throws IOException {
    try {
      String podNamespace = Namespaces.getPodNamespace();
      if (podNamespace != null) {
        return podNamespace;
      }
    } catch (IOException e) {
      // swallow
    }
    try (Reader r = Files.newBufferedReader(Paths.get(System.getProperty("user.home")).resolve(".kube/config"))) {
      String contextNamespace = KubeConfig.loadKubeConfig(r).getNamespace();
      if (contextNamespace != null) {
        return contextNamespace;
      } else {
        return "default";
      }
    }
  }
}
