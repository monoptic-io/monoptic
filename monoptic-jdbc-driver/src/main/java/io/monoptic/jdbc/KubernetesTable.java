package io.monoptic.jdbc;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.common.KubernetesListObject;

import org.apache.calcite.adapter.enumerable.EnumerableTableScan;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.linq4j.AbstractQueryable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.Queryable;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.TableModify;
import org.apache.calcite.rel.logical.LogicalTableModify;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.schema.ModifiableTable;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Schemas;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.prepare.Prepare;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class KubernetesTable<T extends KubernetesObject, U extends KubernetesListObject, V>  extends
    AbstractTable implements TranslatableTable, ModifiableTable, KubernetesObjectMapper<T, V> {

  private final KubernetesConnection connection;
  private final KubernetesResource<T, U> resource;
  private final KubernetesCollection<T, U, V> rows;
  private final Class elementType;
  private final RelDataType javaType;

  public KubernetesTable(KubernetesConnection connection, KubernetesResource<T, U> resource, Class elementType,
      JavaTypeFactory javaTypeFactory) throws ApiException {
    this.connection = connection;
    this.resource = resource;
    this.elementType = elementType;
    this.rows = new KubernetesCollection<T, U, V>(connection, resource, this);
    this.javaType = javaTypeFactory.createType(elementType);
  }

  public KubernetesTable(KubernetesConnection connection, KubernetesResource<T, U> resource, Class elementType)
      throws ApiException {
    this(connection, resource, elementType, new JavaTypeFactoryImpl());
  }

  public KubernetesResource resource() {
    return resource;
  }

  public KubernetesConnection connection() {
    return connection;
  }

  public Collection<V> rows() {
    return rows;
  }

  public String namespace() {
    return connection.namespace();
  }

  public List<String> names() {
    return rows.stream().map(x -> fromRow(x)).map(x -> x.getMetadata().getName())
        .collect(Collectors.toList());
  }

  public Optional<T> find(Predicate<? super T> predicate) {
    return rows.stream().map(x -> fromRow(x)).filter(predicate).findFirst();
  }

  public Optional<T> findByName(String name) {
    return find(x -> x.getMetadata().getName().equals(name));
  }

  @Override
  public RelDataType getRowType(RelDataTypeFactory typeFactory) {
    return typeFactory.copyType(javaType);
  }

  @Override
  public RelNode toRel(RelOptTable.ToRelContext context, RelOptTable relOptTable) {
    RelOptCluster cluster = context.getCluster();
    return EnumerableTableScan.create(context.getCluster(), relOptTable);
  }

  @Override
  public Collection<V> getModifiableCollection() {
    return rows;
  }

  @Override
  public Expression getExpression(SchemaPlus parentSchema, String name, Class clazz) {
    return Schemas.tableExpression(parentSchema, getElementType(), name, clazz);
  }

  @Override
  public Class getElementType() {
    return elementType;
  }

  @Override
  public T fromRow(V v) {
    throw new UnsupportedOperationException("This object is not writable.");
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T2> Queryable<T2> asQueryable(QueryProvider provider, SchemaPlus schema, String tableName) {
    return (Queryable<T2>) Linq4j.asEnumerable(rows).asQueryable();
  }

  @Override
  public TableModify toModificationRel(RelOptCluster cluster, RelOptTable table, Prepare.CatalogReader schema, RelNode input,
      TableModify.Operation operation, List<String> updateColumnList, List<RexNode> sourceExpressionList, boolean flattened) {
    RelTraitSet traitSet = cluster.traitSetOf(Convention.NONE);
    return new LogicalTableModify(cluster, traitSet, table, schema, input,
        operation, updateColumnList, sourceExpressionList, flattened);
  }

  @Override
  public Schema.TableType getJdbcTableType() {
    return Schema.TableType.SYSTEM_TABLE;
  }

  protected static boolean orFalse(Boolean bool) {
    if (bool != null) {
      return bool.booleanValue();
    } else {
      return false;
    }
  }
}
