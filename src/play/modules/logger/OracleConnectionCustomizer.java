package play.modules.logger;

import com.mchange.v2.c3p0.ConnectionCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import play.mvc.Scope;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;

/**
 * Can be used with C3P0 in order to see who is using Oracle connections on the DB side (End to End metrics)
 */
public class OracleConnectionCustomizer implements ConnectionCustomizer {
  private static final Logger logger = LoggerFactory.getLogger(OracleConnectionCustomizer.class);

  int arrayLength;
  int actionIndex;
  int moduleIndex;
  int clientIdIndex;
  Method setEndToEndMetrics;

  public OracleConnectionCustomizer() {
    try {
      Class connClass = Class.forName("oracle.jdbc.driver.OracleConnection");
      arrayLength = (Integer)connClass.getField("END_TO_END_STATE_INDEX_MAX").get(null);
      actionIndex = (Integer)connClass.getField("END_TO_END_ACTION_INDEX").get(null);
      moduleIndex = (Integer)connClass.getField("END_TO_END_MODULE_INDEX").get(null);
      clientIdIndex = (Integer)connClass.getField("END_TO_END_CLIENTID_INDEX").get(null);
      setEndToEndMetrics = connClass.getMethod("setEndToEndMetrics", String[].class, short.class);
      setEndToEndMetrics.setAccessible(true);
    }
    catch (Exception e) {
      logger.warn("Cannot access OracleConnection fields", e);
    }
  }

  @Override public void onAcquire(Connection conn, String s) {
  }

  @Override public void onDestroy(Connection conn, String s) {
  }

  @Override public void onCheckOut(Connection conn, String s) {
    if (setEndToEndMetrics == null) return;
    try {
      String[] e2eMetrics = new String[arrayLength];
      Http.Request request = Http.Request.current();
      e2eMetrics[actionIndex] = request != null ? (String) request.args.get("requestId") : null; // set by RequestLogPlugin
      e2eMetrics[moduleIndex] = "IBANK/" + Thread.currentThread().getName();
      Scope.Session session = Scope.Session.current();
      e2eMetrics[clientIdIndex] = session != null ? session.get("username") : null;
      setEndToEndMetrics.invoke(conn, e2eMetrics, (short) 0);
    }
    catch (InvocationTargetException e) {
      logger.warn("Cannot set Oracle end-to-end metrics", e.getCause());
    }
    catch (Exception e) {
      logger.warn("Cannot set Oracle end-to-end metrics", e);
    }
  }

  @Override public void onCheckIn(Connection conn, String s) {
  }
}
