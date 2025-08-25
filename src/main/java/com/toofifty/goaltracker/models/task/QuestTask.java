package com.toofifty.goaltracker.models.task;

import com.google.gson.annotations.SerializedName;
import com.toofifty.goaltracker.models.enums.Status;
import com.toofifty.goaltracker.models.enums.TaskType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

/**
 * Task representing completion of a quest.
 * Stores the RuneLite Quest reference and returns its name as label.
 */
@Getter
@Setter
@SuperBuilder
public final class QuestTask extends Task
{
    @SerializedName("quest_id")
    private Quest quest;

    /**
     * Map RuneLite's QuestState to this plugin's Task.Status.
     */
    private static Status mapQuestState(final QuestState state)
    {
        if (state == null)
        {
            return Status.NOT_STARTED;
        }
        switch (state)
        {
            case FINISHED:
                return Status.COMPLETED;
            case IN_PROGRESS:
                return Status.IN_PROGRESS;
            case NOT_STARTED:
            default:
                return Status.NOT_STARTED;
        }
    }

    /**
     * Re-evaluate this quest's completion status from the live client and update the Task.status field.
     * Safe to call on login and whenever relevant varbits/varps change.
     */
    public void refreshStatus(final Client client)
    {
        if (client == null || quest == null)
        {
            return;
        }
        final QuestState qs = quest.getState(client);
        setStatus(mapQuestState(qs));
    }

    @Override
    public String toString()
    {
        return quest.getName();
    }

    @Override
    public String getDisplayName()
    {
        return quest.getName();
    }

    @Override
    public TaskType getType()
    {
        return TaskType.QUEST;
    }
}
