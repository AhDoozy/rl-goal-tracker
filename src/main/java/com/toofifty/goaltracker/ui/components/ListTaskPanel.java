package com.toofifty.goaltracker.ui.components;

import com.toofifty.goaltracker.models.ActionHistory;
import com.toofifty.goaltracker.models.ReorderTaskAction;
import com.toofifty.goaltracker.models.enums.Status;
import com.toofifty.goaltracker.models.task.QuestTask;
import com.toofifty.goaltracker.models.task.Task;
import com.toofifty.goaltracker.ui.TaskItemContent;
import com.toofifty.goaltracker.utils.QuestRequirements;
import com.toofifty.goaltracker.utils.ReorderableList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Task row panel with context menu (move, indent, toggle),
 * cascading child handling, and shift-click remove support.
 */
public final class ListTaskPanel extends ListItemPanel<Task>
{
    private TaskItemContent taskContent;

    private final JMenuItem indentItem = new JMenuItem("Indent");
    private final JMenuItem unindentItem = new JMenuItem("Unindent");

    private Consumer<Task> indentedListener;
    private Consumer<Task> unindentedListener;

    private ActionHistory history;

    private final MouseAdapter shiftClickRemoveListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.isShiftDown() && SwingUtilities.isLeftMouseButton(e)) {
                removeItem.doClick();
            }
        }
    };

    private void addShiftRemoveListenerRecursive(Component c)
    {
        c.addMouseListener(shiftClickRemoveListener);
        if (c instanceof Container)
        {
            for (Component child : ((Container) c).getComponents())
            {
                addShiftRemoveListenerRecursive(child);
            }
        }
    }

    public ListTaskPanel(ReorderableList<Task> list, Task item)
    {
        super(list, item);

        indentItem.addActionListener(e -> {
            int index = list.indexOf(item);
            int baseIndent = item.getIndentLevel();

            // Collect affected tasks (item + descendants until sibling/parent)
            java.util.List<Task> affected = new java.util.ArrayList<>();
            java.util.List<Integer> oldIndents = new java.util.ArrayList<>();

            affected.add(item);
            oldIndents.add(baseIndent);
            for (int i = index + 1; i < list.size(); i++) {
                var child = list.get(i);
                if (child.getIndentLevel() <= baseIndent) break;
                affected.add(child);
                oldIndents.add(child.getIndentLevel());
            }

            ActionHistory.Action act = new ActionHistory.Action() {
                @Override public void undo() {
                    for (int i = 0; i < affected.size(); i++) {
                        affected.get(i).setIndentLevel(oldIndents.get(i));
                    }
                }
                @Override public void redo() {
                    for (int i = 0; i < affected.size(); i++) {
                        affected.get(i).setIndentLevel(oldIndents.get(i) + 1);
                    }
                }
            };

            if (history != null) {
                act.redo();
                history.push(act);
            } else {
                // Fallback: apply directly
                for (int i = 0; i < affected.size(); i++) {
                    affected.get(i).setIndentLevel(oldIndents.get(i) + 1);
                }
            }

            if (this.indentedListener != null) this.indentedListener.accept(item);
            refreshParentList();
        });

        unindentItem.addActionListener(e -> {
            int index = list.indexOf(item);
            int baseIndent = item.getIndentLevel();

            // Collect affected tasks (item + descendants until sibling/parent)
            java.util.List<Task> affected = new java.util.ArrayList<>();
            java.util.List<Integer> oldIndents = new java.util.ArrayList<>();

            affected.add(item);
            oldIndents.add(baseIndent);
            for (int i = index + 1; i < list.size(); i++) {
                var child = list.get(i);
                if (child.getIndentLevel() <= baseIndent) break;
                affected.add(child);
                oldIndents.add(child.getIndentLevel());
            }

            ActionHistory.Action act = new ActionHistory.Action() {
                @Override public void undo() {
                    for (int i = 0; i < affected.size(); i++) {
                        affected.get(i).setIndentLevel(oldIndents.get(i));
                    }
                }
                @Override public void redo() {
                    for (int i = 0; i < affected.size(); i++) {
                        affected.get(i).setIndentLevel(Math.max(0, oldIndents.get(i) - 1));
                    }
                }
            };

            if (history != null) {
                act.redo();
                history.push(act);
            } else {
                for (int i = 0; i < affected.size(); i++) {
                    affected.get(i).setIndentLevel(Math.max(0, oldIndents.get(i) - 1));
                }
            }

            if (this.unindentedListener != null) this.unindentedListener.accept(item);
            refreshParentList();
        });
        // Allow shift-click to remove this item and all its indented children
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isShiftDown() && e.getButton() == MouseEvent.BUTTON1) {
                    // Delegate to the Remove action (which cascades children and notifies listeners)
                    removeItem.doClick();
                }
            }
        });
        // Apply the same shift-click removal listener to all nested components
        addShiftRemoveListenerRecursive(this);
    }

    public void refreshMenu()
    {
        super.refresh();
        popupMenu.removeAll();
        javax.swing.JMenu moveMenu = new javax.swing.JMenu("Move");
        boolean hasMove = false;
        if (!list.isFirst(item)) {
            for (var l : moveUp.getActionListeners()) moveUp.removeActionListener(l);
            moveUp.addActionListener(e -> {
                int oldIndex = list.indexOf(item);
                int newIndex = Math.max(0, oldIndex - 1);
                list.moveUp(item);
                if (history != null) {
                    history.push(new ReorderTaskAction(list, item, oldIndex, newIndex));
                }
                refreshParentList();
            });
            moveMenu.add(moveUp);
            hasMove = true;
        }
        if (!list.isLast(item)) {
            for (var l : moveDown.getActionListeners()) moveDown.removeActionListener(l);
            moveDown.addActionListener(e -> {
                int oldIndex = list.indexOf(item);
                int newIndex = Math.min(list.size() - 1, oldIndex + 1);
                list.moveDown(item);
                if (history != null) {
                    history.push(new ReorderTaskAction(list, item, oldIndex, newIndex));
                }
                refreshParentList();
            });
            moveMenu.add(moveDown);
            hasMove = true;
        }
        if (!list.isFirst(item)) {
            for (var l : moveToTop.getActionListeners()) moveToTop.removeActionListener(l);
            moveToTop.addActionListener(e -> {
                int oldIndex = list.indexOf(item);
                list.moveToTop(item);
                if (history != null) {
                    history.push(new ReorderTaskAction(list, item, oldIndex, 0));
                }
                refreshParentList();
            });
            moveMenu.add(moveToTop);
            hasMove = true;
        }
        if (!list.isLast(item)) {
            for (var l : moveToBottom.getActionListeners()) moveToBottom.removeActionListener(l);
            moveToBottom.addActionListener(e -> {
                int oldIndex = list.indexOf(item);
                list.moveToBottom(item);
                if (history != null) {
                    history.push(new ReorderTaskAction(list, item, oldIndex, list.size() - 1));
                }
                refreshParentList();
            });
            moveMenu.add(moveToBottom);
            hasMove = true;
        }
        if (hasMove) {
            popupMenu.add(moveMenu);
        }

        var previousItem = list.getPreviousItem(item);

        if (item.isNotFullyIndented() && previousItem != null && previousItem.getIndentLevel() >= item.getIndentLevel()) {
            popupMenu.add(indentItem);
        }

        if (item.isIndented()) {
            popupMenu.add(unindentItem);
        }

        String toggleLabel = "Mark as " + (item.getStatus() == Status.COMPLETED ? "Incomplete" : "Completed");
        JMenuItem toggleStatusItem = new JMenuItem(toggleLabel);
        toggleStatusItem.addActionListener(e -> {
            // Determine new status for the root item
            Status newStatus = (item.getStatus() == Status.COMPLETED ? Status.NOT_STARTED : Status.COMPLETED);

            // Collect affected tasks (item + descendants until sibling/parent) and their old statuses
            java.util.List<Task> affected = new java.util.ArrayList<>();
            java.util.List<Status> oldStatuses = new java.util.ArrayList<>();

            int index = list.indexOf(item);
            int baseIndent = item.getIndentLevel();
            affected.add(item);
            oldStatuses.add(item.getStatus());
            for (int i = index + 1; i < list.size(); i++) {
                Task child = list.get(i);
                if (child.getIndentLevel() <= baseIndent) break;
                affected.add(child);
                oldStatuses.add(child.getStatus());
            }

            ActionHistory.Action act = new ActionHistory.Action() {
                @Override public void undo() {
                    for (int i = 0; i < affected.size(); i++) {
                        affected.get(i).setStatus(oldStatuses.get(i));
                    }
                }
                @Override public void redo() {
                    for (Task t : affected) {
                        t.setStatus(newStatus);
                    }
                }
            };

            if (history != null) {
                act.redo();          // apply change now
                history.push(act);   // record for undo/redo
            } else {
                // Fallback if no history injected
                for (Task t : affected) {
                    t.setStatus(newStatus);
                }
            }

            if (taskContent != null) {
                taskContent.refresh();
            }
            refreshParentList();
        });
        popupMenu.add(toggleStatusItem);

        // Add quest pre-reqs menu item only if the quest actually has prereqs
        if (item instanceof QuestTask) {
            QuestTask questTask = (QuestTask) item;
            int baseIndent = item.getIndentLevel();
            // Gather existing direct/descendant children under this quest to avoid duplicates
            java.util.Set<String> existingKeys = new java.util.HashSet<>();
            int parentIndex = list.indexOf(item);
            for (int i = parentIndex + 1; i < list.size(); i++) {
                var child = list.get(i);
                if (child.getIndentLevel() <= baseIndent) {
                    break; // stop at siblings/parents
                }
                existingKeys.add(child.getClass().getName() + "|" + child.toString());
            }
            var rawPrereqs = QuestRequirements.getRequirements(questTask.getQuest(), baseIndent + 1);
            java.util.List<Task> missingPrereqs = new java.util.ArrayList<>();
            if (rawPrereqs != null) {
                for (Task p : rawPrereqs) {
                    String key = p.getClass().getName() + "|" + p.toString();
                    if (!existingKeys.contains(key)) {
                        missingPrereqs.add(p);
                    }
                }
            }
            if (!missingPrereqs.isEmpty()) {
                JMenuItem prereqItem = new JMenuItem("Add pre-reqs");
                prereqItem.addActionListener(e -> {
                    // Recompute and filter again at click time
                    var raw = QuestRequirements.getRequirements(questTask.getQuest(), baseIndent + 1);
                    if (raw != null) {
                        // Refresh existing keys in case the list changed since menu was built
                        java.util.Set<String> currentKeys = new java.util.HashSet<>();
                        int pIndex = list.indexOf(item);
                        for (int i = pIndex + 1; i < list.size(); i++) {
                            var child = list.get(i);
                            if (child.getIndentLevel() <= baseIndent) break;
                            currentKeys.add(child.getClass().getName() + "|" + child.toString());
                        }
                        java.util.List<Task> filtered = new java.util.ArrayList<>();
                        for (Task t : raw) {
                            String key = t.getClass().getName() + "|" + t.toString();
                            if (!currentKeys.contains(key)) {
                                filtered.add(t);
                            }
                        }
                        if (!filtered.isEmpty()) {
                            int index = list.indexOf(item);
                            for (Task prereq : filtered) {
                                list.add(index + 1, prereq);
                                index++;
                            }
                            refreshParentList();
                        }
                    }
                });
                popupMenu.add(prereqItem);
            }
        }

        // Make the Remove action also delete all indented children of this item
        for (var l : removeItem.getActionListeners()) {
            removeItem.removeActionListener(l);
        }
        removeItem.addActionListener(e -> {
            int removedIndex = list.indexOf(item);
            int baseIndent = item.getIndentLevel();
            // Remove all children more indented than this item
            while (removedIndex + 1 < list.size() && list.get(removedIndex + 1).getIndentLevel() > baseIndent) {
                list.remove(list.get(removedIndex + 1));
            }
            // Remove the item itself
            list.remove(item);
            // Notify listeners for Undo/Redo support
            if (this.removedWithIndexListener != null) this.removedWithIndexListener.accept(item, removedIndex);
            if (this.removedListener != null) this.removedListener.accept(item);
            // Refresh UI
            refreshParentList();
        });

        removeItem.setText("<html>Remove <span style='font-size: smaller; color: gray;'>(Shift+Left Click)</span></html>");
        popupMenu.add(removeItem);
    }

    public void onIndented(Consumer<Task> indentedListener) {
        this.indentedListener = indentedListener;
    }

    public void onUnindented(Consumer<Task> unindentedListener) {
        this.unindentedListener = unindentedListener;
    }

    public void setTaskContent(TaskItemContent taskContent) {
        this.taskContent = taskContent;
        if (taskContent != null) {
            addShiftRemoveListenerRecursive(taskContent);
        }
    }

    public void setActionHistory(ActionHistory history) {
        this.history = history;
    }

    /**
     * Appends Task-specific context menu items.
     */
    @Override
    protected void buildAdditionalMenu()
    {
        javax.swing.JMenu moveMenu = new javax.swing.JMenu("Move");
        boolean hasMove = false;
        // Rewire generic move actions to include ActionHistory entries
        if (!list.isFirst(item)) {
            for (var l : moveUp.getActionListeners()) moveUp.removeActionListener(l);
            moveUp.addActionListener(e -> {
                int oldIndex = list.indexOf(item);
                int newIndex = Math.max(0, oldIndex - 1);
                list.moveUp(item);
                if (history != null) {
                    history.push(new ReorderTaskAction(list, item, oldIndex, newIndex));
                }
                refreshParentList();
            });
            moveMenu.add(moveUp);
            hasMove = true;
        }
        if (!list.isLast(item)) {
            for (var l : moveDown.getActionListeners()) moveDown.removeActionListener(l);
            moveDown.addActionListener(e -> {
                int oldIndex = list.indexOf(item);
                int newIndex = Math.min(list.size() - 1, oldIndex + 1);
                list.moveDown(item);
                if (history != null) {
                    history.push(new ReorderTaskAction(list, item, oldIndex, newIndex));
                }
                refreshParentList();
            });
            moveMenu.add(moveDown);
            hasMove = true;
        }
        if (!list.isFirst(item)) {
            for (var l : moveToTop.getActionListeners()) moveToTop.removeActionListener(l);
            moveToTop.addActionListener(e -> {
                int oldIndex = list.indexOf(item);
                list.moveToTop(item);
                if (history != null) {
                    history.push(new ReorderTaskAction(list, item, oldIndex, 0));
                }
                refreshParentList();
            });
            moveMenu.add(moveToTop);
            hasMove = true;
        }
        if (!list.isLast(item)) {
            for (var l : moveToBottom.getActionListeners()) moveToBottom.removeActionListener(l);
            moveToBottom.addActionListener(e -> {
                int oldIndex = list.indexOf(item);
                list.moveToBottom(item);
                if (history != null) {
                    history.push(new ReorderTaskAction(list, item, oldIndex, list.size() - 1));
                }
                refreshParentList();
            });
            moveMenu.add(moveToBottom);
            hasMove = true;
        }
        if (hasMove) {
            popupMenu.add(moveMenu);
        }

        // Indent / Unindent
        var previousItem = list.getPreviousItem(item);
        if (item.isNotFullyIndented() && previousItem != null && previousItem.getIndentLevel() >= item.getIndentLevel()) {
            popupMenu.add(indentItem);
        }
        if (item.isIndented()) {
            popupMenu.add(unindentItem);
        }

        // Toggle Completed/Incomplete for this task and its descendants
        String toggleLabel = "Mark as " + (item.getStatus() == Status.COMPLETED ? "Incomplete" : "Completed");
        JMenuItem toggleStatusItem = new JMenuItem(toggleLabel);
        toggleStatusItem.addActionListener(e -> {
            Status newStatus = (item.getStatus() == Status.COMPLETED ? Status.NOT_STARTED : Status.COMPLETED);
            java.util.List<Task> affected = new java.util.ArrayList<>();
            java.util.List<Status> oldStatuses = new java.util.ArrayList<>();
            int index = list.indexOf(item);
            int baseIndent = item.getIndentLevel();
            affected.add(item);
            oldStatuses.add(item.getStatus());
            for (int i = index + 1; i < list.size(); i++) {
                Task child = list.get(i);
                if (child.getIndentLevel() <= baseIndent) break;
                affected.add(child);
                oldStatuses.add(child.getStatus());
            }
            ActionHistory.Action act = new ActionHistory.Action() {
                @Override public void undo() { for (int i = 0; i < affected.size(); i++) affected.get(i).setStatus(oldStatuses.get(i)); }
                @Override public void redo() { for (Task t : affected) t.setStatus(newStatus); }
            };
            if (history != null) { act.redo(); history.push(act); }
            else { for (Task t : affected) t.setStatus(newStatus); }
            if (taskContent != null) taskContent.refresh();
            refreshParentList();
        });
        popupMenu.add(toggleStatusItem);

        // Quest pre-reqs (if available)
        if (item instanceof QuestTask) {
            QuestTask questTask = (QuestTask) item;
            int baseIndent = item.getIndentLevel();
            java.util.Set<String> existingKeys = new java.util.HashSet<>();
            int parentIndex = list.indexOf(item);
            for (int i = parentIndex + 1; i < list.size(); i++) {
                var child = list.get(i);
                if (child.getIndentLevel() <= baseIndent) break;
                existingKeys.add(child.getClass().getName() + "|" + child.toString());
            }
            var rawPrereqs = QuestRequirements.getRequirements(questTask.getQuest(), baseIndent + 1);
            java.util.List<Task> missingPrereqs = new java.util.ArrayList<>();
            if (rawPrereqs != null) {
                for (Task p : rawPrereqs) {
                    String key = p.getClass().getName() + "|" + p.toString();
                    if (!existingKeys.contains(key)) missingPrereqs.add(p);
                }
            }
            if (!missingPrereqs.isEmpty()) {
                JMenuItem prereqItem = new JMenuItem("Add pre-reqs");
                prereqItem.addActionListener(e -> {
                    var raw = QuestRequirements.getRequirements(questTask.getQuest(), baseIndent + 1);
                    if (raw != null) {
                        java.util.Set<String> currentKeys = new java.util.HashSet<>();
                        int pIndex = list.indexOf(item);
                        for (int i = pIndex + 1; i < list.size(); i++) {
                            var child = list.get(i);
                            if (child.getIndentLevel() <= baseIndent) break;
                            currentKeys.add(child.getClass().getName() + "|" + child.toString());
                        }
                        java.util.List<Task> filtered = new java.util.ArrayList<>();
                        for (Task t : raw) {
                            String key = t.getClass().getName() + "|" + t.toString();
                            if (!currentKeys.contains(key)) filtered.add(t);
                        }
                        if (!filtered.isEmpty()) {
                            int insertIndex = list.indexOf(item);
                            for (Task prereq : filtered) {
                                list.add(insertIndex + 1, prereq);
                                insertIndex++;
                            }
                            refreshParentList();
                        }
                    }
                });
                popupMenu.add(prereqItem);
            }
        }

        // Rewire Remove to cascade children and update label
        for (var l : removeItem.getActionListeners()) removeItem.removeActionListener(l);
        removeItem.addActionListener(e -> {
            int removedIndex = list.indexOf(item);
            int baseIndent = item.getIndentLevel();
            while (removedIndex + 1 < list.size() && list.get(removedIndex + 1).getIndentLevel() > baseIndent) {
                list.remove(list.get(removedIndex + 1));
            }
            list.remove(item);
            if (this.removedWithIndexListener != null) this.removedWithIndexListener.accept(item, removedIndex);
            if (this.removedListener != null) this.removedListener.accept(item);
            refreshParentList();
        });
        removeItem.setText("<html>Remove <span style='font-size: smaller; color: gray;'>(Shift+Left Click)</span></html>");
        // Ensure remove is at the bottom: remove and re-add it
        popupMenu.remove(removeItem);
        popupMenu.add(removeItem);
    }

    private void refreshParentList()
    {
        Container parent = SwingUtilities.getAncestorOfClass(ListPanel.class, this);
        if (parent instanceof ListPanel) {
            ((ListPanel<?>) parent).tryBuildList();
            ((ListPanel<?>) parent).refresh();
        } else {
            // Fallback
            revalidate();
            repaint();
        }
    }
    /**
     * Programmatically add quest prerequisites using the same logic as the context menu action.
     * This lets callers (e.g., presets) trigger prereq insertion without simulating a right-click.
     */
    public static void addPrereqsForTask(ReorderableList<Task> list, Task item)
    {
        if (!(item instanceof QuestTask)) {
            return;
        }
        QuestTask questTask = (QuestTask) item;
        int baseIndent = item.getIndentLevel();

        // Gather existing children under this task to avoid duplicates
        java.util.Set<String> existingKeys = new java.util.HashSet<>();
        int parentIndex = list.indexOf(item);
        for (int i = parentIndex + 1; i < list.size(); i++) {
            Task child = list.get(i);
            if (child.getIndentLevel() <= baseIndent) break;
            existingKeys.add(child.getClass().getName() + "|" + child.toString());
        }

        java.util.List<Task> raw = QuestRequirements.getRequirements(questTask.getQuest(), baseIndent + 1);
        if (raw == null || raw.isEmpty()) {
            return;
        }

        // Filter out any already-present entries
        java.util.List<Task> filtered = new java.util.ArrayList<>();
        for (Task t : raw) {
            String key = t.getClass().getName() + "|" + t.toString();
            if (!existingKeys.contains(key)) {
                filtered.add(t);
            }
        }

        if (filtered.isEmpty()) {
            return;
        }

        // Insert directly after the parent item, preserving order from QuestRequirements
        int insertIndex = list.indexOf(item);
        for (Task prereq : filtered) {
            list.add(insertIndex + 1, prereq);
            insertIndex++;
        }
    }
}
