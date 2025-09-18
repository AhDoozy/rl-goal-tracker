package com.toofifty.goaltracker.services;

import com.toofifty.goaltracker.ItemCache;
import com.toofifty.goaltracker.models.enums.Status;
import com.toofifty.goaltracker.models.task.*;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.StatChanged;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Updates task status based on current client state and events.
 */
@Singleton
public final class TaskUpdateService
{
    @Inject private Client client;
    @Inject private ItemCache itemCache;

    /**
     * Dispatch update for a generic task, returning true if status/values changed.
     */
    public boolean update(Task task)
    {
        switch (task.getType())
        {
            case SKILL_LEVEL: return update((SkillLevelTask) task);
            case SKILL_XP:    return update((SkillXpTask) task);
            case QUEST:       return update((QuestTask) task);
            case ITEM:        return update((ItemTask) task);
            default:          return false;
        }
    }

    // -------------------- Skill Level --------------------

    /** Returns true if an update has occurred. */
    public boolean update(SkillLevelTask task)
    {
        if (client.getGameState() != GameState.LOGGED_IN || !client.isClientThread())
        {
            return false;
        }
        return update(task, client.getRealSkillLevel(task.getSkill()));
    }

    /** Returns true if an update has occurred (event-driven). */
    public boolean update(SkillLevelTask task, StatChanged event)
    {
        if (event.getSkill() != task.getSkill())
        {
            return false;
        }
        return update(task, event.getLevel());
    }

    /** Returns true if an update has occurred given a specific level. */
    public boolean update(SkillLevelTask task, int level)
    {
        final Status oldStatus = task.getStatus();
        task.setStatus(level >= task.getLevel() ? Status.COMPLETED : Status.NOT_STARTED);
        return oldStatus != task.getStatus();
    }

    // -------------------- Skill XP --------------------

    /** Returns true if an update has occurred. */
    public boolean update(SkillXpTask task)
    {
        if (client.getGameState() != GameState.LOGGED_IN || !client.isClientThread())
        {
            return false;
        }
        return update(task, client.getSkillExperience(task.getSkill()));
    }

    /** Returns true if an update has occurred (event-driven). */
    public boolean update(SkillXpTask task, StatChanged event)
    {
        if (event.getSkill() != task.getSkill())
        {
            return false;
        }
        return update(task, event.getXp());
    }

    /** Returns true if an update has occurred given a specific XP value. */
    public boolean update(SkillXpTask task, int xp)
    {
        final Status oldStatus = task.getStatus();
        task.setStatus(xp >= task.getXp() ? Status.COMPLETED : Status.NOT_STARTED);
        return oldStatus != task.getStatus();
    }

    // -------------------- Quest --------------------

    /** Returns true if an update has occurred. */
    public boolean update(QuestTask task)
    {
        if (client.getGameState() != GameState.LOGGED_IN || !client.isClientThread())
        {
            return false;
        }
        final Status oldStatus = task.getStatus();
        task.setStatus(Status.fromQuestState(task.getQuest().getState(client)));
        return oldStatus != task.getStatus();
    }

    // -------------------- Item --------------------

    /** Returns true if an update has occurred. */
    public boolean update(ItemTask task)
    {
        final int oldAcquired = task.getAcquired();
        final Status oldStatus = task.getStatus();

        task.setAcquired(Math.min(itemCache.getTotalQuantity(task.getItemId()), task.getQuantity()));

        task.setStatus(
            task.getAcquired() >= task.getQuantity()
                ? Status.COMPLETED
                : (task.getAcquired() > 0 ? Status.IN_PROGRESS : Status.NOT_STARTED)
        );

        return oldAcquired != task.getAcquired() || oldStatus != task.getStatus();
    }
}
