package io.monoptic.jdbc;

import io.monoptic.jdbc.schema.KubernetesSchema;
import io.monoptic.jdbc.schema.KubernetesDatabaseTable;
import io.monoptic.jdbc.schema.KubernetesViewTable;
import io.monoptic.jdbc.schema.MonopticSchema;

import org.apache.calcite.avatica.DriverVersion;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonopticDriver extends org.apache.calcite.jdbc.Driver {

  public MonopticDriver() {
    super(() -> new MonopticPrepare());
  }

  static {
    new MonopticDriver().register();
  }

  @Override
  protected String getConnectStringPrefix() {
    return "jdbc:monoptic:";
  }

  @Override
  protected DriverVersion createDriverVersion() {
    return DriverVersion.load(this.getClass(), "monoptic.properties", "monoptic", "0", "Monoptic", "0");
  }

  @Override
  public Connection connect(String url, Properties props) throws SQLException {
    try {
      Connection connection = super.connect(url, props);
      if (connection == null) {
        throw new IOException("Could not connect to " + url);
      }
      KubernetesConnection kubernetesConnection = new KubernetesConnection();
      CalciteConnection calciteConnection = (CalciteConnection) connection;
      SchemaPlus rootSchema = calciteConnection.getRootSchema();
      KubernetesSchema kubernetes = new KubernetesSchema(kubernetesConnection);
      rootSchema.add("KUBERNETES", kubernetes);
      List<Engine> engines = kubernetes.engines().engines(rootSchema);
      kubernetes.databases().schemas(rootSchema, engines).forEach((k, v) -> rootSchema.add(k, v));
      engines.forEach(x -> rootSchema.add(x.name(), x.schema()));
      rootSchema.add(kubernetesConnection.namespace(), new MonopticSchema(kubernetes.views().tables(rootSchema)));
      calciteConnection.setSchema(kubernetesConnection.namespace());
      return connection;
    } catch (Exception e) {
      throw new SQLException(e);
    }
  }

  private static Map<String, String> parseUrl(String url) {
    String[] fields = url.split("[:&]");
    Map<String, String> res = new HashMap<>();
    for (int i = 0; i < fields.length; i++) {
      String[] pair = fields[i].split("\\s*=\\s*");
      if (pair.length == 2) {
        res.put(pair[0], pair[1]);
      }
    }
    return res;
  }
}
