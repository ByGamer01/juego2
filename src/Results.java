/*
 * Copyright 2022 Mingchun Zhuang (http://me.mczhuang.cn)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.THE
 * SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import javax.swing.*;

/**
 * The {@code Results} class manages a result window that enables user to see the result and choose whether to restart
 * or go back to settings.
 *
 * @author Mingchun Zhuang
 * @version 1.0
 */
public class Results {
    /**
     * A static variable storing the only one instance instantiated.
     */
    private static Results instance;

    /**
     * A static constant holding the width of current window.
     */
    private static final int WINDOW_WIDTH = 600;

    /**
     * A static constant holding the height of current window.
     */
    private static final int WINDOW_HEIGHT = 800;

    /**
     * A static constant holding the height of each content box.
     */
    private static final int CONTENT_HEIGHT = 100;

    /**
     * A static constant holding the size of the interval of contents of current window.
     */
    private static final int CONTENT_MARGIN = 50;

    /**
     * A static constant holding the width of each content box.
     */
    private static final int CONTENT_WIDTH = WINDOW_WIDTH - CONTENT_MARGIN * 2;

    /**
     * A static {@code JFrame} holding the instance of current window.
     */
    private final JFrame window;

    /**
     * A static {@code JTextField} holding the instance of text field that displays success or fail status.
     */
    private final JTextField resultBoard;

    /**
     * A static {@code JTextField} holding the instance of text field that displays the word that the user tried to
     * guess.
     */
    private final JTextField wordBoard;

    /**
     * A static {@code JTextField} holding the instance of text field that displays the tries that the user used to
     * guess.
     */
    private final JTextField triesBoard;

    /**
     * A {@code JTextField} holding the instance of text field that displays copy status.
     */
    private final JTextField copiedReminder;

    /**
     * A {@code JTextField} holding the counter display.
     */
    private final JTextField counterBoard;

    /**
     * A static {@code JTextField} holding the tries that the user used to guess.
     */
    private int triesUsed;

    /**
     * An {@code ArrayList} holding score of each confirmed input, where 0 is for grey, 1 is for yellow, 2 is for green.
     */
    private ArrayList<Integer> scoreByOrder;

    /**
     * A boolean holding the status that whether the user win.
     */
    private Boolean isSuccess;

    /**
     * A boolean holding the status that whether the user opened the helper window.
     */
    private boolean isOpenedHelper = false;

    /**
     * Static counters for wins and losses across the session.
     */
    private static int totalWins = 0;
    private static int totalLosses = 0;

