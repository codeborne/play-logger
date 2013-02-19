package play.modules.logger;

import play.PlayPlugin;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ExceptionsMonitoringPlugin extends PlayPlugin {

  static ConcurrentHashMap<Class, AtomicInteger> EXCEPTIONS = new ConcurrentHashMap<>();

  public static void add(Throwable throwable) {
    Class clazz = throwable.getClass();
    if (EXCEPTIONS.containsKey(clazz))
      EXCEPTIONS.get(clazz).incrementAndGet();
    else
      EXCEPTIONS.put(clazz, new AtomicInteger(1));
  }

  @Override public String getStatus() {
    StringWriter sw = new StringWriter();
    PrintWriter out = new PrintWriter(sw);

    out.println("Exceptions statistics:");
    out.println("~~~~~~~~~~~~~~~~~~~~~~");

    for (Map.Entry<Class, AtomicInteger> entry : EXCEPTIONS.entrySet()) {
      out.println(entry.getKey().getSimpleName() + " : " + entry.getValue().get());
    }

    return sw.toString();
  }
}
