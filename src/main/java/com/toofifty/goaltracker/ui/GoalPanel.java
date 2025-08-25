package com.toofifty.goaltracker.ui;

import com.toofifty.goaltracker.GoalTrackerPlugin;
import com.toofifty.goaltracker.models.ActionHistory;
import com.toofifty.goaltracker.models.Goal;
import com.toofifty.goaltracker.models.RemoveTaskAction;
import com.toofifty.goaltracker.models.enums.TaskType;
import com.toofifty.goaltracker.models.task.Task;
import com.toofifty.goaltracker.ui.components.*;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Panel showing a single Goal: header, action bar (undo/redo, prereqs),
 * editable description, and the task list with add-new controls.
 */
public final class GoalPanel extends JPanel implements Refreshable
{
    private final GoalTrackerPlugin plugin;
    private final Goal goal;

    private final EditableInput descriptionInput;
    private final ListPanel<Task> taskListPanel;
    private Consumer<Goal> goalUpdatedListener;
    private Consumer<Task> taskAddedListener;
    private Consumer<Task> taskUpdatedListener;

    private final ActionHistory actionHistory = new ActionHistory();
    private ActionBarButton undoButton;
    private ActionBarButton redoButton;
    private ActionBarButton prereqsButton;

    GoalPanel(GoalTrackerPlugin plugin, Goal goal, Runnable closeListener)
    {
        super();
        this.plugin = plugin;
        this.goal = goal;

        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(0, 0, 8, 0));
        add(headerPanel, BorderLayout.NORTH);

        // Back row above the action bar
        JPanel backRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        backRow.setBackground(ColorScheme.DARK_GRAY_COLOR);
        ActionBarButton backButton = new ActionBarButton("Back", closeListener::run);
        backRow.add(backButton);

        // Action bar with Add pre-reqs on the left of Undo/Redo
        ActionBar actionBar = new ActionBar();
        prereqsButton = new ActionBarButton("Add pre-reqs", this::addPrereqs);
        prereqsButton.setToolTipText("Add prerequisite quests/tasks for this goal");
        prereqsButton.setEnabled(true);

        undoButton = new ActionBarButton("Undo", this::doUndo);
        redoButton = new ActionBarButton("Redo", this::doRedo);

        actionBar.left().add(prereqsButton);
        actionBar.left().add(undoButton);
        actionBar.left().add(redoButton);

        // Stack back row above the action bar in the header's NORTH
        JPanel headerTop = new JPanel();
        headerTop.setBackground(ColorScheme.DARK_GRAY_COLOR);
        headerTop.setLayout(new BoxLayout(headerTop, BoxLayout.Y_AXIS));
        headerTop.add(backRow);
        headerTop.add(actionBar);

        headerPanel.add(headerTop, BorderLayout.NORTH);

        updateUndoRedoButtons();

        descriptionInput = new EditableInput((value) -> {
            goal.setDescription(value);
            this.goalUpdatedListener.accept(goal);
        });
        headerPanel.add(descriptionInput, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> installClipboardSupport(descriptionInput));
        descriptionInput.addContainerListener(new java.awt.event.ContainerAdapter()
        {
            @Override public void componentAdded(java.awt.event.ContainerEvent e)
            {
                SwingUtilities.invokeLater(() -> installClipboardSupport(descriptionInput));
            }
        });

