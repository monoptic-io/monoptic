package io.monoptic.jdbc;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesApi;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesObject;
import io.kubernetes.client.util.generic.dynamic.Dynamics;
import org.jline.reader.Completer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sqlline.SqlLine;
import sqlline.CommandHandler;
import sqlline.DispatchCallback;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStream;

public class MonopticCliApp {
  private final Logger logger = LoggerFactory.getLogger(MonopticCliApp.class);
  private final Properties properties;

  private SqlLine sqlline;

  public MonopticCliApp(Properties properties) {
    this.properties = properties;
  }

  public static void main(String[] args) throws Exception {
    MonopticCliApp app = new MonopticCliApp(new Properties());
    String uri = "jdbc:monoptic:";
    String gateway = null;
    String serialization = "protobuf";
    String run = null;
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
      case "--gateway":
      case "-g":
        requireParam("--gateway", args, i);
        gateway = args[++i];
        break;
      case "--url":
      case "--uri":
      case "-u":
        requireParam("--url", args, i);
        uri = args[++i]; 
        break; 
      case "--serde":
      case "--serialization":
      case "-s":
        requireParam("--serialization", args, i);
        serialization = args[++i];
        break;
      case "--run":
      case "-r":
        requireParam("--run", args, i);
        run = args[++i];
        break;
      default:
        throw new IllegalArgumentException("Uknown argument " + args[i]);
      }
    }
    if (gateway != null) {
      uri = "jdbc:avatica:remote:serialization=" + serialization + ";url=" + gateway;
    }
    List<String> outArgs = new ArrayList<>();
    outArgs.add("-nn"); outArgs.add("monoptic");
    outArgs.add("-u"); outArgs.add(uri);
    outArgs.add("-n"); outArgs.add("");
    outArgs.add("-p"); outArgs.add("");
    if (run != null) {
      outArgs.add("--run=" + run);
    }
    int result = app.run(outArgs.toArray(new String[]{}));
    System.exit(result);
  }

  public int run(String[] args) throws IOException {
    this.sqlline = new SqlLine();
    Scanner scanner = new Scanner(Thread.currentThread().getContextClassLoader().getResourceAsStream("welcome.txt"));
    while (scanner.hasNext()) {
      sqlline.output(scanner.nextLine());
    }
    List<CommandHandler> commandHandlers = new ArrayList<>();
    commandHandlers.addAll(sqlline.getCommandHandlers()); // include default handlers
    commandHandlers.add(new IntroCommandHandler());
    commandHandlers.add(new InstallCommandHandler());
    sqlline.updateCommandHandlers(commandHandlers);
    return sqlline.begin(args, null, true).ordinal();
  }

  private static void requireParam(String arg, String []args, int i) {
    if (i + 1 > args.length || args[i + 1].charAt(0) == '-') {
      throw new IllegalArgumentException(arg + " requires a parameter.");
    }
  }

  private class IntroCommandHandler implements CommandHandler {

    @Override
    public String getName() {
      return "intro";
    }

    @Override
    public List<String> getNames() {
      return Collections.singletonList(getName());
    }

    @Override
    public String getHelpText() {
      return "What is Monoptic?";
    }

    @Override
    public String matches(String line) {
      if (startsWith(line, "!intro") || startsWith(line, "intro")) {
        return line;
      } else {
        return null;
      }
    }

    @Override
    public void execute(String line, DispatchCallback dispatchCallback) {
      Scanner scanner = new Scanner(Thread.currentThread().getContextClassLoader().getResourceAsStream("intro.txt"));
      while (scanner.hasNext()) {
        sqlline.output(scanner.nextLine());
      }
    }

    @Override
    public List<Completer> getParameterCompleters() {
      return Collections.emptyList();
    }

    @Override
    public boolean echoToFile() {
      return false;
    }
  }

  private class InstallCommandHandler implements CommandHandler {

    @Override
    public String getName() {
      return "install";
    }

    @Override
    public List<String> getNames() {
      return Collections.singletonList(getName());
    }

    @Override
    public String getHelpText() {
      return "Install Monoptic CRDs";
    }

    @Override
    public String matches(String line) {
      if (startsWith(line, "!install") || startsWith(line, "install")) {
        return line;
      } else {
        return null;
      }
    }

    @Override
    public void execute(String line, DispatchCallback dispatchCallback) {
      try {
        ApiClient apiClient = Config.defaultClient();
        applyResource("database.crd.yaml", apiClient);
        applyResource("engine.crd.yaml", apiClient);
        applyResource("view.crd.yaml", apiClient);
      } catch (Exception e) {
        // swallow
      }
    }

    @Override
    public List<Completer> getParameterCompleters() {
      return Collections.emptyList();
    }

    @Override
    public boolean echoToFile() {
      return false;
    }
  }

  static String readAll(InputStream in) {
    Scanner scanner = new Scanner(in);
    StringBuilder builder = new StringBuilder();
    while (scanner.hasNext()) {
      builder.append(scanner.nextLine());
      builder.append('\n');
    }
    return builder.toString();
  }

  private void applyResource(String name, ApiClient apiClient) throws ApiException {
    sqlline.output("Installing " + name);
    String yaml = readAll(Thread.currentThread().getContextClassLoader().getResourceAsStream(name));
    DynamicKubernetesObject obj = Dynamics.newFromYaml(yaml);
    DynamicKubernetesApi api = new DynamicKubernetesApi("apiextensions.k8s.io", "v1", "customresourcedefinitions", apiClient);
    api.create(obj).onFailure((code, status) -> sqlline.output(status.toString()));
  }
 
  private static boolean startsWith(String s, String prefix) {
    return s.matches("(?i)" + prefix + ".*");
  }
}
