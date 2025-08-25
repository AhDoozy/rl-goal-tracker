package com.toofifty.goaltracker.ui;

import com.toofifty.goaltracker.GoalTrackerPlugin;
import com.toofifty.goaltracker.models.ActionHistory;
import com.toofifty.goaltracker.models.Goal;
import com.toofifty.goaltracker.models.ToggleCompleteAction;
import com.toofifty.goaltracker.models.enums.Status;
import com.toofifty.goaltracker.models.task.ManualTask;
import com.toofifty.goaltracker.models.task.Task;
import com.toofifty.goaltracker.services.TaskIconService;
import com.toofifty.goaltracker.ui.components.ListItemPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;

import static com.toofifty.goaltracker.utils.Constants.STATUS_TO_COLOR;

/**
 * UI component for rendering a single Task row inside a Goal.
 * Shows icon, title (editable for manual tasks), right-click context menu,
 * and handles refreshing on task updates.
 */
public final class TaskItemContent extends JPanel implements Refreshable
{
    private final Task task;
    private final Goal goal;
    private final TaskIconService iconService;
    private final JLabel titleLabel = new JLabel();
    private final JTextField titleEdit = new JTextField();
    private final JPanel titleStack = new JPanel(new CardLayout());
    private final JLabel iconLabel = new JLabel();
    private JPanel iconWrapper;
    private boolean titleEditable;

    private final GoalTrackerPlugin plugin;
    private ActionHistory actionHistory;