        taskListPanel = new ListPanel<>(goal.getTasks(), (task) -> {
            ListTaskPanel taskPanel = new ListTaskPanel(goal.getTasks(), task);
            taskPanel.setActionHistory(actionHistory);
            TaskItemContent taskContent = new TaskItemContent(plugin, goal, task);
            taskContent.setActionHistory(actionHistory);
            taskPanel.add(taskContent);
            taskPanel.setTaskContent(taskContent);
            taskContent.refresh();
            taskPanel.setOpaque(true);
            taskPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
            taskPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 6, 0, 6, ColorScheme.DARKER_GRAY_COLOR), // darker line for contrast
                new EmptyBorder(2, 4, 2, 4)
            ));


            taskPanel.onIndented(e -> {
                this.goalUpdatedListener.accept(goal);
                plugin.getUiStatusManager().refresh(goal);
                this.refresh();
            });

            taskPanel.onUnindented(e -> {
                this.goalUpdatedListener.accept(goal);
                plugin.getUiStatusManager().refresh(goal);
                this.refresh();
            });

            taskPanel.onRemovedWithIndex((removedTask, index) -> {
                actionHistory.push(new RemoveTaskAction(goal.getTasks(), removedTask, index));
                updateUndoRedoButtons();
            });

            return taskPanel;
        });
        taskListPanel.setGap(0);
        taskListPanel.setPlaceholder("No tasks added yet");
        add(taskListPanel, BorderLayout.CENTER);

        NewTaskPanel newTaskPanel = new NewTaskPanel(plugin, goal);
        newTaskPanel.onTaskAdded(this::updateFromNewTask);
        add(newTaskPanel, BorderLayout.SOUTH);
    }

    public void updateFromNewTask(Task task)
    {
        taskListPanel.tryBuildList();
        taskListPanel.refresh();
        plugin.setValidateAll(true);
        plugin.getUiStatusManager().refresh(goal);
        revalidate();
        repaint();

        if (Objects.nonNull(this.taskAddedListener)) this.taskAddedListener.accept(task);
        if (Objects.nonNull(this.taskUpdatedListener)) this.taskUpdatedListener.accept(task);
    }

    public void refreshTaskList()
    {
        taskListPanel.tryBuildList();
        taskListPanel.refresh();
        plugin.getUiStatusManager().refresh(goal);
        revalidate();
        repaint();
    }

    @Override
    public void refresh()
    {
        descriptionInput.setValue(goal.getDescription());
        taskListPanel.refresh();
    }

    public void onGoalUpdated(Consumer<Goal> listener)
    {
        this.goalUpdatedListener = listener;
    }

    public void onTaskAdded(Consumer<Task> listener)
    {
        this.taskAddedListener = listener;

        taskListPanel.onUpdated(this.taskAddedListener);
    }

    public void onTaskUpdated(Consumer<Task> listener)
    {
        this.taskUpdatedListener = listener;

        taskListPanel.onUpdated(this.taskUpdatedListener);
    }
    private void installClipboardSupport(Component root)
    {
        JTextComponent tc = findTextComponent(root);
        if (tc == null)
        {
            return;
        }

        // Ensure transfer handler to enable clipboard operations on text property
        tc.setTransferHandler(new TransferHandler("text"));
        tc.setDragEnabled(true);

        // Context menu with Cut/Copy/Paste/Select All
        JPopupMenu menu = new JPopupMenu();
        JMenuItem cut = new JMenuItem(new DefaultEditorKit.CutAction());
        cut.setText("Cut");
        JMenuItem copy = new JMenuItem(new DefaultEditorKit.CopyAction());
        copy.setText("Copy");
        JMenuItem paste = new JMenuItem(new DefaultEditorKit.PasteAction());
        paste.setText("Paste");
        JMenuItem selectAll = new JMenuItem("Select All");
        selectAll.addActionListener(e -> tc.selectAll());
        menu.add(cut);
        menu.add(copy);
        menu.add(paste);
        menu.addSeparator();
        menu.add(selectAll);
        tc.setComponentPopupMenu(menu);

        // Keyboard shortcuts (Ctrl/Cmd + C/V/X/A)
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        InputMap im = tc.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = tc.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, mask), DefaultEditorKit.copyAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, mask), DefaultEditorKit.pasteAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, mask), DefaultEditorKit.cutAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, mask), DefaultEditorKit.selectAllAction);
        am.put(DefaultEditorKit.copyAction, new DefaultEditorKit.CopyAction());
        am.put(DefaultEditorKit.pasteAction, new DefaultEditorKit.PasteAction());
        am.put(DefaultEditorKit.cutAction, new DefaultEditorKit.CutAction());
        am.put(DefaultEditorKit.selectAllAction, new AbstractAction()
        {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { tc.selectAll(); }
        });
    }

    private JTextComponent findTextComponent(Component c)
    {
        if (c instanceof JTextComponent)
        {
            return (JTextComponent) c;
        }
        if (c instanceof Container)
        {
            for (Component child : ((Container) c).getComponents())
            {
                JTextComponent tc = findTextComponent(child);
                if (tc != null)
                {
                    return tc;
                }
            }
        }
        return null;
    }

    private void updateUndoRedoButtons()
    {
        if (undoButton != null)
        {
            undoButton.setEnabled(actionHistory.hasUndo());
        }
        if (redoButton != null)
        {
            redoButton.setEnabled(actionHistory.hasRedo());
        }
    }

    private void doUndo()
    {
        actionHistory.undo();
        refreshTaskList();
        updateUndoRedoButtons();
    }

    private void doRedo()
    {
        actionHistory.redo();
        refreshTaskList();
        updateUndoRedoButtons();
    }

    private void addPrereqs()
    {
        int processed = 0;
        int invoked = 0;

        java.util.List<TaskItemContent> contents = new java.util.ArrayList<>();
        collectTaskItemContents(taskListPanel, contents);

        for (TaskItemContent tic : contents)
        {
            Task t = tic.getTask();
            if (t != null && t.getType() == TaskType.QUEST)
            {
                processed++;
                if (tic.addPrereqsFromContext())
                {
                    invoked++;
                }
            }
        }

        // Refresh UI/state after batch
        plugin.setValidateAll(true);
        plugin.getUiStatusManager().refresh(goal);
        refreshTaskList();
        updateUndoRedoButtons();

        if (processed == 0)
        {
            JOptionPane.showMessageDialog(this,
                    "No quest tasks found in this goal.",
                    "Add pre-reqs",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (invoked == 0)
        {
            JOptionPane.showMessageDialog(this,
                    "All pre-reqs have already been added.\n",
                    "Add pre-reqs",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private static void collectTaskItemContents(Component root, java.util.List<TaskItemContent> out)
    {
        if (root instanceof TaskItemContent)
        {
            out.add((TaskItemContent) root);
        }
        if (root instanceof Container)
        {
            for (Component child : ((Container) root).getComponents())
            {
                collectTaskItemContents(child, out);
            }
        }
    }
}
