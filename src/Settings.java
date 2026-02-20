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
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;
import javax.swing.*;

/**
 * The {@code Setting} class manages a setting window that enables user to
 * configure preferences and start the game via
 * creating a new {@code Game} instance.
 *
 * @author Mingchun Zhuang
 * @version 1.0
 */
public class Settings {
    /**
     * A static variable storing the only one instance instantiated.
     */
    private static Settings instance;

    /**
     * A static constant holding the width of current window.
     */
    private static final int WINDOW_WIDTH = 600;

    /**
     * A static constant holding the height of current window.
     */
    private static final int WINDOW_HEIGHT = 800;

    /**
     * A static constant holding the height of the vertical interval of contents of
     * current window.
     */
    private static final int BREAK_HEIGHT = 50;

    /**
     * A static constant holding the width of each content box.
     */
    private static final int CONTENT_WIDTH = 500;

    /**
     * A static constant holding the height of each content box.
     */
    private static final int CONTENT_HEIGHT = 100;

    /**
     * A static constant holding the height of the horizontal interval of contents
     * of current window.
     */
    private static final int WIDTH_MARGIN = (WINDOW_WIDTH - CONTENT_WIDTH) / 2;

    /**
     * A static {@code JFrame} holding the instance of current window.
     */
    private static JFrame window;

    /**
     * A static {@code JTextField} holding the instance of text field where users
     * can enter their preferred initial
     * word.
     */
    private static JTextField initWordField;

    /**
     * A static {@code JTextField} holding the instance of text field where error
     * messages will be shown.
     */
    private static JTextField errorMessageField;

    /**
     * A static int holding the word length selected by the user.
     */
    private static int wordLength;

    /**
     * A static String holding the word source selected by the user.
     */
    private static String wordSource;

    /**
     * A static String holding the preferred initial word typed by the user.
     */
    private static String initWord;

    /**
     * A static String array holding all word sources available.
     */
    private static String[] wordSourceOptions;

    /**
     * A static String holding current hashtag.
     */
    private static String currentHashtag;

