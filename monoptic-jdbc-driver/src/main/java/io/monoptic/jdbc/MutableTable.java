package io.monoptic.jdbc;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql2rel.InitializerExpressionFactory;

public class MutableTable extends AbstractTable {
  private final RelProtoDataType protoStoredRowType;
  private final RelProtoDataType protoRowType;
  private final InitializerExpressionFactory initializerExpressionFactory;

  MutableTable(String name, RelProtoDataType protoStoredRowType, RelProtoDataType protoRowType,
      InitializerExpressionFactory initializerExpressionFactory) {
    this.protoStoredRowType = protoStoredRowType;
    this.protoRowType = protoRowType;
    this.initializerExpressionFactory = initializerExpressionFactory;
  }

  @Override
  public RelDataType getRowType(RelDataTypeFactory typeFactory) {
    return protoRowType.apply(typeFactory);
  }

  void truncate() {
    throw new UnsupportedOperationException("Can't truncate this table yet.");
  }
}
