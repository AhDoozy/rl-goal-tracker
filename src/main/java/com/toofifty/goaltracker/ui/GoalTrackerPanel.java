package com.toofifty.goaltracker.ui;

import com.toofifty.goaltracker.presets.GoalPresetRepository.Preset;
import com.toofifty.goaltracker.GoalManager;
import com.toofifty.goaltracker.GoalTrackerPlugin;
import com.toofifty.goaltracker.models.Goal;
import com.toofifty.goaltracker.models.UndoStack;
import com.toofifty.goaltracker.models.task.Task;
import com.toofifty.goaltracker.presets.GoalPresetRepository;
import com.toofifty.goaltracker.ui.components.ActionBar;
import com.toofifty.goaltracker.ui.components.ActionBarButton;
import com.toofifty.goaltracker.ui.components.ListItemPanel;
import com.toofifty.goaltracker.ui.components.ListPanel;
import com.toofifty.goaltracker.utils.ReorderableList;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Consumer;

/**
 * Main plugin panel for Goal Tracker.
 * Shows header (title, add/import/export controls), goal list with undo/redo,
 * and switches between home view and individual goal panels.
 */
public final class GoalTrackerPanel extends PluginPanel implements Refreshable
{
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final ListPanel<Goal> goalListPanel;
    private final GoalTrackerPlugin plugin;
    private final GoalManager goalManager;
    private final UndoStack<Goal> undoStack = new UndoStack<>();
    private ActionBarButton undoButtonRef;
    private ActionBarButton redoButtonRef;
    private GoalPanel goalPanel;
    private Consumer<Goal> goalAddedListener;
    private Consumer<Goal> goalUpdatedListener;
    private Consumer<Task> taskAddedListener;
    private Consumer<Task> taskUpdatedListener;
    private Goal pendingNewGoal;

