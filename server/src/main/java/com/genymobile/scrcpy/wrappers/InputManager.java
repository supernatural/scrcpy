package com.genymobile.scrcpy.wrappers;

import com.genymobile.scrcpy.Ln;

import android.content.Intent;
import android.os.IInterface;
import android.view.InputEvent;

import com.genymobile.scrcpy.Ln;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class InputManager {

    public static final int INJECT_INPUT_EVENT_MODE_ASYNC = 0;
    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT = 1;
    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2;

    private final IInterface manager;
    private IInterface mAm = null;
    private Method broadcastIntent = null;
    private Method injectInputEventMethod;

    private static Method setDisplayIdMethod;

    public InputManager(IInterface manager) {
        this.manager = manager;
    }

    private Method getInjectInputEventMethod() throws NoSuchMethodException {
        if (injectInputEventMethod == null) {
            injectInputEventMethod = manager.getClass().getMethod("injectInputEvent", InputEvent.class, int.class);
        }
        return injectInputEventMethod;
    }

    public void setAm(IInterface am, Method method) {
        mAm = am;
        broadcastIntent = method;
    }

    public boolean injectInputEvent(InputEvent inputEvent, int mode) {
        try {
            Method method = getInjectInputEventMethod();
            return (boolean) method.invoke(manager, inputEvent, mode);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            Ln.e("Could not invoke method", e);
            return false;
        }
    }

    private static Method getSetDisplayIdMethod() throws NoSuchMethodException {
        if (setDisplayIdMethod == null) {
            setDisplayIdMethod = InputEvent.class.getMethod("setDisplayId", int.class);
        }
        return setDisplayIdMethod;
    }

    public static boolean setDisplayId(InputEvent inputEvent, int displayId) {
        try {
            Method method = getSetDisplayIdMethod();
            method.invoke(inputEvent, displayId);
            return true;
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            Ln.e("Cannot associate a display id to the input event", e);
            return false;
        }
    }

    public boolean injectText(String text) {
        if (mAm != null && broadcastIntent != null) {
            Intent i = new Intent();
            i.setAction("ADB_INPUT_TEXT");
            i.putExtra("msg", text);

            try {
                broadcastIntent.invoke(mAm, null, i, null, null, 0, null, null, null,
                        -1, null, true, false, 0);
                return true;
            } catch (Exception e) {
                Ln.e("sendBroadcast failed", e);
            }
        }
        return false;
    }
}
