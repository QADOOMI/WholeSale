package database;

import java.util.concurrent.ExecutionException;

import wholesale.callback.CallBack;

// ABSTRACTION
public abstract class DatabaseActionExec {

    protected DatabaseAction databaseAction;

    protected DatabaseActionExec(DatabaseAction databaseAction) {
        this.databaseAction = databaseAction;
    }

    public abstract void beginExecution(CallBack callBack)
            throws ExecutionException, InterruptedException;
}