    TaskItemContent(GoalTrackerPlugin plugin, Goal goal, Task task)
    {
        super(new BorderLayout());
        this.plugin = plugin;
        this.task = task;
        this.goal = goal;
        iconService = plugin.getTaskIconService();

        titleLabel.setPreferredSize(new Dimension(0, 24));
        titleLabel.setBorder(null);
        titleLabel.setOpaque(false);
        titleEdit.setBorder(null);
        titleEdit.setOpaque(false);
        titleEdit.setDragEnabled(true);

        titleStack.setOpaque(false);
        titleStack.add(titleLabel, "label");
        titleStack.add(titleEdit, "edit");
        add(titleStack, BorderLayout.CENTER);

        iconWrapper = new JPanel(new BorderLayout());
        iconWrapper.setBorder(new EmptyBorder(4, 0, 0, 4));
        iconWrapper.add(iconLabel, BorderLayout.NORTH);
        add(iconWrapper, BorderLayout.WEST);

        plugin.getUiStatusManager().addRefresher(task, this::refresh);

        this.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { updateTitleLabel(); }
        });

        titleEditable = (task instanceof ManualTask);
        if (titleEditable) {
            titleLabel.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { enterEdit(); }
            });
            titleEdit.addActionListener(e -> exitEdit(true));
            titleEdit.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusLost(java.awt.event.FocusEvent e) { exitEdit(true); }
            });
        }

        // Right-click to toggle completion with ActionHistory
        MouseAdapter contextMenuListener = new MouseAdapter()
        {
            private void showMenuIfNeeded(MouseEvent e)
            {
                if (!(e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)))
                {
                    return;
                }
                // Prefer the parent ListItemPanel context menu (move up/down/remove, etc.)
                Component src = (Component) e.getSource();
                JComponent listItem = (JComponent) SwingUtilities.getAncestorOfClass(ListItemPanel.class, src);
                if (listItem != null && listItem.getComponentPopupMenu() != null)
                {
                    Point p = SwingUtilities.convertPoint(src, e.getPoint(), listItem);
                    listItem.getComponentPopupMenu().show(listItem, p.x, p.y);
                    return;
                }

                // Fallback: show simple toggle menu if no parent popup menu is available
                boolean currentlyComplete = task.getStatus() == Status.COMPLETED;
                String label = currentlyComplete ? "Mark as Incomplete" : "Mark as Completed";

                JPopupMenu menu = new JPopupMenu();
                JMenuItem toggle = new JMenuItem(label);
                toggle.addActionListener(a -> {
                    ToggleCompleteAction act = new ToggleCompleteAction(task, currentlyComplete, !currentlyComplete);
                    act.redo();
                    if (actionHistory != null)
                    {
                        actionHistory.push(act);
                    }
                    plugin.getUiStatusManager().refresh(goal);
                });
                menu.add(toggle);
                Component invoker = (Component) e.getSource();
                menu.show(invoker, e.getX(), e.getY());
            }

            @Override public void mousePressed(MouseEvent e) { showMenuIfNeeded(e); }
            @Override public void mouseReleased(MouseEvent e) { showMenuIfNeeded(e); }
        };

        // Attach listener to multiple components to make right-click reliable across platforms
        this.addMouseListener(contextMenuListener);
        titleStack.addMouseListener(contextMenuListener);
        titleLabel.addMouseListener(contextMenuListener);
        titleEdit.addMouseListener(contextMenuListener);
        iconLabel.addMouseListener(contextMenuListener);
    }

    public void setActionHistory(ActionHistory history)
    {
        this.actionHistory = history;
    }

    @Override
    public void refresh()
    {
        titleLabel.setForeground(STATUS_TO_COLOR.get(task.getStatus()));
        updateTitleLabel();

        int indent = 16 * task.getIndentLevel();
        iconLabel.setIcon(iconService.get(task));
        iconLabel.setBorder(new EmptyBorder(0, indent, 0, 0));

        revalidate();
    }

    @Override
    public void setBackground(Color bg)
    {
        super.setBackground(bg);
        for (Component component : getComponents()) {
            component.setBackground(bg);
        }
    }

    private void updateTitleLabel()
    {
        String full = task.toString();
        titleLabel.setToolTipText((full == null || full.isEmpty()) ? null : full);
        if (getWidth() <= 0) { titleLabel.setText(full); return; }

        int insets = 0;
        if (getBorder() != null) {
            Insets ins = getBorder().getBorderInsets(this);
            insets = (ins.left + ins.right);
        }
        int iconW = iconWrapper != null ? iconWrapper.getPreferredSize().width : 0;
        int gap = 8;
        int avail = Math.max(16, getWidth() - insets - iconW - gap);

        FontMetrics fm = titleLabel.getFontMetrics(titleLabel.getFont());
        if (full == null) full = "";
        if (fm.stringWidth(full) <= avail) { titleLabel.setText(full); return; }

        String ellipsis = "…";
        int lo = 0, hi = full.length();
        int cut = hi;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            String candidate = full.substring(0, Math.max(0, mid)) + ellipsis;
            if (fm.stringWidth(candidate) <= avail) { cut = mid; lo = mid + 1; }
            else { hi = mid - 1; }
        }
        titleLabel.setText(full.substring(0, Math.max(0, cut)) + ellipsis);
    }

    private void enterEdit()
    {
        if (!titleEditable) return;
        titleEdit.setText(task.toString());
        ((CardLayout) titleStack.getLayout()).show(titleStack, "edit");
        titleEdit.requestFocusInWindow();
        titleEdit.selectAll();
    }

    private void exitEdit(boolean save)
    {
        if (!titleEditable) { ((CardLayout) titleStack.getLayout()).show(titleStack, "label"); return; }
        if (save) {
            String newText = titleEdit.getText();
            if (newText != null && task instanceof ManualTask) {
                ((ManualTask) task).setDescription(newText);
            }
        }
        ((CardLayout) titleStack.getLayout()).show(titleStack, "label");
        updateTitleLabel();
        plugin.getUiStatusManager().refresh(goal);
    }

    public Task getTask()
    {
        return task;
    }

    /**
     * Try to invoke the same context menu action as right-click -> "Add prerequisites".
     * @return true if the action was found and invoked, false otherwise
     */
    public boolean addPrereqsFromContext()
    {
        JComponent listItem = (JComponent) SwingUtilities.getAncestorOfClass(ListItemPanel.class, this);
        if (listItem == null || listItem.getComponentPopupMenu() == null)
        {
            return false;
        }

        JPopupMenu menu = listItem.getComponentPopupMenu();
        // Accept several label variants (case-insensitive)
        String[] targets = new String[] {
                "add prerequisites", "add pre-reqs", "add prereqs", "prerequisites"
        };

        return clickMenuItemByLabels(menu.getSubElements(), targets);
    }

    // Recursively search menu/submenus for a matching label and click it.
    private static boolean clickMenuItemByLabels(MenuElement[] items, String[] needles)
    {
        for (MenuElement me : items)
        {
            if (me instanceof JMenuItem)
            {
                JMenuItem it = (JMenuItem) me;
                String txt = it.getText();
                if (txt != null)
                {
                    String lower = txt.toLowerCase(Locale.ROOT).trim();
                    for (String needle : needles)
                    {
                        if (lower.contains(needle))
                        {
                            it.doClick();
                            return true;
                        }
                    }
                }
            }
            // Recurse into submenus and containers
            MenuElement[] children = me.getSubElements();
            if (children != null && children.length > 0 && clickMenuItemByLabels(children, needles))
            {
                return true;
            }
        }
        return false;
    }
}
