package com.toofifty.goaltracker.models;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Generic undo/redo stack for removed items (e.g., Goals, Tasks).
 * Stores the removed item and its original index for correct restoration.
 */
public final class UndoStack<T>
{
    public static final class RemovedEntry<T>
    {
        private final T item;
        private final int index;

        public RemovedEntry(T item, int index)
        {
            this.item = Objects.requireNonNull(item, "item");
            this.index = index;
        }

        public T getItem()
        {
            return item;
        }

        public int getIndex()
        {
            return index;
        }
    }

    private final Deque<RemovedEntry<T>> undo = new ArrayDeque<>();
    private final Deque<RemovedEntry<T>> redo = new ArrayDeque<>();

    /** Record a removal and clear redo history. */
    public void pushRemove(T item, int index)
    {
        undo.addLast(new RemovedEntry<>(item, index));
        redo.clear();
    }

    /**
     * Prepare to UNDO the most-recent removal.
     *
     * Moves the entry from the undo stack to the redo stack and returns it.
     * Caller should re-insert the item at {@code getIndex()}.
     */
    public RemovedEntry<T> popForUndo()
    {
        RemovedEntry<T> e = undo.pollLast();
        if (e != null)
        {
            redo.addLast(e);
        }
        return e;
    }

    /**
     * Prepare to REDO the most-recent undo (i.e., remove again).
     *
     * Moves the entry from the redo stack back to the undo stack and returns it.
     * Caller should remove the item again.
     */
    public RemovedEntry<T> popForRedo()
    {
        RemovedEntry<T> e = redo.pollLast();
        if (e != null)
        {
            undo.addLast(e);
        }
        return e;
    }

    public boolean hasUndo()
    {
        return !undo.isEmpty();
    }

    public boolean hasRedo()
    {
        return !redo.isEmpty();
    }

    /** Clears all history. */
    public void clear()
    {
        undo.clear();
        redo.clear();
    }
}
