package io.monoptic.jdbc.schema;

import io.monoptic.jdbc.MonopticJdbcConvention;
import io.monoptic.jdbc.Engine;

import org.apache.calcite.adapter.jdbc.JdbcConvention;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.Schemas;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlDialectFactory;
import org.apache.calcite.sql.SqlDialectFactoryImpl;

import java.util.List;
import javax.sql.DataSource;

public class MonopticJdbcSchema extends JdbcSchema {

  public MonopticJdbcSchema(String name, DataSource dataSource, List<Engine> engines, SqlDialect dialect,
      Expression expression) {
    super(dataSource, dialect, new MonopticJdbcConvention(dialect, expression, name, engines), null, null);
  }

  public MonopticJdbcSchema(String name, DataSource dataSource, List<Engine> engines,
      SchemaPlus parentSchema, SqlDialect dialect) {
    this(name, dataSource, engines, dialect, Schemas.subSchemaExpression(parentSchema, name,
      MonopticSchema.class));
  }

  public MonopticJdbcSchema(String name, DataSource dataSource, List<Engine> engines,
      SchemaPlus parentSchema, SqlDialectFactory dialectFactory) {
    this(name, dataSource, engines, parentSchema, createDialect(dialectFactory, dataSource));
  }

  public MonopticJdbcSchema(String name, DataSource dataSource, List<Engine> engines,
      SchemaPlus parentSchema) {
    this(name, dataSource, engines, parentSchema, SqlDialectFactoryImpl.INSTANCE);
  }
}