    /**
     * The only constructor for class {@code Results}.
     *
     * <p>
     * This constructor will initiate the window and complete the configuration. The window is hidden and waits for
     * calling through {@link Results#showResults(String, int, boolean, ArrayList, boolean)}.
     */
    public Results() {
        Results.instance = this;

        // Configure window settings.
        window = new JFrame("Resultats");
        window.setLocationRelativeTo(null);
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel windowPanel = new JPanel();
        windowPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        window.add(windowPanel);
        window.pack();
        windowPanel.setBackground(new Color(34, 139, 34));
        windowPanel.setLayout(null);

        // Add result board to the window.
        int currentHeight = CONTENT_MARGIN / 2;
        resultBoard = Settings.textInit("", "Comic Sans MS", JTextField.CENTER, Font.BOLD,
                CONTENT_MARGIN, currentHeight, CONTENT_WIDTH, CONTENT_HEIGHT, 80, false,
                false);
        resultBoard.setForeground(Color.WHITE);
        windowPanel.add(resultBoard);

        // Add the hint board that displays "Endevinar" and word board to the window.
        currentHeight += CONTENT_HEIGHT + CONTENT_MARGIN / 2;
        JTextField hintBoard = Settings.textInit("Endevinar", "Comic Sans MS", JTextField.CENTER,
                Font.PLAIN, CONTENT_MARGIN, currentHeight - CONTENT_MARGIN / 2, CONTENT_WIDTH, CONTENT_MARGIN,
                30, false, false);
        hintBoard.setForeground(Color.WHITE);
        windowPanel.add(hintBoard);
        wordBoard = Settings.textInit("", "", JTextField.CENTER, Font.PLAIN, CONTENT_MARGIN,
                currentHeight, CONTENT_WIDTH, CONTENT_HEIGHT, 60, false, false);
        wordBoard.setForeground(Color.WHITE);
        windowPanel.add(wordBoard);

        // Add the tries board to the window.
        currentHeight += CONTENT_HEIGHT + CONTENT_MARGIN / 2;
        triesBoard = Settings.textInit("", "Comic Sans MS", JTextField.CENTER, Font.PLAIN,
                CONTENT_MARGIN, currentHeight, CONTENT_WIDTH, CONTENT_HEIGHT / 2, 40, false,
                false);
        triesBoard.setForeground(Color.WHITE);
        windowPanel.add(triesBoard);

        // Add counter board (wins/losses).
        currentHeight += CONTENT_HEIGHT / 2 + 10;
        counterBoard = Settings.textInit("", "Comic Sans MS", JTextField.CENTER, Font.PLAIN,
                CONTENT_MARGIN, currentHeight, CONTENT_WIDTH, CONTENT_MARGIN, 22, false, false);
        counterBoard.setForeground(Color.YELLOW);
        windowPanel.add(counterBoard);

        // Add two buttons to the window with event handlers respectively.
        currentHeight += CONTENT_MARGIN + 10;
        JButton toSettings = Settings.initButton("Configuració", CONTENT_MARGIN, currentHeight,
                (CONTENT_WIDTH - CONTENT_MARGIN) / 2, CONTENT_HEIGHT, 20, event -> {
                    Settings.getInstance().setVisibleStatus(true);
                    window.setVisible(false);
                });
        toSettings.setBackground(new Color(25, 100, 25));
        toSettings.setForeground(Color.WHITE);
        toSettings.setToolTipText("Torna a la pàgina de Preferències");
        windowPanel.add(toSettings);
        JButton toRestart = Settings.initButton("Tornar a jugar",
                CONTENT_MARGIN * 2 + (CONTENT_WIDTH - CONTENT_MARGIN) / 2, currentHeight,
                (CONTENT_WIDTH - CONTENT_MARGIN) / 2, CONTENT_HEIGHT, 20, event -> {
                    Game.createInstance().playGame(Settings.getWordSource(), Settings.getInitWord(),
                            Settings.getCurrentHashtag());
                    window.setVisible(false);
                });
        toRestart.setBackground(new Color(25, 100, 25));
        toRestart.setForeground(Color.WHITE);
        toRestart.setToolTipText("Utilitza les preferències actuals amb la mateixa paraula");
        windowPanel.add(toRestart);

        // Add share button with its event handler and its reminder to the window.
        currentHeight += CONTENT_HEIGHT;
        copiedReminder = Settings.textInit("", "Comic Sans MS", JTextField.CENTER,
                Font.PLAIN, CONTENT_MARGIN, currentHeight, CONTENT_WIDTH, CONTENT_MARGIN / 2, 16,
                false, false);
        copiedReminder.setForeground(Color.YELLOW);
        windowPanel.add(copiedReminder);
        currentHeight += CONTENT_MARGIN / 2;
        JButton shareResult = Settings.initButton("Comparteix", CONTENT_MARGIN, currentHeight,
                CONTENT_WIDTH, CONTENT_HEIGHT, 40, event -> {
                    StringBuilder resultStr = new StringBuilder();
                    resultStr.append("eWordle ").append(isOpenedHelper ? "*" : "").append(isSuccess ? triesUsed : "X")
                            .append("/").append(Settings.getInitWord().length() + 1).append("\n");
                    resultStr.append(Settings.getCurrentHashtag()).append("\n").append("\n");
                    final int initWordLength = Settings.getInitWord().length();
                    for (int i = 0; i < scoreByOrder.size(); i++) {
                        int score = scoreByOrder.get(i);
                        resultStr.append(score == 0 ? "x" : (score == 1 ? "o" : "v"));
                        if (i % initWordLength + 1 == initWordLength)
                            resultStr.append("\n");
                    }
                    StringSelection stringSelection = new StringSelection(resultStr.toString());
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                    copiedReminder.setText("Copiat al porta-retalls.");
                });
        shareResult.setBackground(new Color(25, 100, 25));
        shareResult.setForeground(Color.WHITE);
        shareResult.setToolTipText("Copia els teus resultats al porta-retalls.");
        windowPanel.add(shareResult);

    }

    /**
     * Returns an instance of current class, where only one copy of instance will exist.
     *
     * <p>
     * If no instance found, a new one will be generated and stored. Otherwise, the stored one will be return.
     *
     * @return an instance of current class.
     */
    public static Results getInstance() {
        if (Results.instance == null)
            Results.instance = new Results();
        return Results.instance;
    }

    /**
     * This static method shows result window with given parameters.
     *
     * @param initWord       a String that the user tried to guess.
     * @param tries          an int describing the number of tries used.
     * @param isSuccess      a boolean describing the final status of the game.
     * @param scoreByOrder   an {@code ArrayList} holding scored typed word history.
     * @param isOpenedHelper a boolean holding the status that whether the user opened helper window.
     */
    public void showResults(String initWord, int tries, boolean isSuccess, ArrayList<Integer> scoreByOrder,
                            boolean isOpenedHelper) {
        this.scoreByOrder = scoreByOrder;
        this.copiedReminder.setText("");
        this.isSuccess = isSuccess;
        this.isOpenedHelper = isOpenedHelper;
        triesUsed = tries;

        // Update counter
        if (isSuccess) totalWins++;
        else totalLosses++;

        window.setLocationRelativeTo(null);
        resultBoard.setText(isSuccess ? "Èxit" : "Ha fallat");
        Game.setColor(resultBoard, isSuccess ? new Color(121, 167, 107) : new Color(198, 60, 60),
                new Color(34, 139, 34));
        wordBoard.setText(initWord);
        triesBoard.setText("Intents: " + (isOpenedHelper ? "*" : "") + tries);
        counterBoard.setText("Guanyades: " + totalWins + "  |  Perdudes: " + totalLosses);
        window.setVisible(true);
    }
}