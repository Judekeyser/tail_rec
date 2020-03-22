package be.jdevelopment.tailrec.lib.aspect;

import be.jdevelopment.tailrec.lib.threading.ContextBinderTemplate;

class ContextBinder extends ContextBinderTemplate {

    @Override protected void executeInContext(Runnable runnable) {
        new ContextThread(runnable).start();
    }

}
