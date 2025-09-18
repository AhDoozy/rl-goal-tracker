package com.toofifty.goaltracker.utils;

import com.toofifty.goaltracker.models.task.QuestTask;
import com.toofifty.goaltracker.models.task.SkillLevelTask;
import com.toofifty.goaltracker.models.task.Task;
import net.runelite.api.Quest;
import net.runelite.api.Skill;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QuestRequirementsTest
{
    @Test
    void testGetRequirements()
    {
        List<Task> requirements = QuestRequirements.getRequirements(Quest.FAIRYTALE_I__GROWING_PAINS, 0);
        assertNotNull(requirements, "Requirements list should not be null");
        assertEquals(3, requirements.size(), "Fairytale I should have 3 direct requirements");

        // All items should be indented one level when called with indentLevel=0
        requirements.forEach(t -> assertEquals(1, t.getIndentLevel(), "Each requirement should have indent 1"));

        // Expect two quest tasks and one skill requirement
        long questCount = requirements.stream().filter(t -> t instanceof QuestTask).count();
        long skillCount = requirements.stream().filter(t -> t instanceof SkillLevelTask).count();
        assertEquals(2, questCount, "Should contain two QuestTask requirements");
        assertEquals(1, skillCount, "Should contain one SkillLevelTask requirement");

        // Verify specific quests are present
        boolean hasLostCity = requirements.stream().anyMatch(t -> t instanceof QuestTask && ((QuestTask) t).getQuest() == Quest.LOST_CITY);
        boolean hasNatureSpirit = requirements.stream().anyMatch(t -> t instanceof QuestTask && ((QuestTask) t).getQuest() == Quest.NATURE_SPIRIT);
        assertTrue(hasLostCity, "Lost City should be a requirement");
        assertTrue(hasNatureSpirit, "Nature Spirit should be a requirement");

        // Verify specific skill requirement (Farming 18)
        boolean hasFarming18 = requirements.stream().anyMatch(t -> t instanceof SkillLevelTask && ((SkillLevelTask) t).getSkill() == Skill.FARMING && ((SkillLevelTask) t).getLevel() == 18);
        assertTrue(hasFarming18, "Farming 18 should be a requirement");
    }

    @Test
    void testRequirementsAreFreshInstances()
    {
        List<Task> first = QuestRequirements.getRequirements(Quest.FAIRYTALE_I__GROWING_PAINS, 0);
        List<Task> second = QuestRequirements.getRequirements(Quest.FAIRYTALE_I__GROWING_PAINS, 0);

        assertEquals(first.size(), second.size(), "Both calls should return the same number of items");
        for (int i = 0; i < first.size(); i++) {
            assertNotSame(first.get(i), second.get(i), "Each requirement should be a fresh instance on each call");
            assertEquals(first.get(i).getClass(), second.get(i).getClass(), "Classes should match between calls");
            assertEquals(first.get(i).getIndentLevel(), second.get(i).getIndentLevel(), "Indent levels should match between calls");
        }
    }
}