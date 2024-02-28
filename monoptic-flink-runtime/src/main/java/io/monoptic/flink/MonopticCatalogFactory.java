package io.monoptic.flink;

import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.table.factories.CatalogFactory;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.flink.table.catalog.Catalog;
import org.apache.flink.table.catalog.CommonCatalogOptions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MonopticCatalogFactory implements CatalogFactory {

  private static final ConfigOption<String> DEFAULT_DATABASE = ConfigOptions.key(CommonCatalogOptions.DEFAULT_DATABASE_KEY)
      .stringType().defaultValue("default");
  private static final ConfigOption<String> USERNAME = ConfigOptions.key("username").stringType().defaultValue("noname");
  private static final ConfigOption<String> PASSWORD = ConfigOptions.key("password").stringType().defaultValue("nopass");
  private static final ConfigOption<String> BASE_URL = ConfigOptions.key("base-url").stringType()
      .defaultValue("jdbc:monoptic://kubernetes");

  @Override
  public String factoryIdentifier() {
    return "monoptic";
  }      

  @Override
  public Set<ConfigOption<?>> optionalOptions() {
    Set<ConfigOption<?>> options = new HashSet<>();
    options.add(DEFAULT_DATABASE);
    options.add(USERNAME);
    options.add(PASSWORD);
    options.add(BASE_URL);
    return options;
  }

  @Override
  public Set<ConfigOption<?>> requiredOptions() {
    return Collections.emptySet();
  }

  @Override
  public Catalog createCatalog(CatalogFactory.Context context) {
    FactoryUtil.CatalogFactoryHelper helper = FactoryUtil.createCatalogFactoryHelper(this, context);
    helper.validate();
    return new MonopticCatalog(context.getClassLoader(), context.getName(), helper.getOptions().get(DEFAULT_DATABASE),
        helper.getOptions().get(USERNAME), helper.getOptions().get(PASSWORD), helper.getOptions().get(BASE_URL));
  }
}
