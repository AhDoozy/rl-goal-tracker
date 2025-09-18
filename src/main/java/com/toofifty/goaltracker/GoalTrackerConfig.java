package com.toofifty.goaltracker;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

/**
 * RuneLite config group for Goal Tracker plugin.
 * Stores serialized goals, item cache data, and completion message color.
 */
@ConfigGroup("goaltracker")
public interface GoalTrackerConfig extends Config
{
    @ConfigItem(keyName = "goalTrackerData", name = "", description = "", hidden = true)
    default String goalTrackerData()
    {
        return "";
    }

    @ConfigItem(keyName = "goalTrackerData", name = "", description = "", hidden = true)
    void goalTrackerData(String str);

    @ConfigItem(keyName = "goalTrackerItemCache", name = "", description = "", hidden = true)
    default String goalTrackerItemCache()
    {
        return "";
    }

    @ConfigItem(keyName = "goalTrackerItemCache", name = "", description = "", hidden = true)
    void goalTrackerItemCache(String str);

    @ConfigItem(keyName = "goalTrackerItemNoteMapCache", name = "", description = "", hidden = true)
    default String goalTrackerItemNoteMapCache()
    {
        return "";
    }

    @ConfigItem(keyName = "goalTrackerItemNoteMapCache", name = "", description = "", hidden = true)
    void goalTrackerItemNoteMapCache(String str);

    @ConfigItem(
        keyName = "completionMessageColor",
        name = "Completion Message Color",
        description = "Color of the chat message when a goal is completed"
    )
    @Alpha
    default Color completionMessageColor()
    {
        return new Color(0xF227A509, true);
    }
}
