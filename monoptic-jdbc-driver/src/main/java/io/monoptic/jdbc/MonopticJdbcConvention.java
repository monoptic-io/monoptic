package io.monoptic.jdbc;

import org.apache.calcite.adapter.jdbc.JdbcConvention;
import org.apache.calcite.adapter.jdbc.JdbcToEnumerableConverter;
import org.apache.calcite.adapter.jdbc.JdbcImplementor;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.SingleRel;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.metadata.RelColumnMapping;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.SqlDialect;

import java.util.List;
import java.util.Set;
import java.lang.reflect.Type;
import javax.sql.DataSource;

/** JdbcConvention which can leverage Engines. */
public class MonopticJdbcConvention extends JdbcConvention {

  private final List<Engine> engines;

  public MonopticJdbcConvention(SqlDialect dialect, Expression expression, String name,
      List<Engine> engines) {
    super(dialect, expression, name);
    this.engines = engines;
  }

  @Override
  public void register(RelOptPlanner planner) {
    super.register(planner);
    engines.forEach(x -> new EngineRules(x).register(planner));
  }
}
