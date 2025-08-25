package com.toofifty.goaltracker.ui.components;

import com.toofifty.goaltracker.models.Goal;
import com.toofifty.goaltracker.ui.Refreshable;
import com.toofifty.goaltracker.utils.ReorderableList;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Generic panel representing a list item (Goal or Task).
 * Provides context menu actions (move, remove) and hover/press styling.
 */
public class ListItemPanel<T> extends JPanel implements Refreshable
{
    protected final JMenuItem moveUp = new JMenuItem("Move up");
    protected final JMenuItem moveDown = new JMenuItem("Move down");
    protected final JMenuItem moveToTop = new JMenuItem("Move to top");
    protected final JMenuItem moveToBottom = new JMenuItem("Move to bottom");
    protected final JMenuItem removeItem = new JMenuItem("Remove");
    protected final JPopupMenu popupMenu = new JPopupMenu();

    protected final ReorderableList<T> list;
    protected final T item;

    protected Consumer<T> reorderedListener;
    protected Consumer<T> removedListener;
    protected BiConsumer<T, Integer> removedWithIndexListener;

    private MouseAdapter clickListenerAdapter;

    // Inner face of the goal card; only this area changes color on hover/press
    private JPanel cardBody;

    private void addClickListenerRecursive(Component c)
    {
        if (clickListenerAdapter == null) return;
        c.addMouseListener(clickListenerAdapter);
        if (c instanceof java.awt.Container)
        {
            for (Component child : ((java.awt.Container) c).getComponents())
            {
                addClickListenerRecursive(child);
            }
        }
    }

    private void addContextMenuListenerRecursive(Component c)
    {
        c.addMouseListener(contextMenuListener);
        if (c instanceof java.awt.Container)
        {
            for (Component child : ((java.awt.Container) c).getComponents())
            {
                addContextMenuListenerRecursive(child);
            }
        }
    }

    private final MouseAdapter contextMenuListener = new MouseAdapter()
    {
        private void maybeShow(MouseEvent e)
        {
            if (!(e.isPopupTrigger() || javax.swing.SwingUtilities.isRightMouseButton(e)))
            {
                return;
            }
            popupMenu.show((Component) e.getSource(), e.getX(), e.getY());
        }

        @Override public void mousePressed(MouseEvent e) { maybeShow(e); }
        @Override public void mouseReleased(MouseEvent e) { maybeShow(e); }
    };

