package com.toofifty.goaltracker;

import com.toofifty.goaltracker.models.Goal;
import com.toofifty.goaltracker.models.enums.Status;
import com.toofifty.goaltracker.models.enums.TaskType;
import com.toofifty.goaltracker.models.task.Task;
import com.toofifty.goaltracker.utils.ReorderableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Singleton
/**
 * Central manager for plugin goals.
 * Handles create/load/save, JSON import/export, and listener notifications.
 */
public final class GoalManager
{
    @Inject
    private GoalTrackerConfig config;

    @Inject
    private GoalSerializer goalSerializer;

    @Getter
    private final ReorderableList<Goal> goals = new ReorderableList<>();

    private final List<Runnable> goalsChangedListeners = new ArrayList<>();

    public Goal createGoal()
    {
        Goal goal = Goal.builder().build();
        goals.add(goal);
        return goal;
    }

    /**
     * Append a batch of goals and persist + notify listeners.
     */
    public void addGoals(List<Goal> newGoals)
    {
        if (newGoals == null || newGoals.isEmpty())
        {
            return;
        }
        goals.addAll(newGoals);
        save();
    }

    @SuppressWarnings("unchecked")
    public <T extends Task> List<T> getTasksByTypeAndAnyStatus(TaskType type, Status... statuses)
    {
        List<T> tasks = new ArrayList<>();
        for (Goal goal : goals)
        {
            tasks.addAll((List<T>) goal.getTasks().stream()
                    .filter(task -> task.getType() == type && Arrays.stream(statuses).anyMatch(status -> status == task.getStatus()))
                    .collect(Collectors.toList()));
        }
        return tasks;
    }

    public <T extends Task> List<T> getIncompleteTasksByType(TaskType type)
    {
        return this.getTasksByTypeAndAnyStatus(type, Status.NOT_STARTED, Status.IN_PROGRESS);
    }

    public void save()
    {
        config.goalTrackerData(goalSerializer.serialize(goals));
        log.info("Saved " + goals.size() + " goals");
        notifyGoalsChanged();
    }

    public void load()
    {
        try
        {
            this.goals.clear();
            this.goals.addAll(goalSerializer.deserialize(config.goalTrackerData()));
            notifyGoalsChanged();
            log.info("Loaded " + this.goals.size() + " goals");
        }
        catch (Exception e)
        {
            log.error("Failed to load goals!", e);
        }
    }
    /**
     * Return the current goals as JSON for export.
     * @param pretty pretty-print output
     */
    public String exportJson(boolean pretty)
    {
        return goalSerializer.serialize(goals, pretty);
    }

    /**
     * Import goals from JSON and persist them.
     */
    public void importJson(String json)
    {
        try
        {
            this.goals.clear();
            this.goals.addAll(goalSerializer.deserialize(json));
            save();
        }
        catch (Exception e)
        {
            log.error("Failed to import goals!", e);
        }
    }
    public void addGoalsChangedListener(Runnable listener)
    {
        if (listener != null && !goalsChangedListeners.contains(listener))
        {
            goalsChangedListeners.add(listener);
        }
    }

    private void notifyGoalsChanged()
    {
        for (Runnable r : goalsChangedListeners)
        {
            try { r.run(); } catch (Exception ignored) {}
        }
    }
}