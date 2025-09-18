package com.toofifty.goaltracker.models;

import com.toofifty.goaltracker.models.task.Task;

import java.util.List;

/**
 * Action for reordering a task within a list (move up/down).
 * Stores old and new indices for undo/redo.
 */
public final class ReorderTaskAction implements ActionHistory.Action
{
    private final List<Task> tasks;
    private final Task task;
    private final int oldIndex;
    private final int newIndex;

    public ReorderTaskAction(List<Task> tasks, Task task, int oldIndex, int newIndex)
    {
        this.tasks = tasks;
        this.task = task;
        this.oldIndex = oldIndex;
        this.newIndex = newIndex;
    }

    @Override
    public void undo()
    {
        tasks.remove(task);
        int insertIndex = Math.max(0, Math.min(oldIndex, tasks.size()));
        tasks.add(insertIndex, task);
    }

    @Override
    public void redo()
    {
        tasks.remove(task);
        int insertIndex = Math.max(0, Math.min(newIndex, tasks.size()));
        tasks.add(insertIndex, task);
    }
}
