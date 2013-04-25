package com.iheanyiekechukwu.tubalr;

public class ForegroundHelper {
    public static boolean[] activityStates = {false, false};
    public static final int MAINACT = 0;
    public static final int PLAYACT = 1;


    public static boolean activityExistsInForeground(){
        for(boolean b : activityStates){
            if(b)
                return true;
        }

        return false;
    }
}
