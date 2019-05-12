package database;

import wholesale.callback.CallBack;

public class ActionExec extends DatabaseActionExec {

    private final static String TAG = "ActionExec";

    public ActionExec(DatabaseAction databaseAction) {
        super(databaseAction);
    }

    @Override
    public void beginExecution(CallBack callBack)  {
         databaseAction.onAccess(callBack);
    }
}
