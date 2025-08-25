package com.toofifty.goaltracker.models;

import com.toofifty.goaltracker.models.task.Task;

import java.util.List;

/**
 * Action for indenting or outdenting a task in a list.
 * Stores old and new indent levels for undo/redo.
 */
public final class IndentChangeAction implements ActionHistory.Action
{
    private final List<Task> tasks;
    private final Task task;
    private final int oldIndent;
    private final int newIndent;

    public IndentChangeAction(List<Task> tasks, Task task, int oldIndent, int newIndent)
    {
        this.tasks = tasks;
        this.task = task;
        this.oldIndent = oldIndent;
        this.newIndent = newIndent;
    }

    @Override
    public void undo()
    {
        task.setIndentLevel(oldIndent);
    }

    @Override
    public void redo()
    {
        task.setIndentLevel(newIndent);
    }
}
