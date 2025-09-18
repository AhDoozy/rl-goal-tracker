package com.toofifty.goaltracker.ui.inputs;

import com.toofifty.goaltracker.GoalTrackerPlugin;
import com.toofifty.goaltracker.models.Goal;
import com.toofifty.goaltracker.models.task.ItemTask;
import com.toofifty.goaltracker.ui.SimpleDocumentListener;
import com.toofifty.goaltracker.ui.components.TextButton;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.FlatTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;



/**
 * Input panel for creating Item tasks.
 * Provides search with close overlay, quantity field with k/m suffix support,
 * and automatic task submission on selection.
 */
public final class ItemTaskInput extends TaskInput
{
    private final ItemManager itemManager;
    private final ClientThread clientThread;

    private final FlatTextField quantityField = new FlatTextField();
    private final TextButton searchItemButton = new TextButton("Search...");
    private boolean searchOpen = false;
    private final JLabel selectedItemLabel = new JLabel();
    private final JPanel selectedItemPanel = new JPanel(new BorderLayout());

    private final Pattern numberPattern = Pattern.compile("^(?:\\d+)?$");
    private final Pattern mPattern = Pattern.compile("^(?:\\d+m)?$", Pattern.CASE_INSENSITIVE);
    private final Pattern kPattern = Pattern.compile("^(?:\\d+k)?$", Pattern.CASE_INSENSITIVE);

    private String quantityFieldValue = "1";
    private ItemComposition selectedItem;
    private String lastSearchText = "";

