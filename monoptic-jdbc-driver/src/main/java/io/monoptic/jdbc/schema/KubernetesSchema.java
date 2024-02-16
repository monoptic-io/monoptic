package io.monoptic.jdbc.schema;

import io.kubernetes.client.openapi.ApiException;

import io.monoptic.jdbc.KubernetesConnection;

import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class KubernetesSchema extends AbstractSchema {

  private final KubernetesConnection kubernetesConnection;
  private final Map<String, Table> tableMap = new HashMap<>();
  private final KubernetesDatabaseTable databaseTable;
  private final KubernetesEngineTable engineTable;
  private final KubernetesViewTable viewTable;

  public KubernetesSchema(KubernetesConnection kubernetesConnection) throws ApiException {
    this.kubernetesConnection = kubernetesConnection;
    this.databaseTable = new KubernetesDatabaseTable(kubernetesConnection);
    this.engineTable = new KubernetesEngineTable(kubernetesConnection);
    this.viewTable = new KubernetesViewTable(kubernetesConnection);
    tableMap.put("DATABASES", databaseTable);
    tableMap.put("ENGINES", engineTable);
    tableMap.put("VIEWS", viewTable);
  }

  public KubernetesDatabaseTable databases() {
    return databaseTable;
  }

  public KubernetesViewTable views() {
    return viewTable;
  }

  public KubernetesEngineTable engines() {
    return engineTable;
  }

  @Override
  public Map<String, Table> getTableMap() {
    return tableMap; 
  }
}
