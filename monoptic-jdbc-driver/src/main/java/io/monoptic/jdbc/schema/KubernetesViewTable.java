package io.monoptic.jdbc.schema;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

import io.monoptic.jdbc.KubernetesConnection;
import io.monoptic.jdbc.KubernetesTable;
import io.monoptic.jdbc.KubernetesResource;
import io.monoptic.jdbc.MaterializedView;

import io.kubernetes.client.openapi.ApiException;
import io.monoptic.kubernetes.models.V1alpha1View;
import io.monoptic.kubernetes.models.V1alpha1ViewList;
import io.monoptic.kubernetes.models.V1alpha1ViewSpec;

import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.schema.TableMacro;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.ViewTable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KubernetesViewTable extends KubernetesTable<V1alpha1View, V1alpha1ViewList,
      KubernetesViewTable.Row> {

  public static class Row {
    public String NAME;
    public String SQL;
    public boolean MATERIALIZE;

    public Row(String name, String sql, boolean materialize) {
      this.NAME = name;
      this.SQL = sql;
      this.MATERIALIZE = materialize;
    }
  };
  
  public KubernetesViewTable(KubernetesConnection kubernetesConnection) throws ApiException {
    super(kubernetesConnection, KubernetesResource.VIEWS, Row.class);
  }

  public Map<String, Table> tables(SchemaPlus parent) throws ApiException {
    return rows().stream()
      .collect(Collectors.toMap(x -> x.NAME, x -> makeView(parent, x)));
  }

  private Table makeView(SchemaPlus parent, Row row) {
    TranslatableTable view = ViewTable.viewMacro(parent, row.SQL, Collections.singletonList(namespace()),
      Collections.singletonList(row.NAME), false)
      .apply(Collections.emptyList());
    if (row.MATERIALIZE) {
      return new MaterializedView(view);
    } else {
      return view;
    }
  }

 public void create(String name, String sql, boolean materialize) {
    getModifiableCollection().add(new Row(name, sql, materialize));
  }
 
  @Override
  public Row toRow(V1alpha1View obj) {
    return new Row(obj.getMetadata().getName(), obj.getSpec().getSql(), orFalse(obj.getSpec().getMaterialize()));
  }

  @Override
  public V1alpha1View fromRow(Row row) {
    return new V1alpha1View().kind(resource().kind()).apiVersion(resource().apiVersion())
        .metadata(new V1ObjectMeta().name(row.NAME))
        .spec(new V1alpha1ViewSpec().sql(row.SQL).materialize(row.MATERIALIZE));
  }
}
