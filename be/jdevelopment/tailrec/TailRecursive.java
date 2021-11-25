package be.jdevelopment.tailrec;

import java.lang.invoke.*;
import java.lang.reflect.*;

public enum TailRecursive {;

  @SuppressWarnings("unchecked")
  public static <T> T optimize (Class<T> clz, String expectedMethodName, InvocationHandler ctx) {
    var TOKEN = new Object();

    class SwitchCallSite implements InvocationHandler {
      private boolean s = false; private Object[] args;
      @Override public Object invoke (Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals(expectedMethodName)) {
          this.s = !this.s;
          if (s) {
            this.args = this.args == null ? args : this.args;
            return ctx .invoke(proxy, method, this.args);
          } else {
            this.args = args;
            return TOKEN;
          }
        } else throw new IllegalStateException();
      }
    }

    class TailRecTrap implements InvocationHandler {
      private Object sFibo; private SwitchCallSite callSite;
      @Override public Object invoke (Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals(expectedMethodName)) {
          if (sFibo == null) {
            assert callSite == null;
            callSite = new SwitchCallSite();
            sFibo = Proxy .newProxyInstance (clz.getClassLoader(), new Class[] {clz}, callSite);
          }
          for(;;) {
            var caught = callSite .invoke (sFibo, method, args);
            if (caught != TOKEN) return caught;
          }
        }
        return ctx .invoke(proxy, method, args);
      }
    }

    return (T) Proxy .newProxyInstance (clz.getClassLoader(), new Class[] {clz}, new TailRecTrap());
  }

}
