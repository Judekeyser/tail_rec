package be.jdevelopment.tailrec.lib.aspect;

import be.jdevelopment.tailrec.lib.threading.ContextBinderTemplate;

class DefaultContextBinder extends ContextBinderTemplate {

    private DefaultContextHolder holder;
    DefaultContextBinder(DefaultContextHolder holder) {
        this.holder = holder;
    }

    @Override protected void executeInContext(ContextualizedRunner runnable) {
        runnable.run(holder.getMethodExecutionContext());
    }

}
