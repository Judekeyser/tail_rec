package be.jdevelopment.tailrec.lib.threading;

class ContextThread extends Thread implements WithMethodExecutionContext {

    private final MethodExecutionContext ctx = new MethodExecutionContextBasicImpl();

    ContextThread(Runnable r) {
        super(r);
    }

    @Override public MethodExecutionContext getMethodExecutionContext() {
        return ctx;
    }
}