    public ItemTaskInput(GoalTrackerPlugin plugin, Goal goal)
    {
        super(plugin, goal, "Item");
        this.itemManager = plugin.getItemManager();
        this.clientThread = plugin.getClientThread();

        searchItemButton.onClick(e -> {
            if (!searchOpen) {
                if (plugin.getClient().getGameState() != GameState.LOGGED_IN) {
                    JOptionPane.showMessageDialog(this,
                        "You must be logged in to choose items",
                        "UwU",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                plugin.getItemSearch()
                    .tooltipText("Choose an item")
                    .onItemSelected(this::setSelectedItem)
                    .build();
                searchItemButton.setText("Close");
                searchOpen = true;
            }
            else {
                try {
                    plugin.getChatboxPanelManager().close();
                } catch (Exception ignored) {}
                searchItemButton.setText("Search...");
                searchOpen = false;
            }
        });
        getInputRow().add(searchItemButton, BorderLayout.WEST);

        quantityField.setBorder(new EmptyBorder(0, 8, 0, 8));
        quantityField.getTextField().setHorizontalAlignment(SwingConstants.RIGHT);
        quantityField.setText(quantityFieldValue);
        quantityField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        quantityField.getDocument().addDocumentListener(
            (SimpleDocumentListener) e -> SwingUtilities.invokeLater(() -> {
                String value = quantityField.getText();

                if (mPattern.matcher(value).find()) {
                    value = value.replace("m", "000000");
                    quantityFieldValue = value;
                    quantityField.setText(quantityFieldValue);
                }

                if (kPattern.matcher(value).find()) {
                    value = value.replace("k", "000");
                    quantityFieldValue = value;
                    quantityField.setText(quantityFieldValue);
                }

                if (!numberPattern.matcher(value).find()) {
                    quantityField.setText(quantityFieldValue);
                    return;
                }

                quantityFieldValue = value;
            }));
        quantityField.setPreferredSize(new Dimension(92, PREFERRED_INPUT_HEIGHT));

        getInputRow().add(quantityField, BorderLayout.CENTER);

        selectedItemPanel.setBorder(new EmptyBorder(0, 8, 0, 8));
        selectedItemPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        selectedItemPanel.add(selectedItemLabel, BorderLayout.CENTER);
        TextButton clearItemButton = new TextButton("X")
                .setMainColor(ColorScheme.PROGRESS_ERROR_COLOR)
                .onClick((e) -> clearSelectedItem());
        selectedItemPanel.add(clearItemButton, BorderLayout.EAST);
    }

    private void setSelectedItem(Integer rawId)
    {
        clientThread.invokeLater(() -> {
            int id = itemManager.canonicalize(rawId);
            selectedItem = itemManager.getItemComposition(id);
            selectedItemLabel.setText(selectedItem.getName());

            getInputRow().remove(searchItemButton);
            getInputRow().add(selectedItemPanel, BorderLayout.WEST);

            revalidate();
            repaint();

            // Immediately add the item task
            submit();

            // Reopen search so user can add multiple items
            plugin.getItemSearch()
                .tooltipText("Choose an item")
                .onItemSelected(this::setSelectedItem)
                .build();

            searchItemButton.setText("Close");
            searchOpen = true;
        });
    }

    @Override
    protected void submit()
    {
        if (selectedItem == null || quantityField.getText().isEmpty()) {
            return;
        }

        this.addTask(ItemTask.builder()
            .itemId(selectedItem.getId())
            .itemName(selectedItem.getName())
            .quantity(Integer.parseInt(quantityField.getText()))
            .build());
    }

    @Override
    protected void reset()
    {
        clearSelectedItem();
        quantityFieldValue = "1";
        quantityField.setText(quantityFieldValue);
    }

    private void clearSelectedItem()
    {
        selectedItem = null;

        getInputRow().remove(selectedItemPanel);
        getInputRow().add(searchItemButton, BorderLayout.WEST);

        revalidate();
        repaint();

        searchItemButton.setText("Search...");
        searchOpen = false;
    }

    private boolean containsTextField(Container c)
    {
        if (c == null) return false;
        for (Component comp : c.getComponents())
        {
            if (comp instanceof JTextComponent) return true;
            if (comp instanceof Container && containsTextField((Container) comp)) return true;
        }
        return false;
    }

    private JTextComponent findTextField(Container c)
    {
        if (c == null) return null;
        for (Component comp : c.getComponents())
        {
            if (comp instanceof JTextComponent) return (JTextComponent) comp;
            if (comp instanceof Container)
            {
                JTextComponent tf = findTextField((Container) comp);
                if (tf != null) return tf;
            }
        }
        return null;
    }

    // Small floating red X button to close the item search popup
    private void showCloseOverlay()
    {
        final int[] attempts = {0};
        final int maxAttempts = 20;
        final int delayMs = 150;

        ActionListener tryShow = new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                final JTextComponent searchField = (focusOwner instanceof JTextComponent) ? (JTextComponent) focusOwner : null;
                Window target = (focusOwner != null) ? SwingUtilities.getWindowAncestor(focusOwner) : null;

                if (target == null || searchField == null)
                {
                    if (++attempts[0] < maxAttempts)
                    {
                        new Timer(delayMs, this) {{ setRepeats(false); }}.start();
                    }
                    return;
                }

                // Create overlay dialog owned by the search window so it closes with it
                final JDialog overlay = new JDialog(target);
                overlay.setUndecorated(true);
                overlay.setAlwaysOnTop(true);
                overlay.setFocusableWindowState(false);
                overlay.setType(Window.Type.UTILITY);

                JPanel header = new JPanel(new BorderLayout());
                header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                JButton close = new JButton("X");
                close.setForeground(Color.WHITE);
                close.setBackground(ColorScheme.PROGRESS_ERROR_COLOR);
                close.setBorder(new EmptyBorder(2, 6, 2, 6));
                close.setFocusable(false);
                close.addActionListener(e2 -> {
                    overlay.dispose();
                    lastSearchText = "";
                    if (target instanceof JDialog) {
                        ((JDialog) target).dispose();
                    } else if (target instanceof JFrame) {
                        ((JFrame) target).dispose();
                    } else {
                        target.dispose();
                    }
                });
                header.add(close, BorderLayout.EAST);
                overlay.getContentPane().add(header);
                overlay.pack();

                // Position at top-right of the search text field (anchor to the actual box)
                Point tfLoc = searchField.getLocationOnScreen();
                int tfRight = tfLoc.x + searchField.getWidth();
                int x = tfRight - overlay.getWidth() - 4; // tuck inside right edge
                int y = tfLoc.y + 2; // inside the box near the top
                overlay.setLocation(x, y);
                overlay.setVisible(true);
                overlay.toFront();

                // Restore and persist the user's query
                if (lastSearchText != null && !lastSearchText.isEmpty())
                {
                    searchField.setText(lastSearchText);
                    searchField.setCaretPosition(searchField.getText().length());
                    searchField.requestFocusInWindow();
                }
                searchField.getDocument().addDocumentListener(new DocumentListener()
                {
                    @Override public void insertUpdate(DocumentEvent e) { lastSearchText = searchField.getText(); }
                    @Override public void removeUpdate(DocumentEvent e) { lastSearchText = searchField.getText(); }
                    @Override public void changedUpdate(DocumentEvent e) { lastSearchText = searchField.getText(); }
                });

                if (target instanceof Window) {
                    ((Window) target).addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosed(java.awt.event.WindowEvent e) { overlay.dispose(); }
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent e) { overlay.dispose(); }
                    });

                    target.addComponentListener(new java.awt.event.ComponentAdapter() {
                        @Override
                        public void componentMoved(java.awt.event.ComponentEvent e) {
                            try {
                                Point tfLoc2 = searchField.getLocationOnScreen();
                                int tfRight2 = tfLoc2.x + searchField.getWidth();
                                int x2 = tfRight2 - overlay.getWidth() - 4;
                                int y2 = tfLoc2.y + 2;
                                overlay.setLocation(x2, y2);
                            } catch (IllegalComponentStateException ignored) { }
                        }
                        @Override
                        public void componentResized(java.awt.event.ComponentEvent e) {
                            try {
                                Point tfLoc2 = searchField.getLocationOnScreen();
                                int tfRight2 = tfLoc2.x + searchField.getWidth();
                                int x2 = tfRight2 - overlay.getWidth() - 4;
                                int y2 = tfLoc2.y + 2;
                                overlay.setLocation(x2, y2);
                            } catch (IllegalComponentStateException ignored) { }
                        }
                    });
                }
            }
        };

        // start first attempt slightly delayed to give the popup time to appear
        new Timer(delayMs, tryShow) {{ setRepeats(false); }}.start();
    }

    // Try to locate the RuneLite Item Search popup window reliably
    private Window findItemSearchWindow()
    {
        // Prefer visible JDialogs containing a text field (likely the chatbox item search)
        for (Window w : Window.getWindows())
        {
            if (w != null && w.isShowing() && w instanceof JDialog)
            {
                if (w instanceof Container && containsTextField((Container) w))
                {
                    return w;
                }
            }
        }
        Window active = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (active != null && active.isShowing() && active instanceof Container && containsTextField((Container) active))
        {
            return active;
        }
        for (Window w : Window.getWindows())
        {
            if (w != null && w.isShowing() && w instanceof Container && containsTextField((Container) w))
            {
                return w;
            }
        }
        return null;
    }
    @Override
    protected boolean showAddButton()
    {
        return false;
    }
}