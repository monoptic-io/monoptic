package io.monoptic.jdbc.schema;

import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

import java.util.Map;

public class MonopticSchema extends AbstractSchema {

  private final Map<String, Table> tableMap;

  public MonopticSchema(Map<String, Table> tableMap) {
    this.tableMap = tableMap;
  }

  @Override
  public Map<String, Table> getTableMap() {
    return tableMap;
  }
}
