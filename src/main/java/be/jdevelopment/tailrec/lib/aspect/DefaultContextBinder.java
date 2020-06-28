package be.jdevelopment.tailrec.lib.aspect;

import be.jdevelopment.tailrec.lib.threading.ContextBinderTemplate;

class DefaultContextBinder extends ContextBinderTemplate {

    private final MethodExecutionContextBasicImpl holder;
    DefaultContextBinder(MethodExecutionContextBasicImpl holder) {
        this.holder = holder;
    }

    @Override protected void executeInContext(ContextualizedRunner runnable) {
        runnable.run(holder.getMethodExecutionContext());
    }

}
