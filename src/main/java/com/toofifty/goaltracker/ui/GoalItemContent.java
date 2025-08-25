package com.toofifty.goaltracker.ui;

import com.toofifty.goaltracker.GoalTrackerPlugin;
import com.toofifty.goaltracker.models.Goal;
import com.toofifty.goaltracker.ui.components.ListItemPanel;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.toofifty.goaltracker.utils.Constants.STATUS_TO_COLOR;

/**
 * UI component for rendering a Goal in a list.
 * Shows the title (editable), progress count, and a slim progress bar.
 * Also supports pinning, context menus, and refresh on goal status change.
 */
public final class GoalItemContent extends JPanel implements Refreshable
{
    private final JLabel titleLabel = new JLabel();
    private final JTextField titleEdit = new JTextField();
    private final JLabel progress = new JLabel();
    private final SlimBar progressBar = new SlimBar();
    private final JPanel titleStack = new JPanel(new CardLayout());

    private final Goal goal;

    private JPanel topRow;

    //private static final int PIN_STRIPE_W = 3; // px
    private static final Color PINNED_BG_COLOR = new Color(45, 45, 45); // darker gray background

    GoalItemContent(GoalTrackerPlugin plugin, Goal goal)
    {
        super(new BorderLayout());
        this.goal = goal;

        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8)); // padding for centered text
        // Let the parent card body paint the background; avoid double fills
        setOpaque(false);
        setBackground(null);

        topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        // Title label (display mode)
        titleLabel.setBorder(null);
        titleLabel.setOpaque(false);
        titleLabel.setFocusable(false);
        titleLabel.setToolTipText(null);

        // Title edit (edit mode)
        titleEdit.setBorder(null);
        titleEdit.setOpaque(false);
        titleEdit.setDragEnabled(true);

        titleStack.setOpaque(false);
        titleStack.add(titleLabel, "label");
        titleStack.add(titleEdit, "edit");
        topRow.add(titleStack, BorderLayout.CENTER);

        // Swap to edit on label click
        titleLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { enterEdit(); }
        });
        // Commit edits on Enter and when focus is lost
        titleEdit.addActionListener(e -> exitEdit(true));
        titleEdit.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) { exitEdit(true); }
        });

        topRow.add(progress, BorderLayout.EAST);

        // Reserve fixed width for progress like 999/999 so it never clips
        int progW = getFontMetrics(progress.getFont()).stringWidth("999/999");
        Dimension progSize = new Dimension(progW, progress.getPreferredSize().height);
        progress.setPreferredSize(progSize);
        progress.setMinimumSize(progSize);

        add(topRow, BorderLayout.CENTER);

        // Slim custom progress bar under the title row
        progressBar.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0)); // gap above the bar
        progressBar.setPreferredSize(new Dimension(0, 6)); // 6px tall bar
        add(progressBar, BorderLayout.SOUTH);

        // Initialize visible text and colors immediately (before first refresh)
        {
            Color color = STATUS_TO_COLOR.get(goal.getStatus());
            updateTitleLabel();
            titleEdit.setCaretColor(color);
            progress.setText(goal.getComplete().size() + "/" + goal.getTasks().size());
            progress.setForeground(color);
        }

        topRow.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { updateTitleLabel(); }
        });

        plugin.getUiStatusManager().addRefresher(goal, this::refresh);

        // Ensure right-click on any child shows the parent ListItemPanel context menu
        MouseAdapter forwardPopup = new MouseAdapter()
        {
            private void maybeShow(MouseEvent e)
            {
                if (!(e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)))
                {
                    return;
                }
                Component src = (Component) e.getSource();
                JComponent listItem = (JComponent) SwingUtilities.getAncestorOfClass(ListItemPanel.class, src);
                if (listItem == null)
                {
                    listItem = (JComponent) SwingUtilities.getAncestorOfClass(ListItemPanel.class, GoalItemContent.this);
                }
                if (listItem != null && listItem.getComponentPopupMenu() != null)
                {
                    Point p = SwingUtilities.convertPoint(src, e.getPoint(), listItem);
                    JPopupMenu menu = listItem.getComponentPopupMenu();

                    // --- Pin / Unpin (temporary additions) ---
                    JSeparator sep = new JSeparator();
                    ((JComponent) sep).putClientProperty("pinToggle", Boolean.TRUE);
                    menu.add(sep);

                    JMenuItem pinToggle = new JMenuItem(goal.isPinned() ? "Unpin" : "Pin");
                    ((JComponent) pinToggle).putClientProperty("pinToggle", Boolean.TRUE);
                    pinToggle.addActionListener(ev -> {
                        goal.setPinned(!goal.isPinned());
                        try {
                            plugin.getGoalManager().save();
                        } catch (Throwable t) {
                            plugin.getUiStatusManager().refresh(goal);
                        }
                        GoalItemContent.this.revalidate();
                        GoalItemContent.this.repaint();
                    });
                    menu.add(pinToggle);

                    PopupMenuListener cleanup = new PopupMenuListener() {
                        @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
                        @Override public void popupMenuCanceled(PopupMenuEvent e) { cleanup(menu, this); }
                        @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { cleanup(menu, this); }
                        private void cleanup(JPopupMenu m, PopupMenuListener self) {
                            // Remove only the components we added this time
                            java.util.List<Component> toRemove = new java.util.ArrayList<>();
                            for (Component c : m.getComponents()) {
                                if (c instanceof JComponent) {
                                    Object flag = ((JComponent) c).getClientProperty("pinToggle");
                                    if (Boolean.TRUE.equals(flag)) {
                                        toRemove.add(c);
                                    }
                                }
                            }
                            for (Component c : toRemove) {
                                m.remove(c);
                            }
                            m.removePopupMenuListener(self);
                        }
                    };
                    menu.addPopupMenuListener(cleanup);

                    menu.show(listItem, p.x, p.y);
                }
            }

            @Override public void mousePressed(MouseEvent e) { maybeShow(e); }
            @Override public void mouseReleased(MouseEvent e) { maybeShow(e); }
        };

        this.addMouseListener(forwardPopup);
        titleLabel.addMouseListener(forwardPopup);
        titleEdit.addMouseListener(forwardPopup);
        progress.addMouseListener(forwardPopup);
        progressBar.addMouseListener(forwardPopup);
        // Ensure item icon/text initialize on first render (e.g., on login)
        SwingUtilities.invokeLater(this::refresh);
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        // In case construction happened before UI was realized, refresh when shown
        SwingUtilities.invokeLater(this::refresh);
    }

    @Override
    public void refresh()
    {
        Color color = STATUS_TO_COLOR.get(goal.getStatus());

        updateTitleLabel();
        titleEdit.setCaretColor(color);

        progress.setText(
            goal.getComplete().size() + "/" + goal.getTasks().size());
        progress.setForeground(color);

        int total = goal.getTasks().size();
        int done = goal.getComplete().size();
        progressBar.setVisible(total > 0);
        progressBar.setProgress(done, total, color);
        SwingUtilities.invokeLater(this::updateTitleLabel);
    }
    private static class SlimBar extends JComponent {
        private int done = 0;
        private int total = 1;
        private Color fill = Color.GREEN;

        void setProgress(int done, int total, Color fill) {
            this.done = done;
            this.total = Math.max(1, total);
            this.fill = fill;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();
            // track
            g2.setColor(ColorScheme.DARKER_GRAY_COLOR);
            g2.fillRect(0, 0, w, h);
            // fill
            int barW = (int) ((done / (double) total) * w);
            g2.setColor(fill != null ? fill : Color.GREEN);
            g2.fillRect(0, 0, barW, h);
            g2.dispose();
        }
    }

    private void updateTitleLabel()
    {
        if (topRow == null) return;
        String full = goal.getDescription() != null ? goal.getDescription() : "";
        titleLabel.setToolTipText(full.isEmpty() ? null : full);

        // Compute available width for title (row width minus progress preferred width and a small gap)
        int rowW = topRow.getWidth();
        if (rowW <= 0) { titleLabel.setText(full); return; }
        int gap = 8;
        int avail = Math.max(16, rowW - progress.getPreferredSize().width - gap);

        FontMetrics fm = titleLabel.getFontMetrics(titleLabel.getFont());
        if (fm.stringWidth(full) <= avail) {
            titleLabel.setText(full);
            return;
        }
        String ellipsis = "…";
        String text = full;
        // Binary-like trim from the end until it fits
        int lo = 0, hi = full.length();
        int cut = hi;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            String candidate = full.substring(0, Math.max(0, mid)) + ellipsis;
            if (fm.stringWidth(candidate) <= avail) {
                cut = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        text = full.substring(0, Math.max(0, cut)) + ellipsis;
        titleLabel.setText(text);
    }

    private void enterEdit()
    {
        titleEdit.setText(goal.getDescription() != null ? goal.getDescription() : "");
        ((CardLayout) titleStack.getLayout()).show(titleStack, "edit");
        titleEdit.requestFocusInWindow();
        titleEdit.selectAll();
    }

    private void exitEdit(boolean save)
    {
        if (save) {
            String newText = titleEdit.getText();
            if (newText != null) {
                goal.setDescription(newText);
            }
        }
        ((CardLayout) titleStack.getLayout()).show(titleStack, "label");
        updateTitleLabel();
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        if (goal != null && goal.isPinned())
        {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(PINNED_BG_COLOR);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
