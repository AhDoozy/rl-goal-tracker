package com.toofifty.goaltracker.ui.inputs;

import com.toofifty.goaltracker.GoalTrackerPlugin;
import com.toofifty.goaltracker.models.Goal;
import com.toofifty.goaltracker.models.task.QuestTask;
import com.toofifty.goaltracker.ui.components.ComboBox;
import net.runelite.api.Quest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Input panel for creating Quest tasks.
 * Provides a searchable dropdown with RuneScape UF font and Enter-to-submit support.
 */
public final class QuestTaskInput extends TaskInput
{
    private final List<Quest> allQuests;
    private Quest bestMatch;
    private final ComboBox<Quest> questDropdown;

    public QuestTaskInput(GoalTrackerPlugin plugin, Goal goal)
    {
        super(plugin, goal, "Quest");

        // Initialize all quests and sort them
        allQuests = Arrays.asList(Quest.values());
        allQuests.sort(Comparator.comparing(Quest::getName));

        // Initialize dropdown via shared ComboBox for consistent arrows and crisp fonts
        questDropdown = new ComboBox<>(allQuests);
        // questDropdown.setCompact(true); // ~10% smaller font
        questDropdown.setFormatter(q -> q != null ? q.getName() : "");

        // Force RuneScape UF font at normal size
        Font rsFont = new Font("RuneScape UF", Font.PLAIN, 10);
        questDropdown.setFont(rsFont);



        questDropdown.setSelectedIndex(-1); // no selection initially
        questDropdown.addActionListener(e -> {
            Quest selected = (Quest) questDropdown.getSelectedItem();
            if (selected != null) {
                bestMatch = selected;
            }
        });

        questDropdown.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submitQuest");
        questDropdown.getActionMap().put("submitQuest", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submit();
            }
        });

        // Add components to layout
        JPanel container = new JPanel(new BorderLayout(5, 5));
        container.add(questDropdown, BorderLayout.CENTER);
        getInputRow().add(container, BorderLayout.CENTER);
    }

    @Override
    protected void submit()
    {
        if (bestMatch != null) {
            QuestTask mainQuestTask = QuestTask.builder()
                .quest(bestMatch)
                .indentLevel(0)
                .build();

            addTask(mainQuestTask);
        }
    }

    @Override
    protected void reset()
    {
        questDropdown.setSelectedIndex(-1);
        bestMatch = null;
    }
}