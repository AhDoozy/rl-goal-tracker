package com.toofifty.goaltracker.models.task;

import com.toofifty.goaltracker.models.enums.TaskType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.runelite.api.Skill;

@Setter
@Getter
@SuperBuilder
/**
 * Task representing reaching a target level in a skill.
 * Stores the RuneLite Skill and required level.
 */
public final class SkillLevelTask extends Task
{
    private Skill skill;
    private int level;

    @Override
    public String toString()
    {
        return String.format("%s %s", level, skill.getName());
    }

    @Override
    public String getDisplayName()
    {
        return String.format("Reach level %d %s", level, skill.getName());
    }

    @Override
    public TaskType getType()
    {
        return TaskType.SKILL_LEVEL;
    }
}
