package io.monoptic.jdbc;

import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelOptCluster;

public class MaterializedView extends AbstractTable implements TranslatableTable {

  private final TranslatableTable view;

  public MaterializedView(TranslatableTable view) {
    this.view = view;
  }

  @Override
  public RelDataType getRowType(RelDataTypeFactory typeFactory) {
    return view.getRowType(typeFactory);
  }

  @Override
  public Schema.TableType getJdbcTableType() {
    return Schema.TableType.MATERIALIZED_VIEW;
  }

  @Override
  public RelNode toRel(RelOptTable.ToRelContext context, RelOptTable relOptTable) {
    return view.toRel(context, relOptTable);
  }
}
