package com.toofifty.goaltracker.ui.components;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Styled small button used inside ActionBar, with hover background effect.
 */
public final class ActionBarButton extends JButton
{
    private boolean hover;

    public ActionBarButton(String text, Runnable onClick)
    {
        super(text);
        setFont(FontManager.getRunescapeSmallFont());
        setForeground(Color.WHITE);
        setFocusable(false);
        setOpaque(false);
        setBorder(new EmptyBorder(4, 10, 4, 10));
        setMargin(new Insets(0,0,0,0));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(false);

        addActionListener(e -> { if (onClick != null) onClick.run(); });

        addMouseListener(new MouseAdapter()
        {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bg = hover ? ColorScheme.DARKER_GRAY_HOVER_COLOR : ColorScheme.DARK_GRAY_COLOR.brighter();
        Color outline = ColorScheme.DARKER_GRAY_HOVER_COLOR.darker();

        int arc = 8;
        int w = getWidth();
        int h = getHeight();

        int x = 0;
        int y = 0;
        int drawW = w;
        int drawH = h;

        g2.setColor(bg);
        g2.fillRoundRect(x, y, drawW, drawH, arc, arc);

        g2.setColor(outline);
        g2.drawRoundRect(x, y, drawW - 1, drawH - 1, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public boolean isContentAreaFilled()
    {
        return false;
    }

    @Override
    public boolean isOpaque()
    {
        return false;
    }

    @Override
    public Dimension getMinimumSize()
    {
        return getPreferredSize();
    }
}