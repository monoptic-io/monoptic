package io.monoptic.jdbc;

import org.apache.calcite.adapter.jdbc.JdbcConvention;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.schema.Schemas;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlDialectFactory;
import org.apache.calcite.sql.SqlDialectFactoryImpl;
import org.apache.calcite.sql.dialect.AnsiSqlDialect;

import javax.sql.DataSource;

public class Engine {

  private final String name;
  private final DataSource dataSource;
  private final Expression expression;
  private final SqlDialect dialect;
  private final JdbcConvention convention;
  private final JdbcSchema schema;

  public Engine(SchemaPlus parentSchema, String name, DataSource dataSource) {
    this.name = name;
    this.dataSource = dataSource;
    this.expression = Schemas.subSchemaExpression(parentSchema, name, JdbcSchema.class);
    this.dialect = AnsiSqlDialect.DEFAULT;//JdbcSchema.createDialect(SqlDialectFactoryImpl.INSTANCE, dataSource);
    this.convention = JdbcConvention.of(dialect, expression, name);
    this.schema = new JdbcSchema(dataSource, dialect, convention, null, null);
  }
  
  public String name() {
    return name;
  }

  public DataSource dataSource() {
    return dataSource;
  }

  public JdbcSchema schema() {
    return schema;
  }

  public JdbcConvention convention() {
    return convention;
  }

  public Expression expression() {
    return expression;
  }

  public SqlDialect dialect() {
    return dialect;
  }
} 
