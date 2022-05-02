package be.jdevelopment.tailrec;

import java.lang.reflect.*;

public enum TailRecursive {;

  @SuppressWarnings("unchecked")
  public static <T> T optimize (Class<T> clz, String expectedMethodName, InvocationHandler ctx) {
    var TOKEN = new Object();
    Object[][] _args = {null};

    class SwitchCallSite implements InvocationHandler {
      private boolean s = false;

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        assert expectedMethodName.equals(method.getName())
          :"Proxy method invoke error: only one method should be delegated, but got %s".formatted(method.getName());
        return ((s = !s) || (_args[0] = args) == null /* never true*/) ? ctx.invoke(proxy, method, _args[0]): TOKEN;
      }
    }

    class TailRecTrap implements InvocationHandler {
      private final SwitchCallSite callSite = new SwitchCallSite();
      private final Object sFibo = Proxy .newProxyInstance (clz.getClassLoader(), new Class[] {clz}, callSite);
      @Override public Object invoke (Object proxy, Method method, Object[] args) throws Throwable {
        if (expectedMethodName.equals(method.getName())) {
          var caught = TOKEN;
          _args[0] = args;
          while(caught == TOKEN)
            caught = callSite.invoke(sFibo, method, args /* forced cause invoke is sensitive to shape of array */);
          return caught;
        }
        return ctx .invoke(proxy, method, args);
      }
    }

    return (T) Proxy .newProxyInstance (clz.getClassLoader(), new Class[] {clz}, new TailRecTrap());
  }

}
