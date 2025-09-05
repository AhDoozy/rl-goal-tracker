package com.toofifty.goaltracker.ui.components;

import com.toofifty.goaltracker.ui.Refreshable;
import com.toofifty.goaltracker.utils.ReorderableList;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Scrollable container that renders and manages a list of items with optional placeholder text.
 * Supports reordering, removal, and refresh of child panels.
 */
public final class ListPanel<T> extends JScrollPane implements Refreshable
{
    private final JPanel listPanel = new JPanel(new GridBagLayout());

    private final ReorderableList<T> reorderableList;
    private final Function<T, ListItemPanel<T>> renderItem;

    private final Map<T, ListItemPanel<T>> itemPanelMap = new HashMap<>();

    private int gap = 2;
    private JComponent placeholder = new JLabel("Nothing interesting happens.");
    private Consumer<T> updatedListener;

    private int rowLeftInset = 0;
    private int rowRightInset = 0;

    public ListPanel(
        ReorderableList<T> reorderableList,
        Function<T, ListItemPanel<T>> renderItem
    ) {
        super();
        this.reorderableList = reorderableList;
        this.renderItem = renderItem;


        listPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        // Add left/right padding so card content aligns with the header
        listPanel.setBorder(new EmptyBorder(0, 10, 0, 10));

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        wrapperPanel.add(listPanel, BorderLayout.NORTH);

        setBorder(new EmptyBorder(0, 0, 0, 0));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        getVerticalScrollBar().setBorder(new EmptyBorder(0, 4, 0, 0));

        setViewportView(wrapperPanel);

        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        tryBuildList();
    }

    public void setGap(int gap)
    {
        this.gap = gap;
        setBorder(new EmptyBorder(0, 0, 0, 0));
        tryBuildList();
    }

    public void setRowSideInsets(int left, int right)
    {
        this.rowLeftInset = Math.max(0, left);
        this.rowRightInset = Math.max(0, right);
        tryBuildList();
    }

    public void setPlaceholder(String placeholderText)
    {
        this.placeholder = new JLabel(placeholderText);
        tryBuildList();
    }

    public void setPlaceholder(JComponent placeholderComponent)
    {
        this.placeholder = placeholderComponent;
        tryBuildList();
    }

    private List<ListItemPanel<T>> buildItemPanels()
    {
        return reorderableList
            .stream()
            .map(this::buildItemPanel)
            .collect(Collectors.toList());
    }

    @Override
    public void refresh()
    {
        // Rebuild the list first in case the underlying data changed
        tryBuildList();

        // Then refresh all children
        for (Component component : listPanel.getComponents()) {
            if (component instanceof Refreshable) {
                ((Refreshable) component).refresh();
            }
        }
    }

    private ListItemPanel<T> buildItemPanel(T item)
    {
        if (itemPanelMap.containsKey(item)) {
            return itemPanelMap.get(item);
        }

        ListItemPanel<T> itemPanel = renderItem.apply(item);

        itemPanel.onReordered((updatedItem) -> {
            tryBuildList();
            if (this.updatedListener != null) this.updatedListener.accept(updatedItem);
        });

        itemPanel.onRemoved((updatedItem) -> {
            tryBuildList();
            if (this.updatedListener != null) this.updatedListener.accept(updatedItem);
        });

        itemPanelMap.put(item, itemPanel);

        return itemPanel;
    }

    private GridBagConstraints getConstraints()
    {
        return getConstraints(0);
    }

    private GridBagConstraints getConstraints(int gridy)
    {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.gridy = gridy;
        constraints.gridx = 0;
        constraints.insets = new Insets(4, rowLeftInset, gap, rowRightInset);
        return constraints;
    }

    private void refreshChildMenus()
    {
        for (Component component : listPanel.getComponents()) {
            if (component instanceof ListItemPanel) {
                ((Refreshable) component).refresh();
            }
        }
    }

    /**
     * Build the initial list, if items are provided otherwise build a placeholder
     */
    public void tryBuildList()
    {
        itemPanelMap.clear();

        if (reorderableList.isEmpty()) {
            listPanel.removeAll();

            if (placeholder instanceof JLabel) {
                ((JLabel) placeholder).setForeground(ColorScheme.LIGHT_GRAY_COLOR);
            }
            JPanel placeholderPanel = new JPanel();
            placeholderPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
            placeholderPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            placeholderPanel.add(placeholder);
            listPanel.add(placeholderPanel, getConstraints());
        } else {
            listPanel.removeAll();

            GridBagConstraints constraints = getConstraints();
            buildItemPanels().forEach(component -> {
                listPanel.add(component, constraints);
                constraints.gridy++;
            });
        }

        refreshChildMenus();
        revalidate();
    }

    public void onUpdated(Consumer<T> listener) {
        this.updatedListener = listener;
    }
}