    @Inject
    public GoalTrackerPanel(GoalTrackerPlugin plugin, GoalManager goalManager)
    {
        super(false);
        this.plugin = plugin;
        this.goalManager = goalManager;
        this.goalManager.addGoalsChangedListener(() -> SwingUtilities.invokeLater(this::refreshHomeListIfVisible));

        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8, 8, 8, 8));

        // Header with title and + Add goal on right
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        titlePanel.setLayout(new BorderLayout());
        titlePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JLabel title = new JLabel("Goal Tracker");
        title.setForeground(Color.WHITE);
        title.setFont(FontManager.getRunescapeBoldFont());

        // Title row (top)
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setBackground(ColorScheme.DARK_GRAY_COLOR);
        titleRow.add(title);

        // Buttons row (bottom)
        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonsRow.setBackground(ColorScheme.DARK_GRAY_COLOR);
        ActionBarButton addGoalBtn = new ActionBarButton("+ Add goal", this::addNewGoal);
        ActionBarButton addFromPresetBtn = new ActionBarButton("Add from Preset…", this::addFromPreset);
        buttonsRow.add(addGoalBtn);
        buttonsRow.add(addFromPresetBtn);

        // Stack rows vertically
        JPanel headerTop = new JPanel();
        headerTop.setLayout(new BoxLayout(headerTop, BoxLayout.Y_AXIS));
        headerTop.setBackground(ColorScheme.DARK_GRAY_COLOR);
        headerTop.add(titleRow);
        headerTop.add(Box.createVerticalStrut(4));
        headerTop.add(buttonsRow);

        titlePanel.add(headerTop, BorderLayout.CENTER);

        // Action bar
        ActionBar actionBar = new ActionBar();
        actionBar.right().setBorder(new EmptyBorder(0, 4, 0, 0));

        undoButtonRef = new ActionBarButton("Undo", this::doUndo);
        redoButtonRef = new ActionBarButton("Redo", this::doRedo);
        actionBar.left().add(undoButtonRef);
        actionBar.left().add(redoButtonRef);

        ActionBarButton exportButton = new ActionBarButton("Export", this::exportGoalsToFile);
        ActionBarButton importButton = new ActionBarButton("Import", this::importGoalsFromFile);
        actionBar.right().add(exportButton);
        actionBar.right().add(importButton);

        updateUndoRedoButtons();

        JPanel titleWrapper = new JPanel(new BorderLayout());
        titleWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
        titleWrapper.add(titlePanel, BorderLayout.CENTER);

        JPanel headerSeparator = new JPanel();
        headerSeparator.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        headerSeparator.setPreferredSize(new Dimension(1, 4));
        titleWrapper.add(headerSeparator, BorderLayout.SOUTH);

        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
        headerContainer.add(titleWrapper, BorderLayout.NORTH);
        headerContainer.add(actionBar, BorderLayout.SOUTH);

        goalListPanel = new ListPanel<>(goalManager.getGoals(), (goal) -> {
            var panel = new ListItemPanel<>(goalManager.getGoals(), goal);
            panel.onClick(e -> this.view(goal));
            panel.add(new GoalItemContent(plugin, goal));
            panel.onRemovedWithIndex(this::recordGoalRemoval);
            return panel;
        });
        goalListPanel.setGap(0);
        goalListPanel.setPlaceholder("<html><div style='text-align:center;color:#bfbfbf;padding:8px 0;'>No goals yet.<br/>Click <b>+ Add goal</b> above to create your first one.</div></html>");

        mainPanel.add(headerContainer, BorderLayout.NORTH);
        mainPanel.add(goalListPanel, BorderLayout.CENTER);

        home();
    }

    private void refreshHomeListIfVisible()
    {
        if (goalPanel == null)
        {
            sortGoalsForHome();
            goalListPanel.tryBuildList();
            goalListPanel.refresh();
            revalidate();
            repaint();
        }
    }

    public void view(Goal goal)
    {
        removeAll();
        this.goalPanel = new GoalPanel(plugin, goal, this::home);
        this.goalPanel.onGoalUpdated(this.goalUpdatedListener);
        this.goalPanel.onTaskAdded(this.taskAddedListener);
        this.goalPanel.onTaskUpdated(this.taskUpdatedListener);
        add(this.goalPanel, BorderLayout.CENTER);
        this.goalPanel.refresh();
        revalidate();
        repaint();
    }

    public void home()
    {
        if (pendingNewGoal != null)
        {
            try {
                if (pendingNewGoal.getTasks() == null || pendingNewGoal.getTasks().isEmpty()) {
                    goalManager.getGoals().remove(pendingNewGoal);
                }
            } finally {
                pendingNewGoal = null;
            }
        }
        removeAll();
        sortGoalsForHome();
        goalListPanel.tryBuildList();
        goalListPanel.refresh();
        mainPanel.remove(goalListPanel);
        mainPanel.add(goalListPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
        revalidate();
        repaint();
        this.goalPanel = null;
    }


    @Override
    public void refresh()
    {
        for (Component component : getComponents()) {
            if (component instanceof Refreshable) {
                ((Refreshable) component).refresh();
            }
        }
        goalListPanel.refresh();
    }

    public void onGoalUpdated(Consumer<Goal> listener)
    {
        this.goalUpdatedListener = listener;
        this.goalListPanel.onUpdated(this.goalUpdatedListener);
        if (this.goalPanel != null) {
            this.goalPanel.onGoalUpdated(this.goalUpdatedListener);
        }
    }

    public void onTaskUpdated(Consumer<Task> listener)
    {
        this.taskUpdatedListener = listener;
        if (this.goalPanel != null) {
            this.goalPanel.onTaskUpdated(this.taskUpdatedListener);
        }
    }

    public void onTaskAdded(Consumer<Task> listener)
    {
        this.taskAddedListener = listener;
        if (this.goalPanel != null) {
            this.goalPanel.onTaskAdded(this.taskAddedListener);
        }
    }

    private void updateUndoRedoButtons()
    {
        if (undoButtonRef != null)
        {
            undoButtonRef.setEnabled(undoStack.hasUndo());
            undoButtonRef.setToolTipText(undoStack.hasUndo() ? null : "Nothing to undo");
        }
        if (redoButtonRef != null)
        {
            redoButtonRef.setEnabled(undoStack.hasRedo());
            redoButtonRef.setToolTipText(undoStack.hasRedo() ? null : "Nothing to redo");
        }
    }

    private void sortGoalsForHome()
    {
        java.util.List<Goal> goals = goalManager.getGoals();
        Collections.sort(goals, Comparator
                .comparing(Goal::isPinned).reversed()
                .thenComparing(g -> {
                    String d = g.getDescription();
                    return d == null ? "" : d.toLowerCase();
                }));
    }

    private void doUndo()
    {
        var entry = undoStack.popForUndo();
        if (entry == null) { updateUndoRedoButtons(); return; }
        java.util.List<Goal> goals = goalManager.getGoals();
        int idx = Math.max(0, Math.min(entry.getIndex(), goals.size()));
        goals.add(idx, entry.getItem());
        if (goalPanel == null)
        {
            goalListPanel.tryBuildList();
            goalListPanel.refresh();
            revalidate();
            repaint();
        }
        updateUndoRedoButtons();
    }

    private void doRedo()
    {
        var entry = undoStack.popForRedo();
        if (entry == null) { updateUndoRedoButtons(); return; }
        java.util.List<Goal> goals = goalManager.getGoals();
        int idx = goals.indexOf(entry.getItem());
        if (idx >= 0)
        {
            goals.remove(idx);
        }
        if (goalPanel == null)
        {
            goalListPanel.tryBuildList();
            goalListPanel.refresh();
            revalidate();
            repaint();
        }
        updateUndoRedoButtons();
    }

    public void recordGoalRemoval(Goal goal, int index)
    {
        undoStack.pushRemove(goal, index);
        updateUndoRedoButtons();
    }

    private void addNewGoal()
    {
        Goal goal = Goal.builder().tasks(ReorderableList.from()).build();
        goalManager.getGoals().add(0, goal);
        pendingNewGoal = goal;
        view(goal);
    }

    private void exportGoalsToFile()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));
        chooser.setDialogTitle("Export goals to JSON");
        chooser.setSelectedFile(new java.io.File("goals.json"));
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) { return; }
        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".json"))
        {
            file = new java.io.File(file.getParentFile(), file.getName() + ".json");
        }
        try (java.io.FileWriter fw = new java.io.FileWriter(file))
        {
            String json = goalManager.exportJson(true);
            fw.write(json);
            fw.flush();
            JOptionPane.showMessageDialog(this, "Exported " + goalManager.getGoals().size() + " goal(s) to\n" + file.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "Failed to export goals: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importGoalsFromFile()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));
        chooser.setDialogTitle("Import goals from JSON");
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) { return; }
        java.io.File file = chooser.getSelectedFile();
        try
        {
            String json = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            goalManager.importJson(json);
            plugin.warmItemIcons();
            if (goalPanel != null) {
                home();
            } else {
                goalListPanel.tryBuildList();
                goalListPanel.refresh();
                revalidate();
                repaint();
            }
            JOptionPane.showMessageDialog(this, "Imported goals from\n" + file.getAbsolutePath(), "Import Complete", JOptionPane.INFORMATION_MESSAGE);
        }
        catch (Exception ex)
        {
            JOptionPane.showMessageDialog(this, "Failed to import goals: " + ex.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addFromPreset()
    {
        java.util.List<Preset> options = GoalPresetRepository.getAll();

        JComboBox<Preset> combo = new JComboBox<>(options.toArray(new Preset[0]));
        JTextArea details = new JTextArea(6, 36);
        details.setWrapStyleWord(true);
        details.setLineWrap(true);
        details.setEditable(false);
        details.setBackground(new JPanel().getBackground());

        Consumer<Preset> update = (opt) -> {
            if (opt == null) { details.setText(""); return; }
            int goals = opt.getGoals().size();
            int tasks = opt.getGoals().stream().mapToInt(g -> g.getTasks().size()).sum();
            details.setText(opt.getDescription() + "\n\nThis will add " + goals + " goal(s) with " + tasks + " task(s).");
        };
        combo.addActionListener(e -> update.accept((Preset) combo.getSelectedItem()));
        update.accept((Preset) combo.getSelectedItem());

        JPanel panel = new JPanel(new BorderLayout(6,6));
        panel.add(new JLabel("Choose a preset:"), BorderLayout.NORTH);
        panel.add(combo, BorderLayout.CENTER);
        panel.add(new JScrollPane(details), BorderLayout.SOUTH);

        int res = JOptionPane.showConfirmDialog(this, panel, "Add from Preset…", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) { return; }

        Preset selected = (Preset) combo.getSelectedItem();
        if (selected == null) { return; }

        goalManager.addGoals(selected.getGoals());

        if (goalPanel != null) {
            home();
        } else {
            goalListPanel.tryBuildList();
            goalListPanel.refresh();
            revalidate();
            repaint();
        }
    }
}