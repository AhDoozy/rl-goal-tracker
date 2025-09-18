package com.toofifty.goaltracker.models;

import com.google.gson.annotations.SerializedName;
import com.toofifty.goaltracker.models.enums.Status;
import com.toofifty.goaltracker.models.task.Task;
import com.toofifty.goaltracker.utils.ReorderableList;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents a player goal containing one or more tasks.
 * Provides helpers for status aggregation and task updates.
 */
@Setter
@Getter
@SuperBuilder
public final class Goal
{
    @Builder.Default
    private String description = "New goal";

    @Builder.Default
    private int displayOrder = -1;

    @Builder.Default
    private boolean pinned = false;

    @SerializedName("items")
    @Builder.Default
    private ReorderableList<Task> tasks = new ReorderableList<>();

    private List<Task> filterBy(Predicate<Task> predicate)
    {
        return tasks.stream().filter(predicate).collect(Collectors.toList());
    }

    /** True if all tasks are of the given status. */
    public boolean isStatus(Status status)
    {
        return tasks.stream().allMatch(task -> task.getStatus() == status);
    }

    /** True if any task matches one of the given statuses. */
    public boolean isAnyStatus(Status... statuses)
    {
        return tasks.stream().anyMatch(task ->
            Arrays.stream(statuses).anyMatch(s -> s == task.getStatus()));
    }

    /** List of completed tasks. */
    public List<Task> getComplete()
    {
        return filterBy(Task::isDone);
    }

    /** Aggregated status of this goal. */
    public Status getStatus()
    {
        if (isStatus(Status.COMPLETED))
        {
            return Status.COMPLETED;
        }
        if (isAnyStatus(Status.IN_PROGRESS, Status.COMPLETED))
        {
            return Status.IN_PROGRESS;
        }
        return Status.NOT_STARTED;
    }

    /** Mark all tasks as complete or not started. */
    public void setAllTasksCompleted(boolean completed)
    {
        for (Task task : tasks)
        {
            task.setStatus(completed ? Status.COMPLETED : Status.NOT_STARTED);
        }
    }
}
