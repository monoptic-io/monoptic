package io.monoptic.flink;

import org.apache.flink.connector.jdbc.catalog.AbstractJdbcCatalog;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.factories.CatalogFactory;
import org.apache.flink.table.catalog.CatalogBaseTable;
import org.apache.flink.table.catalog.CatalogTable;
import org.apache.flink.table.catalog.ObjectPath;
import org.apache.flink.table.catalog.UniqueConstraint;
import org.apache.flink.table.catalog.exceptions.CatalogException;
import org.apache.flink.table.catalog.exceptions.TableNotExistException;
import org.apache.flink.table.types.DataType;

import static org.apache.flink.connector.jdbc.table.JdbcConnectorOptions.PASSWORD;
import static org.apache.flink.connector.jdbc.table.JdbcConnectorOptions.TABLE_NAME;
import static org.apache.flink.connector.jdbc.table.JdbcConnectorOptions.URL;
import static org.apache.flink.connector.jdbc.table.JdbcConnectorOptions.USERNAME;
import static org.apache.flink.connector.jdbc.table.JdbcDynamicTableFactory.IDENTIFIER;
import static org.apache.flink.table.factories.FactoryUtil.CONNECTOR;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

public class MonopticCatalog extends AbstractJdbcCatalog {

  public MonopticCatalog(ClassLoader classLoader, String catalogName, String defaultDatabase, String username,
      String password, String baseUrl) {
    super(classLoader, catalogName, defaultDatabase, username, password, baseUrl);
  }

  @Override
  public boolean tableExists(ObjectPath objectPath) {
    return !extractColumnValuesBySQL(defaultUrl, "SELECT \"tableName\" FROM \"metadata\".TABLES"
        + " WHERE \"tableSchem\" = ? AND \"tableName\" = ?", 1, x -> true, objectPath.getDatabaseName(),
        objectPath.getObjectName()).isEmpty();
 
  }

  @Override
  public List<String> listTables(String databaseName) {
    return extractColumnValuesBySQL(defaultUrl, "SELECT \"tableName\" FROM \"metadata\".TABLES"
        + " WHERE \"tableSchem\" = ?", 1, x -> true, databaseName);
  }

  @Override
  public List<String> listDatabases() {
    return extractColumnValuesBySQL(defaultUrl, "SELECT DISTINCT \"tableSchem\" FROM \"metadata\".TABLES"
        + " WHERE \"tableSchem\" <> 'metadata'", 1, x -> true);
  }

  // This is copy-pasted from the base class in Apache Flink in order to fix a bug.
  // TODO in upstream, fix SELECT prepared statement
  @Override
  public CatalogBaseTable getTable(ObjectPath tablePath) throws TableNotExistException, CatalogException {
    if (!tableExists(tablePath)) {
      throw new TableNotExistException(getName(), tablePath);
    }

    String databaseName = tablePath.getDatabaseName();
    String dbUrl = baseUrl + databaseName;

    try (Connection conn = DriverManager.getConnection(dbUrl, username, pwd)) {
      DatabaseMetaData metaData = conn.getMetaData();
      Optional<UniqueConstraint> primaryKey = getPrimaryKey(metaData, databaseName, getSchemaName(tablePath),
          getTableName(tablePath));

      PreparedStatement ps = conn.prepareStatement(String.format("SELECT * FROM %s", getSchemaTableName(tablePath)));
      ResultSetMetaData resultSetMetaData = ps.getMetaData();
      String[] columnNames = new String[resultSetMetaData.getColumnCount()];
      DataType[] types = new DataType[resultSetMetaData.getColumnCount()];

      for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
        columnNames[i - 1] = resultSetMetaData.getColumnName(i);
        types[i - 1] = fromJDBCType(tablePath, resultSetMetaData, i);
        if (resultSetMetaData.isNullable(i) == ResultSetMetaData.columnNoNulls) {
          types[i - 1] = types[i - 1].notNull();
        }
      }

      Schema.Builder schemaBuilder = Schema.newBuilder().fromFields(columnNames, types);
      primaryKey.ifPresent(pk -> schemaBuilder.primaryKeyNamed(pk.getName(), pk.getColumns()));
      Schema tableSchema = schemaBuilder.build();

      Map<String, String> props = new HashMap<>();
      props.put(CONNECTOR.key(), IDENTIFIER);
      props.put(URL.key(), dbUrl);
      props.put(USERNAME.key(), username);
      props.put(PASSWORD.key(), pwd);
      props.put(TABLE_NAME.key(), getSchemaTableName(tablePath));
      return CatalogTable.of(tableSchema, null, Collections.emptyList(), props);
    } catch (Exception e) {
      throw new CatalogException(String.format("Failed getting table %s", tablePath.getFullName()), e);
    }
  }

  @Override
  protected DataType fromJDBCType(ObjectPath tablePath, ResultSetMetaData metadata, int colIndex) throws SQLException {
    int typeCode = metadata.getColumnType(colIndex);
//    String columnName = metadata.getColumnName(colIndex);
    int precision = metadata.getPrecision(colIndex);
    int scale = metadata.getScale(colIndex);

    switch (typeCode) {
    case Types.BINARY: 
      return DataTypes.BINARY(precision);
    case Types.BOOLEAN:
      return DataTypes.BOOLEAN();
    case Types.VARCHAR:
    case Types.LONGNVARCHAR:
    case Types.LONGVARCHAR:
    case Types.NCHAR:
      if (precision > 0) {
        return DataTypes.VARCHAR(precision);
      } else {
        return DataTypes.STRING();
      }
    case Types.BIGINT:
      return DataTypes.BIGINT();
    case Types.DECIMAL:
      return DataTypes.DECIMAL(precision, scale);
    case Types.DOUBLE:
    case Types.REAL:
      return DataTypes.DOUBLE();
    case Types.FLOAT:
      return DataTypes.FLOAT();
    case Types.INTEGER:
      return DataTypes.INT();
    case Types.SMALLINT:
      return DataTypes.SMALLINT();
    case Types.TINYINT:
      return DataTypes.TINYINT();
    case Types.VARBINARY:
      return DataTypes.VARBINARY(precision);
    case Types.TIMESTAMP:
      return DataTypes.TIMESTAMP(precision);
    default:
      throw new SQLException("Unsupported type " + metadata.getColumnTypeName(colIndex));
    }
  }

  @Override
  protected String getTableName(ObjectPath tablePath) {
    return tablePath.getObjectName();
  }

  @Override
  protected String getSchemaName(ObjectPath tablePath) {
    return tablePath.getDatabaseName();
  }

  @Override
  protected String getSchemaTableName(ObjectPath tablePath) {
    return tablePath.getFullName();
  }
}
