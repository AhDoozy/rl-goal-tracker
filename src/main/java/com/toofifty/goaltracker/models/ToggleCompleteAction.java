package com.toofifty.goaltracker.models;

import com.toofifty.goaltracker.models.enums.Status;
import com.toofifty.goaltracker.models.task.Task;

/**
 * Action for toggling a task's completion state.
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
        applyCompletion(oldValue);
    }

    @Override
    public void redo()
    {
        applyCompletion(newValue);
    }

    private void applyCompletion(boolean completed)
    {
        task.setStatus(completed ? Status.COMPLETED : Status.NOT_STARTED);
    }
}
