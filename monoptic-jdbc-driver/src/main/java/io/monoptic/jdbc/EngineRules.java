package io.monoptic.jdbc;

import org.apache.calcite.adapter.enumerable.JavaRowFormat;
import org.apache.calcite.adapter.enumerable.PhysType;
import org.apache.calcite.adapter.enumerable.PhysTypeImpl;
import org.apache.calcite.adapter.jdbc.JdbcImplementor;
import org.apache.calcite.adapter.jdbc.JdbcRel;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.linq4j.tree.BlockBuilder;
import org.apache.calcite.linq4j.tree.ConstantExpression;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.linq4j.tree.ParameterExpression;
import org.apache.calcite.linq4j.tree.Primitive;
import org.apache.calcite.linq4j.tree.UnaryExpression;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCost;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTraitSet;
import org.apache.calcite.rel.InvalidRelException;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Join;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.convert.ConverterImpl;
import org.apache.calcite.rel.logical.LogicalJoin;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.metadata.RelMetadataQuery;
import org.apache.calcite.rel.rel2sql.RelToSqlConverter;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.dialect.AnsiSqlDialect;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.sql.util.SqlString;
import org.apache.calcite.util.BuiltInMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;
import java.sql.SQLException;
import java.sql.ResultSet;
import static java.util.Objects.requireNonNull;
import javax.sql.DataSource;


/** Calling convention using an engine to execute queries remotely. */
public class EngineRules {

  private final Engine engine;

  public EngineRules(Engine engine) {
    this.engine = engine;
  }

  public void register(RelOptPlanner planner) {
    planner.addRule(RemoteJoinRule.Config.INSTANCE
        .withConversion(Join.class, Convention.NONE, engine.convention(), "RemoteJoinRule")
        .withRuleFactory(RemoteJoinRule::new)
        .as(RemoteJoinRule.Config.class)
        .toRule(RemoteJoinRule.class));
    planner.addRule(RemoteTableScanRule.Config.INSTANCE
        .withConversion(TableScan.class, Convention.NONE, engine.convention(), "RemoteTableScanRule")
        .withRuleFactory(RemoteTableScanRule::new)
        .as(RemoteTableScanRule.Config.class)
        .toRule(RemoteTableScanRule.class));
  }

  private class RemoteTableScanRule extends ConverterRule {

    protected RemoteTableScanRule(Config config) {
      super(config);
    }

    @Override
    public RelNode convert(RelNode rel) {
      TableScan scan = (TableScan) rel;
      RelOptTable relOptTable = scan.getTable();
      RelTraitSet newTraitSet = rel.getTraitSet().replace(getOutTrait());
      return new RemoteTableScan(rel.getCluster(), newTraitSet, relOptTable);
    }
  }

  private class RemoteTableScan extends TableScan implements JdbcRel {

    public RemoteTableScan(RelOptCluster cluster, RelTraitSet traitSet, RelOptTable table) {
      super(cluster, traitSet, Collections.emptyList(), table);
    }

    @Override
    public JdbcImplementor.Result implement(JdbcImplementor implementor) {
      JdbcImplementor.Result res = new JdbcImplementor(engine.dialect(), new JavaTypeFactoryImpl()).implement(getInput(0));
      System.out.println(res.toString());
      return res;
    }
  }
 
  private class RemoteJoinRule extends ConverterRule {

    protected RemoteJoinRule(Config config) {
      super(config);
    }

    @Override
    public RelNode convert(RelNode rel) {
      RelTraitSet newTraitSet = rel.getTraitSet().replace(getOutTrait());
      Join join = (Join) rel;
      try {
        return new RemoteJoin(rel.getCluster(), newTraitSet, join.getLeft(), join.getRight(), join.getCondition(),
            join.getJoinType());
      } catch (InvalidRelException e) {
        throw new AssertionError(e);
      }
    }
  }

  private class RemoteJoin extends Join implements JdbcRel {

    public RemoteJoin(RelOptCluster cluster, RelTraitSet traitSet, RelNode left, RelNode right,
        RexNode condition, JoinRelType joinType) throws InvalidRelException {
      super(cluster, traitSet, Collections.emptyList(), left, right, condition, Collections.emptySet(),
          joinType);
    }

    @Override
    public Join copy(RelTraitSet traitSet, RexNode condition,
        RelNode left, RelNode right, JoinRelType joinType, boolean semiJoinDone) {
      try { 
        return new RemoteJoin(getCluster(), traitSet, left, right, condition, joinType);
      } catch (InvalidRelException e) {
        throw new AssertionError(e);
      }
    }

    @Override
    public RelOptCost computeSelfCost(RelOptPlanner planner, RelMetadataQuery mq) {
      return super.computeSelfCost(planner, mq).multiplyBy(0);  // TODO fix zero cost
    }

    @Override
    public JdbcImplementor.Result implement(JdbcImplementor implementor) {
      JdbcImplementor.Result res = new JdbcImplementor(engine.dialect(), new JavaTypeFactoryImpl()).implement(getInput(0));
      System.out.println(res.toString());
      return res;
    }
  }
} 
