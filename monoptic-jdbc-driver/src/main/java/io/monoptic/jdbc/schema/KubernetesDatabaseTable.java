package io.monoptic.jdbc.schema;

import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject;
import io.kubernetes.client.openapi.ApiException;

import io.monoptic.jdbc.Engine;
import io.monoptic.jdbc.KubernetesConnection;
import io.monoptic.jdbc.KubernetesTable;
import io.monoptic.jdbc.KubernetesResource;

import io.monoptic.kubernetes.models.V1alpha1Database;
import io.monoptic.kubernetes.models.V1alpha1DatabaseList;

import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.adapter.jdbc.JdbcRel;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Schemas;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlDialectFactoryImpl;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.sql.DataSource;

public class KubernetesDatabaseTable extends KubernetesTable<V1alpha1Database, V1alpha1DatabaseList,
    KubernetesDatabaseTable.Row> {

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

  public KubernetesDatabaseTable(KubernetesConnection kubernetesConnection) throws ApiException {
    super(kubernetesConnection, KubernetesResource.DATABASES, Row.class);
  }

  public Map<String, Schema> schemas(SchemaPlus parentSchema, List<Engine> engines) throws ApiException {
    return rows().stream().collect(Collectors.toMap(x -> x.NAME, x -> makeSchema(parentSchema, engines, x)));
  }

  private Schema makeSchema(SchemaPlus parentSchema, List<Engine> engines, Row row) {
    try {
      Map<String, String> secret = connection().secret(row.SECRET);
      DataSource dataSource = JdbcSchema.dataSource(row.URL, null, secret.get("username"),
          secret.get("password")); 
      return new MonopticJdbcSchema(row.NAME, dataSource, engines, parentSchema);
    } catch (Exception e) {
      throw new RuntimeException("Unable to connect to database '" + row.NAME + "'.", e);
      //return new ExceptionalSchema(e);
    }
  }

  @Override
  public Row toRow(V1alpha1Database database) {
    return new Row(database.getMetadata().getName(), database.getSpec().getUrl(),
      database.getSpec().getSecret());
  }
}
