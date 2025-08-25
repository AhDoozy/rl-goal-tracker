package com.toofifty.goaltracker.models.task;

import com.toofifty.goaltracker.models.enums.TaskType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.runelite.api.Skill;

@Getter
@Setter
@SuperBuilder
/**
 * Task representing earning a target XP amount in a skill.
 * Stores the RuneLite Skill and required XP.
 */
public final class SkillXpTask extends Task
{
    private Skill skill;
    private int xp;

    @Override
    public String toString()
    {
        return String.format("%d %s XP", xp, skill.getName());
    }

    @Override
    public String getDisplayName()
    {
        return String.format("%d %s XP", xp, skill.getName());
    }

    @Override
    public TaskType getType()
    {
        return TaskType.SKILL_XP;
    }
}
