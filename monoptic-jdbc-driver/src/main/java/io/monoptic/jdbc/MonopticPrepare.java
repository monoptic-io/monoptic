package io.monoptic.jdbc;

import org.apache.calcite.prepare.CalcitePrepareImpl;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParser;

class MonopticPrepare extends CalcitePrepareImpl {

  @Override
  protected SqlParser.Config parserConfig() {
    return SqlParser.config().withParserFactory(MonopticDdlExecutor.PARSER_FACTORY);
  }  

  @Override
  public void executeDdl(Context context, SqlNode node) {
    new MonopticDdlExecutor().executeDdl(context, node);
  }

}
