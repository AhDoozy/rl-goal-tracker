package com.toofifty.goaltracker.ui.components;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.Text;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

/**
 * Simple generic ComboBox with optional formatter and a renderer
 * that matches RuneLite dark styling. Includes a helper to keep the
 * popup open after a selection (for rapid multi-add flows).
 *
 * This version also supports:
 *  - Custom up/down arrow icons loaded from resources (combo_arrow_down.png / combo_arrow_up.png)
 *  - Compact mode (~10% smaller font), or arbitrary font scaling via setFontScale
 */
/**
 * Generic RuneLite-styled combo box with optional formatting, custom arrow icons, and font scaling.
 */
public final class ComboBox<T> extends JComboBox<T>
{
    private Function<T, String> formatter = null;

    private double fontScale = 1.0; // 1.0 = default; 0.9 = compact
    private ImageIcon arrowDownIcon;
    private ImageIcon arrowUpIcon;
    private JButton arrowButtonRef; // created by UI#createArrowButton

    public ComboBox()
    {
        super();
        setRenderer(new ComboBoxListRenderer());
        // Keep consistent look with RuneLite
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setForeground(Color.WHITE);

        // Load arrow icons from resources if available
        // (If null, we just fall back to no icon customization)
        arrowDownIcon = loadIcon("/combo_arrow_down.png");
        arrowUpIcon = loadIcon("/combo_arrow_up.png");

        // Install a BasicComboBoxUI that uses our custom arrow button (if icons are available)
        setUI(new BasicComboBoxUI()
        {
            @Override
            protected JButton createArrowButton()
            {
                JButton b = (arrowDownIcon != null) ? new JButton(arrowDownIcon) : new JButton();
                b.setBorder(new EmptyBorder(0, 6, 0, 6));
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
                b.setOpaque(false);
                arrowButtonRef = b;
                return b;
            }
        });

        // Swap arrow icon on popup visibility changes (if we have icons)
        addPopupMenuListener(new PopupMenuListener()
        {
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e)
            {
                if (arrowButtonRef != null && arrowUpIcon != null)
                {
                    arrowButtonRef.setIcon(arrowUpIcon);
                }
            }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
            {
                if (arrowButtonRef != null && arrowDownIcon != null)
                {
                    arrowButtonRef.setIcon(arrowDownIcon);
                }
            }
            @Override public void popupMenuCanceled(PopupMenuEvent e)
            {
                if (arrowButtonRef != null && arrowDownIcon != null)
                {
                    arrowButtonRef.setIcon(arrowDownIcon);
                }
            }
        });

        // Apply initial font scaling
        applyFontScale();
    }

    public ComboBox(T[] items)
    {
        this();
        setItems(java.util.Arrays.asList(items));
    }

    public ComboBox(List<T> items)
    {
        this();
        setItems(items);
    }

    private ImageIcon loadIcon(String path)
    {
        java.net.URL url = getClass().getResource(path);
        return (url != null) ? new ImageIcon(url) : null;
    }

    /**
     * Compact preset (~10% smaller font).
     */
    public void setCompact(boolean compact)
    {
        setFontScale(compact ? 0.9 : 1.0);
    }

    /**
     * Arbitrary font scaling (1.0 = normal).
     */
    public void setFontScale(double scale)
    {
        if (scale <= 0) scale = 1.0;
        this.fontScale = scale;
        applyFontScale();
        repaint();
    }

    private void applyFontScale()
    {
        Font f = getFont();
        if (f != null)
        {
            int newSize = Math.max(10, Math.round(f.getSize() * (float) fontScale));
            setFont(new Font(f.getName(), f.getStyle(), newSize));
        }
    }

    public void setItems(List<T> items)
    {
        DefaultComboBoxModel<T> model = new DefaultComboBoxModel<>();
        if (items != null)
        {
            for (T it : items)
            {
                model.addElement(it);
            }
        }
        setModel(model);
    }

    public void setFormatter(Function<T, String> formatter)
    {
        this.formatter = formatter;
        repaint();
    }

    /**
     * Re-opens the popup after a selection is made, enabling fast repeated adds.
     * Call this once after constructing the combo if you want the behavior.
     */
    public void setStayOpenOnSelection(boolean stayOpen)
    {
        if (stayOpen)
        {
            this.addActionListener(e ->
                SwingUtilities.invokeLater(() -> this.setPopupVisible(true))
            );
        }
    }

    private class ComboBoxListRenderer implements ListCellRenderer<T>
    {
        @Override
        public Component getListCellRendererComponent(
            JList<? extends T> list,
            T value,
            int index,
            boolean isSelected,
            boolean cellHasFocus)
        {
            JPanel container = new JPanel(new BorderLayout());
            container.setBorder(new EmptyBorder(2, 6, 2, 6));

            JLabel label = new JLabel();
            label.setOpaque(false);

            if (value != null)
            {
                if (formatter != null)
                {
                    label.setText(formatter.apply(value));
                }
                else if (value instanceof Enum)
                {
                    label.setText(Text.titleCase((Enum<?>) value));
                }
                else
                {
                    label.setText(value.toString());
                }
            }
            else
            {
                label.setText("");
            }

            // Use the exact same font as the combo for crisp text
            label.setFont(ComboBox.this.getFont());

            container.add(label, BorderLayout.WEST);

            if (isSelected)
            {
                container.setBackground(ColorScheme.DARK_GRAY_COLOR);
                label.setForeground(Color.WHITE);
            }
            else
            {
                container.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                label.setForeground(Color.WHITE);
            }

            return container;
        }
    }
}