    /**
     * This method configs the setting window at the very beginning and should be
     * called before being set visible.
     *
     * @param wordLength        an int describing the length of words to be guessed.
     * @param wordSource        a String describing the specific source type,
     *                          included in <var>wordSourceOptions</var>.
     * @param wordLengthOptions a String array containing the word lengths to be
     *                          chosen.
     * @param wordSourceOptions a String array containing the word sources to be
     *                          chosen.
     */
    public void configSettings(int wordLength, String wordSource, String[] wordLengthOptions,
        String[] wordSourceOptions) {
        Settings.instance = this;
        Settings.wordLength = wordLength;
        Settings.wordSource = wordSource;
        Settings.wordSourceOptions = wordSourceOptions;

        // Configure window settings.
        window = new JFrame("Benvingut - eWordle");
        window.setLocationRelativeTo(null);
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel windowPanel = new JPanel();
        windowPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        window.add(windowPanel);
        window.pack();
        windowPanel.setBackground(new Color(34, 139, 34));
        windowPanel.setLayout(null);

        // ---- HEADER SECTION (top of window) ----
        int currentY = 15;

        // Main title
        JTextField titleField = Settings.textInit("\uD83C\uDFAE eWordle Català \uD83C\uDFAE", "Comic Sans MS",
                JTextField.CENTER, Font.BOLD, WIDTH_MARGIN, currentY, CONTENT_WIDTH, 55, 38, false, false);
        titleField.setForeground(Color.WHITE);
        windowPanel.add(titleField);
        currentY += 55;

        // CIDE subtitle
        JTextField cideHeader = Settings.textInit("CIDE", "Comic Sans MS", JTextField.CENTER, Font.BOLD,
                WIDTH_MARGIN, currentY, CONTENT_WIDTH, 35, 24, false, false);
        cideHeader.setForeground(Color.WHITE);
        cideHeader.setBackground(new Color(25, 100, 25));
        cideHeader.setOpaque(true);
        windowPanel.add(cideHeader);
        currentY += 40;

        // "Preferències" section header
        JTextField prefsLabel = Settings.textInit("Preferències", "Comic Sans MS", JTextField.CENTER,
                Font.BOLD, WIDTH_MARGIN, currentY, CONTENT_WIDTH, 35, 22, false, false);
        prefsLabel.setForeground(new Color(200, 255, 200));
        windowPanel.add(prefsLabel);
        currentY += 45;

        // ---- SEPARATOR ----
        JSeparator separator = new JSeparator();
        separator.setBounds(WIDTH_MARGIN, currentY, CONTENT_WIDTH, 2);
        separator.setForeground(new Color(25, 100, 25));
        windowPanel.add(separator);
        currentY += 15;

        // ---- FORM SECTION ----
        // Shared event consumer for both combos
        Consumer<ItemEvent> comboEventConsumer = event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                String selectedItem = (String) event.getItem();
                if (selectedItem.startsWith("Longitud: ")) {
                    Settings.wordLength = Integer.parseInt(selectedItem.substring(10));
                } else if (selectedItem.startsWith("Font: ")) {
                    Settings.wordSource = selectedItem.substring(6);
                }
            }
        };

        int labelHeight = 24;
        int comboHeight = 30;
        int fieldGap = 8;
        int sectionGap = 18;

        // -- Word length --
        JTextField lengthLabel = Settings.textInit("Longitud de Paraula", "", JTextField.LEFT, Font.PLAIN,
                WIDTH_MARGIN, currentY, CONTENT_WIDTH, labelHeight, 16, false, false);
        lengthLabel.setForeground(Color.WHITE);
        windowPanel.add(lengthLabel);
        currentY += labelHeight + fieldGap;

        windowPanel.add(initCombo("Longitud: ", wordLengthOptions, currentY,
                comboEventConsumer, "Longitud (Predeterminat: " + wordLength + " o última ronda)",
                wordLength + ""));
        currentY += comboHeight + sectionGap;

        // -- Word source --
        JTextField sourceLabel = Settings.textInit("Font de Paraula", "", JTextField.LEFT, Font.PLAIN,
                WIDTH_MARGIN, currentY, CONTENT_WIDTH, labelHeight, 16, false, false);
        sourceLabel.setForeground(Color.WHITE);
        windowPanel.add(sourceLabel);
        currentY += labelHeight + fieldGap;

        windowPanel.add(initCombo("Font: ", wordSourceOptions, currentY,
                comboEventConsumer, "Font (Predeterminat: " + wordSource + " o última ronda)",
                wordSource));
        currentY += comboHeight + sectionGap;

        // -- Word / hashtag input --
        JTextField wordLabel = Settings.textInit("Paraula o Hashtag Wordle", "", JTextField.LEFT, Font.PLAIN,
                WIDTH_MARGIN, currentY, CONTENT_WIDTH, labelHeight, 16, false, false);
        wordLabel.setForeground(Color.WHITE);
        windowPanel.add(wordLabel);
        currentY += labelHeight + fieldGap;

        initWordField = Settings.textInit("", "", JTextField.LEFT, Font.PLAIN, WIDTH_MARGIN,
                currentY, CONTENT_WIDTH, 28, 16, true, true);
        initWordField.setForeground(Color.BLACK);
        initWordField.setOpaque(true);
        initWordField.setBackground(Color.WHITE);
        windowPanel.add(initWordField);
        currentY += 28 + fieldGap;

        // Hint
        JTextField hintLabel = Settings.textInit(
                "Consell: Deixa buit per endevinar una paraula aleatòria.", "",
                JTextField.LEFT, Font.PLAIN, WIDTH_MARGIN, currentY, CONTENT_WIDTH, labelHeight, 13,
                false, false);
        hintLabel.setForeground(new Color(200, 255, 200));
        windowPanel.add(hintLabel);
        currentY += labelHeight + fieldGap;

        // Add logo image below hint
        try {
            String imagePath = "juego2\\javadoc\\script-dir\\images\\logo Cide.jpg";
            java.io.File imageFile = new java.io.File(imagePath);
            if (imageFile.exists()) {
                ImageIcon logoIcon = new ImageIcon(imagePath);
                Image logoImage = logoIcon.getImage();
                Image scaledImage = logoImage.getScaledInstance(200, 130, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
                logoLabel.setBounds(WIDTH_MARGIN + 150, currentY, 200, 175);
                windowPanel.add(logoLabel);
                currentY += 130 + fieldGap;
            } else {
                System.out.println("Archivo no encontrado: " + imageFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Error cargando la imagen: " + e.getMessage());
            e.printStackTrace();
        }

        // ---- BOTTOM SECTION (error message + buttons) ----
        int bottomY = WINDOW_HEIGHT - BREAK_HEIGHT * 2 - CONTENT_HEIGHT;

        // Error message field
        errorMessageField = Settings.textInit("", "", JTextField.CENTER, Font.BOLD, WIDTH_MARGIN,
                bottomY, CONTENT_WIDTH, BREAK_HEIGHT, 15, false, false);
        errorMessageField.setForeground(Color.YELLOW);
        errorMessageField.setBackground(new Color(34, 139, 34));
        windowPanel.add(errorMessageField);

        bottomY += BREAK_HEIGHT;

        // Instructions button (left)
        int btnHeight = 60;
        JButton instructionsButton = initButton("Instruccions",
                WIDTH_MARGIN, bottomY,
                CONTENT_WIDTH / 3 - 10, btnHeight, 18,
                event -> showInstructions());
        instructionsButton.setBackground(new Color(25, 100, 25));
        instructionsButton.setForeground(Color.WHITE);
        windowPanel.add(instructionsButton);

        // Start button (right, larger)
        JButton startButton = initButton("Iniciar Joc",
                WIDTH_MARGIN + CONTENT_WIDTH / 3 + 10, bottomY,
                CONTENT_WIDTH * 2 / 3 - 10, btnHeight, 24,
                event -> start());
        startButton.setBackground(new Color(25, 100, 25));
        startButton.setForeground(Color.WHITE);
        windowPanel.add(startButton);
    }

    /**
     * Returns an instance of current class, where only one copy of instance will
     * exist.
     *
     * <p>
     * If no instance found, a new one will be generated and stored. Otherwise, the
     * stored one will be return.
     *
     * @return an instance of current class.
     */
    public static Settings getInstance() {
        if (Settings.instance == null)
            Settings.instance = new Settings();
        return Settings.instance;
    }

    /**
     * Returns the word source selected by the user.
     *
     * @return a String describing the word source selected by the user.
     */
    public static String getWordSource() {
        return Settings.wordSource;
    }

    /**
     * Returns the initial word typed by the user.
     *
     * @return a String describing the initial word typed by the user.
     */
    public static String getInitWord() {
        return Settings.initWord;
    }

    /**
     * Returns current hashtag.
     *
     * @return a String describing current hashtag.
     */
    public static String getCurrentHashtag() {
        return Settings.currentHashtag;
    }

    /**
     * This method sets the window to the center and makes it change its visible
     * status
     *
     * @param status a boolean describing the intended visible status of the window.
     */
    public void setVisibleStatus(Boolean status) {
        window.setLocationRelativeTo(null);
        window.setVisible(status);
    }

    /**
     * This static method returns a configured {@code JTextField}.
     *
     * @param content      a String describing the name of the text field.
     * @param fontName     a String describing the name of the font.
     * @param alignment    an int describing the alignment of the words of the text
     *                     field.
     * @param fontStyle    an int describing the font style of the text field.
     * @param x            an int describing the new horizontal or
     *                     {@code x}-coordinate of the text field.
     * @param y            an int describing the new vertical or
     *                     {@code y}-coordinate of the text field.
     * @param width        an int describing the horizontal size of the text field.
     * @param height       an int describing the vertical size of the text field.
     * @param fontSize     an int describing the font size of the text field.
     * @param opaqueStatus a boolean describing the opaque status of the text field.
     * @param editable     a boolean describing the editable status of the text
     *                     field.
     * @return a configured JTextField.
     */
    public static JTextField textInit(String content, String fontName, int alignment, int fontStyle, int x, int y,
            int width, int height, int fontSize, boolean opaqueStatus, boolean editable) {
        JTextField textField = new JTextField(content);
        textField.setHorizontalAlignment(alignment);
        textField.setBounds(x, y, width, height);
        textField.setEditable(editable);
        textField.setOpaque(opaqueStatus);
        textField.setBorder(null);
        textField.setFont(new Font(fontName, fontStyle, fontSize));
        return textField;
    }

    /**
     * This static method returns a configured {@code JButton}.
     *
     * @param content  a String describing the content displayed on the button.
     * @param x        an int describing the new horizontal or {@code x}-coordinate
     *                 of the button.
     * @param y        an int describing the new vertical or {@code y}-coordinate of
     *                 the button.
     * @param xSize    an int describing the horizontal size of the button.
     * @param ySize    an int describing the vertical size of the button.
     * @param fontSize an int describing the font size of the button.
     * @param event    an {@code ActionListener} that will be called if the button
     *                 is pressed.
     * @return a configured JButton.
     */
    public static JButton initButton(String content, int x, int y, int xSize, int ySize, int fontSize,
            ActionListener event) {
        JButton button = new JButton(content);
        button.setBounds(x, y, xSize, ySize);
        button.setFont(new Font("Comic Sans MS", Font.PLAIN, fontSize));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(event);
        button.setFocusable(false);
        return button;
    }

    /**
     * This method returns a configured combo to the window.
     *
     * @param hint         a String describing the hint to add before each content.
     * @param contents     a String array describing contents to be displayed in the
     *                     combo.
     * @param height       an int describing the new vertical or
     *                     {@code y}-coordinate of the combo.
     * @param consumer     a {@code Consumer<ItemEvent>} that consumes events
     *                     related to the combo.
     * @param toolTip      a String describing the tooltip displayed if the mouse is
     *                     placed onto the combo.
     * @param selectedItem a String describing the default selected item of the
     *                     combo.
     * @return a configured {@code JComboBox<String>}.
     */
    private JComboBox<String> initCombo(String hint, String[] contents, int height, Consumer<ItemEvent> consumer,
            String toolTip, String selectedItem) {
        contents = contents.clone();
        for (int i = 0; i < contents.length; i++)
            contents[i] = hint + contents[i];
        JComboBox<String> result = new JComboBox<>(contents);
        result.setBounds(WIDTH_MARGIN, height, CONTENT_WIDTH, CONTENT_HEIGHT / 3);
        result.setToolTipText(toolTip);
        result.setCursor(new Cursor(Cursor.HAND_CURSOR));
        result.addItemListener(consumer::accept);
        result.setSelectedItem(hint + selectedItem);
        return result;
    }

    /**
     * This method checks the word typed by the user and create a new {@code Game}
     * instance to start the game if the
     * check is passed. Otherwise, this method will display error message in the
     * <var>errorMessageField</var>
     */
    private void start() {
        // All internal letters are stored and processed in uppercase.
        String text = initWordField.getText().toUpperCase();
        // Hashtag handler
        if (text.length() > 0 && text.charAt(0) == '#') {
            String[] decodeResult = Settings.hashtagDecoder(text).split("\\$");
            if (decodeResult[0].length() != 0) {
                errorMessageField.setText(decodeResult[0]);
                return;
            }
            errorMessageField.setText("");
            this.setVisibleStatus(false);
            Settings.initWord = decodeResult[1];
            currentHashtag = text;
            Game.createInstance().playGame(Settings.wordSourceOptions[Integer.parseInt(decodeResult[2]) - 1],
                    decodeResult[1], currentHashtag);
        }
        // Not hashtag
        else if (text.length() == wordLength || text.length() == 0) {
            String checkResult = Service.getInstance().checkExistence(text, wordSource);
            if (checkResult.length() == 0) {
                if (text.length() == 0) {
                    text = Service.getInstance().generateRandomWord(wordLength, wordSource);
                    initWordField.setText(text);
                }
                if (!text.equals("No trobat")) {
                    errorMessageField.setText("");
                    this.setVisibleStatus(false);
                    Settings.initWord = text;
                    currentHashtag = Settings.hashtagEncoder(wordSource, text);
                    Game.createInstance().playGame(wordSource, text, currentHashtag);
                } else
                    errorMessageField.setText(text);
            } else
                errorMessageField.setText(checkResult);
        } else
            errorMessageField.setText("Error: La longitud de la paraula de Wordle és massa " +
                    (text.length() < wordLength ? "curta" : "llarga") + "!");
    }

    /**
     * This static method encodes current settings and return the hashtag.
     *
     * <p>
     * The hashtag is generated from three parameters: <var>hashtagWordSource</var>,
     * <var>hashtagWord</var>, and
     * <var>hashtagWordLength</var>(calculated from <var>hashtagWord</var>).
     *
     * <p>
     * The first step is to generate a base-29 integer decoded three parameters
     * mentioned above, where the order from
     * the lower digit of the integer is <var>hashtagWordLength</var>,
     * <var>hashtagWordSource</var>, and then
     * <var>hashtagWord</var>, whose alphabetic letters are converted to integer
     * counting from 0 to 25 inclusion. For
     * example, when <var>hashtagWordLength=5</var>, <var>hashtagWordSource=3</var>,
     * and <var>hashtagWord="APPLE"</var>,
     * the integer generated equals to (29^0)*5 + (29^1)*3 + (29^2)*0 + (29^3)*15 +
     * (29^4)*15 + (29^5)*11 + (29^6)*4 =
     * 2615891065.
     *
     * <p>
     * The second step is to convert the number system to base-36 (26+10),
     * representing by number and alphabet letter,
     * where number counts from 0 to 9 and alphabet letter counts from 10 to 35. For
     * example, the sample shown above
     * will become #179FMGP.
     *
     * @param hashtagWordSource a String describing the word source selected.
     * @param hashtagWord       a String describing the Wordle word selected.
     * @return a String describing the decoded hashtag result.
     */
    static String hashtagEncoder(String hashtagWordSource, String hashtagWord) {
        // System.out.println("Encoding:"+hashtagWordSource+" "+hashtagWord);
        final long hashtagLetterCount = 26 + 10;
        final long radix = 29;
        long integer = 0;
        for (int i = hashtagWord.length() - 1; i >= 0; i--) {
            integer *= radix;
            integer += hashtagWord.charAt(i) - 'A';
        }
        for (int i = 0; i < Settings.wordSourceOptions.length; i++)
            // Found word source in wordSourceOptions with index to be later decoded.
            if (Settings.wordSourceOptions[i].equals(hashtagWordSource)) {
                // Encode hashtag word source, which counts from 1.
                integer *= radix;
                integer += i + 1;
                // Encode hashtag word length.
                integer *= radix;
                integer += hashtagWord.length();
                // Convert integer to base-36 hashtag representation.
                StringBuilder reverseHashtag = new StringBuilder();
                do {
                    int currentDigit = (int) (integer % hashtagLetterCount);
                    reverseHashtag.append(currentDigit < 10 ? (char) (currentDigit + (int) '0')
                            : (char) (currentDigit - 10 + (int) 'A'));
                    integer /= hashtagLetterCount;
                } while (integer > 0);
                return "#" + reverseHashtag.reverse();
            }
        return "Error de hashtag: no s'ha trobat la paraula font";
    }

    /**
     * This static method decodes hashtag and return the results.
     *
     * @param hashtag a String describing the hint to add before each content,
     *                maximum length (excluded '#') 12
     *                supported using current decoder base on {@code long} (possible
     *                longer support under using
     *                BigInteger but not necessary ).
     * @return a String describing the results, whose format is
     *         "errorMessage$word$difficulty", typed
     *         "String$String$int", where the latter two will be not null when
     *         {@code errorMessage} is empty, representing
     *         successfully decoded.
     *         Note: difficulty counts from 1 to total word sources available.
     *         Sample: error: "Invalid hashtag input$$", successfully decoded:
     *         "$apple$1".
     */
    static private String hashtagDecoder(String hashtag) {
        final long hashtagLetterCount = 26 + 10;
        final long radix = 29;
        long encoded = 0;
        if (hashtag.length() > 13)
            return "Invalid hashtag input: length too large$$";
        // Decode raw base-hashtagLetterCount String to long.
        for (int i = 1; i < hashtag.length(); i++) {
            char ch = hashtag.charAt(i);
            if (Character.isDigit(ch)) {
                encoded *= hashtagLetterCount;
                encoded += Character.digit(ch, 10);
            } else if (Character.isAlphabetic(ch)) {
                encoded *= hashtagLetterCount;
                encoded += ((int) ch) - ((int) 'A') + 10;
            } else // illegal letter
                return "Invalid hashtag input: illegal letter$$";
        }
        /* Retrieve details from decoded base-radix(29) integer. */
        // Retrieve word length.
        long hashtagWordLength = encoded % radix;
        encoded /= radix;
        // Retrieve word source.
        int hashtagWordSource = (int) (encoded % radix);
        if (!(0 < hashtagWordSource && hashtagWordSource <= Settings.wordSourceOptions.length))
            return "Invalid hashtag input: illegal word source option$$";
        String hashtagWordSourceStr = Settings.wordSourceOptions[hashtagWordSource - 1];
        encoded /= radix;
        // Retrieve Wordle word.
        StringBuilder hashtagWord = new StringBuilder();
        for (int i = 0; i < hashtagWordLength; i++) {
            long currentDigit = encoded % radix;
            encoded /= radix;
            if (0 <= currentDigit && currentDigit < 26)
                hashtagWord.append((char) (currentDigit + (int) 'A'));
            else
                return "Invalid hashtag input: illegal word letter$$";
        }
        // Check decoded result in Service.
        String hashtagCheckResult = Service.getInstance().checkExistence(hashtagWord.toString(), hashtagWordSourceStr);
        if (hashtagCheckResult.length() == 0) {
            return "$" + hashtagWord + "$" + hashtagWordSource;
        }
        return "Invalid hashtag input: " + hashtagCheckResult + "$$";
    }

    /**
     * This method displays instructions for the game in a new window.
     */
    private void showInstructions() {
        JFrame instructionsFrame = new JFrame("Instruccions - eWordle");
        instructionsFrame.setResizable(true);
        instructionsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextArea instructionsText = new JTextArea();
        instructionsText.setEditable(false);
        instructionsText.setLineWrap(true);
        instructionsText.setWrapStyleWord(true);
        instructionsText.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
        instructionsText.setBackground(new Color(34, 139, 34));
        instructionsText.setForeground(Color.WHITE);
        instructionsText.setMargin(new Insets(20, 20, 20, 20));

        String instructions = "🎮 COM JUGAR A eWORDLE 🎮" +
                "Objectiu:" +
                "Endevina la paraula en 6 intents o menys!" +
                "Com funciona:" +
                "1. Trieu la longitud de la paraula (5-8 lletres)" +
                "2. Trieu una font de paraules (Matemàtiques, Biologia, Llengües, etc.)" +
                "3. Escriviu les lletres del vostre palpit i premeu ENTER" +
                "4. Els colors us indicaran si la vostra resposta és correcta:" +
                "   🟩 VERD: Lletra correcta en la posició correcta" +
                "   🟨 GROC: Lletra que està a la paraula però en altra posició" +
                "   ⬜ GRIS: Lletra que no està a la paraula" +
                "Consells:" +
                "- Deixa el camp buit per obtenir una paraula aleatòria" +
                "- Pots usar hashtags per compartir el teu palpit" +
                "- Pulsa el botó '?' durant el joc per obtenir ajuda" +
                "Diccionaris disponibles:" +
                "• Català\n• Matemàtiques\n • Biologia\n• Llengües\n• Esports\n• Futbolistes\n• Informàtica\n• Tot\n";

        instructionsText.setText(instructions);
        instructionsText.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(instructionsText);
        instructionsFrame.add(scrollPane);
        instructionsFrame.setSize(500, 600);
        instructionsFrame.setLocationRelativeTo(window);
        instructionsFrame.setVisible(true);
    }
}