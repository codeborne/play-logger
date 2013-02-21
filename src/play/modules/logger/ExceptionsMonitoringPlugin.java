package play.modules.logger;

import play.PlayPlugin;
import play.exceptions.JavaExecutionException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

public class ExceptionsMonitoringPlugin extends PlayPlugin {

  private static ConcurrentHashMap<String, AtomicInteger> exceptions = new ConcurrentHashMap<>();

  public static void register(Throwable throwable) {
    if (throwable instanceof JavaExecutionException) throwable = throwable.getCause();
    String key = throwable.toString().split("\n")[0];
    AtomicInteger value = exceptions.get(key);
    if (value == null) exceptions.put(key, value = new AtomicInteger());
    value.incrementAndGet();
  }

  @Override public String getStatus() {
    StringWriter sw = new StringWriter();
    PrintWriter out = new PrintWriter(sw);

    out.println("Exceptions statistics:");
    out.println("~~~~~~~~~~~~~~~~~~~~~~");

    ArrayList<Map.Entry<String, AtomicInteger>> sorted = new ArrayList<>(exceptions.entrySet());
    Collections.sort(sorted, new Comparator<Map.Entry<String, AtomicInteger>>() {
      @Override public int compare(Map.Entry<String, AtomicInteger> o1, Map.Entry<String, AtomicInteger> o2) {
        return o2.getValue().get() - o1.getValue().get();
      }
    });

    for (Map.Entry<String, AtomicInteger> entry : sorted) {
      out.println(format("%4d : %s", entry.getValue().get(), entry.getKey()));
    }

    return sw.toString();
  }
}
