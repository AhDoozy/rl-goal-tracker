package com.toofifty.goaltracker.models;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Simple undo/redo stack for user actions.
 * Push new actions with {@link #push(Action)}; call {@link #undo()} / {@link #redo()} from UI.
 */
public final class ActionHistory
{
    public interface Action
    {
        void undo();
        void redo();
    }

    private final Deque<Action> undoStack = new ArrayDeque<>();
    private final Deque<Action> redoStack = new ArrayDeque<>();

    /** Push a new action and clear the redo history. */
    public void push(final Action action)
    {
        Objects.requireNonNull(action, "action");
        undoStack.addLast(action);
        redoStack.clear();
    }

    /** Undo the most recent action, if any. */
    public void undo()
    {
        final Action action = undoStack.pollLast();
        if (action != null)
        {
            action.undo();
            redoStack.addLast(action);
        }
    }

    /** Redo the most recently undone action, if any. */
    public void redo()
    {
        final Action action = redoStack.pollLast();
        if (action != null)
        {
            action.redo();
            undoStack.addLast(action);
        }
    }

    public boolean hasUndo()
    {
        return !undoStack.isEmpty();
    }

    public boolean hasRedo()
    {
        return !redoStack.isEmpty();
    }

    /** Clear both undo and redo history. */
    public void clear()
    {
        undoStack.clear();
        redoStack.clear();
    }
}
