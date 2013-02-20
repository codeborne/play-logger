package play.modules.logger;

import play.PlayPlugin;
import play.cache.Cache;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ExceptionsMonitoringPlugin extends PlayPlugin {

  static void register(Throwable throwable) {

    ConcurrentHashMap<Class, AtomicInteger> exceptions = getExceptions();

    Class clazz = throwable.getClass();
    if (exceptions.containsKey(clazz))
      exceptions.get(clazz).incrementAndGet();
    else
      exceptions.put(clazz, new AtomicInteger(1));

    Cache.replace("exceptions", exceptions);
  }

  @Override public String getStatus() {
    StringWriter sw = new StringWriter();
    PrintWriter out = new PrintWriter(sw);

    out.println("Exceptions statistics:");
    out.println("~~~~~~~~~~~~~~~~~~~~~~");

    for (Map.Entry<Class, AtomicInteger> entry : getExceptions().entrySet()) {
      out.println(entry.getKey().getSimpleName() + " : " + entry.getValue().get());
    }

    return sw.toString();
  }

  @SuppressWarnings("unchecked") private static ConcurrentHashMap<Class, AtomicInteger> getExceptions() {
    ConcurrentHashMap<Class, AtomicInteger> map = (ConcurrentHashMap<Class, AtomicInteger>) Cache.get("exceptions");
    if (map == null) map = new ConcurrentHashMap<>();
    return map;
  }
}
