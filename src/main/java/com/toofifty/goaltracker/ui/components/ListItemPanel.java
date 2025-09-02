package com.toofifty.goaltracker.ui.components;

import com.toofifty.goaltracker.models.Goal;
import com.toofifty.goaltracker.ui.Refreshable;
import com.toofifty.goaltracker.utils.ReorderableList;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.Border;
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
    private static final int BASE_TOP = 8, BASE_LEFT = 6, BASE_BOTTOM = 8, BASE_RIGHT = 6;
    private static final int TAN_THICKNESS = 1;
    private static final int INNER_TOP = 4, INNER_LEFT = 6, INNER_BOTTOM = 4, INNER_RIGHT = 6;

    private static final Border UNPINNED_BORDER = new EmptyBorder(BASE_TOP, BASE_LEFT, BASE_BOTTOM, BASE_RIGHT);
    private static final Border PINNED_BORDER = new MatteBorder(TAN_THICKNESS, TAN_THICKNESS, TAN_THICKNESS, TAN_THICKNESS, new Color(210, 180, 140));

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
            if (e.isPopupTrigger())
            {
                popupMenu.show((Component) e.getSource(), e.getX(), e.getY());
            }
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
        cardBody.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
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
        cardBody.setComponentPopupMenu(popupMenu);
        setComponentPopupMenu(popupMenu);
        // Also show context menu on press/release to handle platform differences
        // this.addMouseListener(contextMenuListener); // Removed to avoid duplicate popup invocations
        // Ensure popup and click listeners cover all descendants initially
        // addContextMenuListenerRecursive(this); // Removed to avoid multiple menus

        setOpaque(true);

        // Attach context-menu and click listeners automatically to any components added later inside the card
        cardBody.addContainerListener(new java.awt.event.ContainerAdapter() {
            @Override
            public void componentAdded(java.awt.event.ContainerEvent e) {
                java.awt.Component child = e.getChild();
                if (child instanceof JComponent) {
                    ((JComponent) child).setComponentPopupMenu(popupMenu);
                }
                if (clickListenerAdapter != null) {
                    addClickListenerRecursive(child);
                }
            }
        });
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
                ((JComponent) comp).setComponentPopupMenu(popupMenu);
            }
            // addContextMenuListenerRecursive(comp);
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
                ((JComponent) comp).setComponentPopupMenu(popupMenu);
            }
            // addContextMenuListenerRecursive(comp);
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
        if (comp instanceof JComponent) {
            ((JComponent) comp).setComponentPopupMenu(popupMenu);
        }
        // addContextMenuListenerRecursive(comp);
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

    private void applyBaseBorderWithOptionalTan()
    {
        // Outer panel: keep spacing constant
        setBorder(UNPINNED_BORDER);

        if (cardBody == null)
        {
            return;
        }

        // Ensure the inner content always has consistent padding
        cardBody.setBorder(new EmptyBorder(INNER_TOP, INNER_LEFT, INNER_BOTTOM, INNER_RIGHT));

        if (item instanceof Goal && ((Goal) item).isPinned())
        {
            // Apply tan to the OUTER card border; keep total insets stable
            int top = Math.max(0, BASE_TOP - TAN_THICKNESS);
            int left = Math.max(0, BASE_LEFT - TAN_THICKNESS);
            int bottom = Math.max(0, BASE_BOTTOM - TAN_THICKNESS);
            int right = Math.max(0, BASE_RIGHT - TAN_THICKNESS);
            Border inner = new EmptyBorder(top, left, bottom, right);
            Color tan = new Color(210, 180, 140);
            setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(TAN_THICKNESS, TAN_THICKNESS, TAN_THICKNESS, TAN_THICKNESS, tan),
                inner
            ));
        }
        // else: leave UNPINNED_BORDER already applied above
    }

    private void applyGoalCardDefaultStyle()
    {
        // Borders
        applyBaseBorderWithOptionalTan();

        // Backgrounds
        setBackground(ColorScheme.DARK_GRAY_COLOR); // keep outer area stable
        if (cardBody != null)
        {
            cardBody.setBackground(ColorScheme.DARK_GRAY_COLOR);
        }
    }

    private void applyGoalCardHoverStyle()
    {
        // Keep borders as set by applyBaseBorderWithOptionalTan();
        applyBaseBorderWithOptionalTan();
        if (cardBody != null)
        {
            cardBody.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
        }
    }

    private void applyGoalCardPressedStyle()
    {
        // Keep borders as set by applyBaseBorderWithOptionalTan();
        applyBaseBorderWithOptionalTan();
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

        // Deduplicate Pin/Unpin items if multiple sources added them
        for (int i = popupMenu.getComponentCount() - 1, seenPin = 0, seenUnpin = 0; i >= 0; i--)
        {
            java.awt.Component c = popupMenu.getComponent(i);
            if (c instanceof javax.swing.JMenuItem)
            {
                String text = ((javax.swing.JMenuItem) c).getText();
                if ("Pin goal".equals(text))
                {
                    if (seenPin++ > 0) popupMenu.remove(i);
                }
                else if ("Unpin goal".equals(text))
                {
                    if (seenUnpin++ > 0) popupMenu.remove(i);
                }
            }
        }

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