    public ListItemPanel(ReorderableList<T> list, T item)
    {
        super(new BorderLayout());
        this.list = list;
        this.item = item;

        // Create inner card surface to isolate hover/press background changes
        cardBody = new JPanel(new BorderLayout());
        cardBody.setOpaque(true);
        // Add the cardBody as the main content area
        super.add(cardBody, BorderLayout.CENTER);

        if (item instanceof Goal) {
            applyGoalCardDefaultStyle();
        } else {
            setBorder(new EmptyBorder(2, 4, 2, 4)); // add horizontal and vertical spacing for tasks
            setBackground(ColorScheme.DARK_GRAY_COLOR);
        }

        moveUp.addActionListener(e -> {
            list.moveUp(item);
            if (this.reorderedListener != null) this.reorderedListener.accept(item);
        });

        moveDown.addActionListener(e -> {
            list.moveDown(item);
            if (this.reorderedListener != null) this.reorderedListener.accept(item);
        });

        moveToTop.addActionListener(e -> {
            list.moveToTop(item);
            if (this.reorderedListener != null) this.reorderedListener.accept(item);
        });

        moveToBottom.addActionListener(e -> {
            list.moveToBottom(item);
            if (this.reorderedListener != null) this.reorderedListener.accept(item);
        });

        removeItem.addActionListener(e -> {
            int index = list.indexOf(item);
            list.remove(item);
            if (this.removedWithIndexListener != null) this.removedWithIndexListener.accept(item, index);
            if (this.removedListener != null) this.removedListener.accept(item);
        });

        popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));

        setComponentPopupMenu(popupMenu);
        // Also show context menu on press/release to handle platform differences
        this.addMouseListener(contextMenuListener);
        // Ensure popup and click listeners cover all descendants initially
        addContextMenuListenerRecursive(this);
        setOpaque(true);
    }

    @Override
    public void add(Component comp, Object constraints)
    {
        if (item instanceof Goal)
        {
            cardBody.add(comp, BorderLayout.CENTER);
            // For goal cards, strip inner borders from the content to avoid double outlines
            if (comp instanceof JComponent)
            {
                ((JComponent) comp).setBorder(new EmptyBorder(0, 0, 0, 0));
            }
            addContextMenuListenerRecursive(comp);
            if (clickListenerAdapter != null) addClickListenerRecursive(comp);
            return;
        }
        super.add(comp, constraints);
    }

    @Override
    public Component add(String name, Component comp)
    {
        if (item instanceof Goal)
        {
            cardBody.add(comp, BorderLayout.CENTER);
            if (comp instanceof JComponent)
            {
                ((JComponent) comp).setBorder(new EmptyBorder(0, 0, 0, 0));
            }
            addContextMenuListenerRecursive(comp);
            if (clickListenerAdapter != null) addClickListenerRecursive(comp);
            return comp;
        }
        return super.add(name, comp);
    }

    public ListItemPanel<T> add(Component comp)
    {
        cardBody.add(comp, BorderLayout.CENTER);
        // For goal cards, strip inner borders from the content to avoid double outlines
        if (item instanceof Goal && comp instanceof JComponent)
        {
            ((JComponent) comp).setBorder(new EmptyBorder(0, 0, 0, 0));
        }
        addContextMenuListenerRecursive(comp);
        if (clickListenerAdapter != null) addClickListenerRecursive(comp);
        return this;
    }

    public void onClick(Consumer<MouseEvent> clickListener)
    {
        clickListenerAdapter = new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON1)
                {
                    if (item instanceof Goal) {
                        applyGoalCardPressedStyle();
                    }
                    clickListener.accept(e);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                if (item instanceof Goal) {
                    applyGoalCardHoverStyle();
                }
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                if (item instanceof Goal) {
                    applyGoalCardDefaultStyle();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (item instanceof Goal) {
                    if (contains(e.getPoint())) {
                        applyGoalCardHoverStyle();
                    } else {
                        applyGoalCardDefaultStyle();
                    }
                }
            }
        };

        // Attach to this panel and all current children
        addMouseListener(clickListenerAdapter);
        addClickListenerRecursive(this);

        // Optional: use a hand cursor to indicate clickability
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    public void onRemoved(Consumer<T> removeListener) {
        this.removedListener = removeListener;
    }

    public void onReordered(Consumer<T> reorderListener) {
        this.reorderedListener = reorderListener;
    }

    public void onRemovedWithIndex(BiConsumer<T, Integer> removeListener) {
        this.removedWithIndexListener = removeListener;
    }

    private void applyGoalCardDefaultStyle()
    {
        // Outer spacing + shadow on the container panel
        setBorder(javax.swing.BorderFactory.createCompoundBorder(
            new EmptyBorder(8, 6, 8, 6),
            new MatteBorder(2, 2, 4, 4, new Color(0, 0, 0, 60)) // shadow on all sides
        ));
        setBackground(ColorScheme.DARK_GRAY_COLOR); // keep outer area stable

        // Inner card face: rounded outline + inner padding
        if (cardBody != null)
        {
            cardBody.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(ColorScheme.DARK_GRAY_COLOR, 1, true),
                new EmptyBorder(6, 8, 6, 8)
            ));
            cardBody.setBackground(ColorScheme.DARK_GRAY_COLOR);
        }
    }

    private void applyGoalCardHoverStyle()
    {
        if (cardBody != null)
        {
            cardBody.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
        }
    }

    private void applyGoalCardPressedStyle()
    {
        if (cardBody != null)
        {
            cardBody.setBackground(ColorScheme.DARK_GRAY_COLOR);
        }
    }

    @Override
    public void refresh()
    {
        // Refresh the context menu
        popupMenu.removeAll();
        if (!list.isFirst(item)) {
            popupMenu.add(moveUp);
        }
        if (!list.isLast(item)) {
            popupMenu.add(moveDown);
        }
        if (!list.isFirst(item)) {
            popupMenu.add(moveToTop);
        }
        if (!list.isLast(item)) {
            popupMenu.add(moveToBottom);
        }
        popupMenu.add(removeItem);

        buildAdditionalMenu();

        // Refresh all descendants that implement Refreshable
        for (Component component : getComponents()) {
            refreshDescendants(component);
        }
        revalidate();
        repaint();
    }

    /**
     * Hook for subclasses to append extra context‑menu items.
     * Base implementation adds Goal‑specific actions only.
     */
    protected void buildAdditionalMenu()
    {
        if (item instanceof Goal)
        {
            JMenuItem markAllComplete = new JMenuItem("Mark all as completed");
            JMenuItem markAllIncomplete = new JMenuItem("Mark all as incomplete");

            markAllComplete.addActionListener(e -> {
                Goal goal = (Goal) item;
                goal.setAllTasksCompleted(true);
                refresh();
            });

            markAllIncomplete.addActionListener(e -> {
                Goal goal = (Goal) item;
                goal.setAllTasksCompleted(false);
                refresh();
            });

            popupMenu.addSeparator();
            popupMenu.add(markAllComplete);
            popupMenu.add(markAllIncomplete);
        }
        // Non‑Goal rows (e.g., Tasks) should add their own items in subclasses like ListTaskPanel
    }

    private void refreshDescendants(Component c)
    {
        if (c instanceof Refreshable) {
            ((Refreshable) c).refresh();
        }
        if (c instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) c).getComponents()) {
                refreshDescendants(child);
            }
        }
    }
}
