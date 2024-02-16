package io.monoptic.jdbc.schema;

import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject;
import io.kubernetes.client.openapi.ApiException;

import io.monoptic.jdbc.Engine;
import io.monoptic.jdbc.KubernetesConnection;
import io.monoptic.jdbc.KubernetesTable;
import io.monoptic.jdbc.KubernetesResource;

import io.monoptic.kubernetes.models.V1alpha1Engine;
import io.monoptic.kubernetes.models.V1alpha1EngineList;

import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.sql.DataSource;

public class KubernetesEngineTable extends KubernetesTable<V1alpha1Engine, V1alpha1EngineList,
    KubernetesEngineTable.Row> {

  public static class Row {
    public String NAME;
    public String URL;
    public String SECRET;

    public Row(String name, String url, String secret) {
      this.NAME = name;
      this.URL = url;
      this.SECRET = secret;
    }
  }

  public KubernetesEngineTable(KubernetesConnection kubernetesConnection) throws ApiException {
    super(kubernetesConnection, KubernetesResource.ENGINES, Row.class);
  }

  public List<Engine> engines(SchemaPlus parentSchema) throws ApiException {
    return rows().stream().map(x -> makeEngine(parentSchema, x)).collect(Collectors.toList());
  }

  private Engine makeEngine(SchemaPlus parentSchema, Row row) {
    try {
      Map<String, String> secret = connection().secret(row.SECRET);
      return new Engine(parentSchema, row.NAME, JdbcSchema.dataSource(row.URL, null, secret.get("username"),
          secret.get("password")));
    } catch (Exception e) {
      throw new RuntimeException("Unable to connect to engine '" + row.NAME + "'.", e);
      //return new ExceptionalSchema(e);
    }
  }


/*
  public Map<String, Engine> engines() {
    return rows().stream().collect(Collectors.toMap(x -> x.NAME, x -> makeEngine(x)));
  }

  private Engine makeEngine(Row row) {
    try {
      return new JdbcEngine(row.NAME, row.URL, connection().secret(row.SECRET));
    } catch (Exception e) {
      throw new RuntimeException("Failed to connect to engine '" + row.NAME + "'.", e);
    }
  }

  public Engine engine(String name) {
    if (name == null) {
      return null;
    }
    Engine res = engines().get(name);
    if (res == null) {
      throw new IllegalArgumentException("No engine '" + name + "'");
    } else {
      return res;
    }
  }
*/

  @Override
  public Row toRow(V1alpha1Engine engine) {
    return new Row(engine.getMetadata().getName(), engine.getSpec().getUrl(),
        engine.getSpec().getSecret());
  }
}
