package com.toofifty.goaltracker.models;

import com.toofifty.goaltracker.models.task.Task;

import java.lang.reflect.Method;

/**
 * Action for toggling a task's completion state.
 * Uses reflection to avoid a hard dependency on the Status enum.
 */
public final class ToggleCompleteAction implements ActionHistory.Action
{
    private final Task task;
    private final boolean oldValue;
    private final boolean newValue;

    public ToggleCompleteAction(Task task, boolean oldValue, boolean newValue)
    {
        this.task = task;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public void undo()
    {
        setStatusByName(oldValue ? "COMPLETED" : "NOT_STARTED");
    }

    @Override
    public void redo()
    {
        setStatusByName(newValue ? "COMPLETED" : "NOT_STARTED");
    }

    /**
     * Set task status by enum name without importing the enum type.
     * This works whether Status is a nested enum or a top-level type.
     */
    private void setStatusByName(String name)
    {
        try
        {
            Object current = task.getStatus();
            Class<?> enumClass = current.getClass();
            @SuppressWarnings({"unchecked","rawtypes"})
            Enum newStatus = Enum.valueOf((Class<Enum>) enumClass, name);

            Method m = task.getClass().getMethod("setStatus", enumClass);
            m.invoke(task, newStatus);
        }
        catch (Exception ignored)
        {
            // If we can't reflectively set it, ignore rather than crash.
        }
    }
}
