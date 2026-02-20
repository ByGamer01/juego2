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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * The {@code Service} class loads words from {@code Trimmed.csv} and stores indexes of those words, providing all the
 * functionalities related to word sources.
 *
 * @author Mingchun Zhuang
 * @version 1.0
 */
public class Service {
    /**
     * A static variable storing the only one instance instantiated.
     */
    private static Service instance;

    /**
     * A nested HashMap storing the difficulty of words from the given source, which should be attained by
     * <var>length</var> and then the word itself.
     */
    private HashMap<Integer, HashMap<String, Integer>> indexByLength;

    /**
     * A nested HashMap storing the words from the given source, which should be attained first by
     * <var>length</var> and then <var>difficulty</var>.
     */
    private HashMap<Integer, HashMap<Integer, ArrayList<String>>> wordByLengthThenDifficulty;

    /**
     * Optional Catalan word stores (loaded from Trimmed_ca.csv if present).
     */
    private HashMap<Integer, HashMap<String, Integer>> indexByLengthCatalan;
    private HashMap<Integer, HashMap<Integer, ArrayList<String>>> wordByLengthThenDifficultyCatalan;

    /**
     * Per-source thematic dictionaries loaded from individual CSV files.
     */
    private HashMap<String, HashMap<Integer, HashMap<String, Integer>>> thematicIndex;
    private HashMap<String, HashMap<Integer, HashMap<Integer, ArrayList<String>>>> thematicWords;

    /**
     * A HashMap storing the <var>difficulty</var> of <var>wordSource</var>
     */
    private HashMap<String, Integer> difficultyByWordSource;

    /**
     * Mapping from word source name to CSV filename for thematic sources.
     */
    private static final HashMap<String, String> SOURCE_TO_CSV = new HashMap<>();
    static {
        SOURCE_TO_CSV.put("Matemàtiques", "Matematiques_dificultat.csv"); // Si pone matematicas, se le devuelve/da el valor de matematiques_dificultat.csv ; asi funciona el HashMap @ByGamer01
        SOURCE_TO_CSV.put("Biologia", "Biologia_dificultat.csv"); // .put para poner informacion dentro del HashMap @ByGamer01
        SOURCE_TO_CSV.put("Llengües", "Llengues_dificultat.csv");
        SOURCE_TO_CSV.put("Esports", "Deportes_dificultat.csv");
        SOURCE_TO_CSV.put("Futbolistes", "Futbolistes_dificultat.csv");
        SOURCE_TO_CSV.put("Informàtica", "Informatica_dificultat.csv");
    }

    /**
     * Returns an instance of current class, where only one copy of instance will exist.
     *
     * <p>
     * If no instance found, a new one will be generated and stored. Otherwise, the stored one will be return.
     *
     * @return an instance of current class.
     */
    public static Service getInstance() {
        if (Service.instance == null)
            Service.instance = new Service();
        return Service.instance;
    }

    /**
     * Returns a string representation of initialization results.
     *
     * <p>
     * If the returning String is empty, the initialization process is successfully completed. Otherwise, the
     * returning String will contain error details.
     *
     * @param wordSources a String array containing word sources ordered by difficulty increasingly.
     * @param wordLengths a String array containing word lengths ordered increasingly.
     * @return a string representation of initialization results.
     */
    public String initService(String[] wordSources, String[] wordLengths) {
        final int minLength = Integer.parseInt(wordLengths[0]);
        final int maxLength = Integer.parseInt(wordLengths[wordLengths.length - 1]);
        difficultyByWordSource = new HashMap<>(); //  otro HashMap pero para la dificultad de la palabra @ByGamer01
        for (int i = 0; i < wordSources.length; i++)
            difficultyByWordSource.put(wordSources[i], i + 1);
        indexByLength = new HashMap<>();
        wordByLengthThenDifficulty = new HashMap<>();

        /* Load words from main word source (Trimmed.csv). */
        try {
            String FilePath = findTrimmedCsvPath();
            if (FilePath == null)
                return "No s'ha trobat Trimmed.csv";
            FileReader fileReader = new FileReader(FilePath);
            BufferedReader bufReader = new BufferedReader(fileReader);
            for (String curLine = bufReader.readLine(); curLine != null; curLine = bufReader.readLine()) {
                String[] items = curLine.split(",");
                if (items.length != 2)
                    continue;
                int difficulty;
                try { difficulty = Integer.parseInt(items[1].trim()); } catch (NumberFormatException e) { continue; }
                String word = items[0].toUpperCase();
                int wordLength = word.length();
                if (wordLength < minLength || wordLength > maxLength)
                    continue;
                indexByLength.putIfAbsent(wordLength, new HashMap<>());
                indexByLength.get(wordLength).put(word, difficulty);

                wordByLengthThenDifficulty.putIfAbsent(wordLength, new HashMap<>());
                HashMap<Integer, ArrayList<String>> currentWordByLengthThenDifficulty =
                        wordByLengthThenDifficulty.get(wordLength);
                currentWordByLengthThenDifficulty.putIfAbsent(difficulty, new ArrayList<>());
                currentWordByLengthThenDifficulty.get(difficulty).add(word);
            }
        } catch (Exception e) {
            return e.toString();
        }
        
        // Load optional Catalan trimmed file (Trimmed_ca.csv).
        indexByLengthCatalan = new HashMap<>();
        wordByLengthThenDifficultyCatalan = new HashMap<>();
        try {
            String caPath = findCsvPath("Trimmed_ca.csv");
            if (caPath != null) {
                loadCsvInto(caPath, minLength, maxLength, indexByLengthCatalan, wordByLengthThenDifficultyCatalan);
            }
        } catch (Exception ignored) {
        }

        // Fall back to main Trimmed.csv if no Catalan-specific file was found.
        if (indexByLengthCatalan.isEmpty()) {
            indexByLengthCatalan = indexByLength;
        }
        if (wordByLengthThenDifficultyCatalan.isEmpty()) {
            wordByLengthThenDifficultyCatalan = wordByLengthThenDifficulty;
        }

        // Load thematic CSV files for each word source.
        thematicIndex = new HashMap<>();
        thematicWords = new HashMap<>();
        for (Map.Entry<String, String> entry : SOURCE_TO_CSV.entrySet()) {
            String sourceName = entry.getKey();
            String csvFile = entry.getValue();
            String csvPath = findCsvPath(csvFile);
            if (csvPath != null) {
                HashMap<Integer, HashMap<String, Integer>> idx = new HashMap<>();
                HashMap<Integer, HashMap<Integer, ArrayList<String>>> words = new HashMap<>();
                try {
                    loadCsvInto(csvPath, minLength, maxLength, idx, words);
                    if (!idx.isEmpty()) {
                        thematicIndex.put(sourceName, idx);
                        thematicWords.put(sourceName, words);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        // "Tot" combina Catalan con todas las tematicas
        HashMap<Integer, HashMap<String, Integer>> totIdx = new HashMap<>();
        HashMap<Integer, HashMap<Integer, ArrayList<String>>> totWords = new HashMap<>();
        // Add Catalan words first.
        mergeDictionaries(totIdx, totWords, indexByLengthCatalan, wordByLengthThenDifficultyCatalan);
        // Add all thematic words.
        for (String sourceName : thematicIndex.keySet()) {
            mergeDictionaries(totIdx, totWords, thematicIndex.get(sourceName), thematicWords.get(sourceName));
        }
        if (!totIdx.isEmpty()) {
            thematicIndex.put("Tot", totIdx);
            thematicWords.put("Tot", totWords);
        }

        return "";
    }

    /**
     * Load a CSV file (word,difficulty) into the given index and words maps.
     */
    private void loadCsvInto(String path, int minLength, int maxLength,
                              HashMap<Integer, HashMap<String, Integer>> idx,
                              HashMap<Integer, HashMap<Integer, ArrayList<String>>> words) throws Exception {
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        for (String curLine = br.readLine(); curLine != null; curLine = br.readLine()) {
            String[] items = curLine.split(",");
            if (items.length != 2) continue;
            int difficulty;
            try { difficulty = Integer.parseInt(items[1].trim()); } catch (NumberFormatException e) { continue; }
            String word = items[0].toUpperCase();
            int wordLength = word.length();
            if (wordLength < minLength || wordLength > maxLength) continue;
            idx.putIfAbsent(wordLength, new HashMap<>());
            idx.get(wordLength).put(word, difficulty);
            words.putIfAbsent(wordLength, new HashMap<>());
            HashMap<Integer, ArrayList<String>> m = words.get(wordLength);
            m.putIfAbsent(difficulty, new ArrayList<>());
            m.get(difficulty).add(word);
        }
        br.close();
    }

    /**
     * Merge source dictionaries into target dictionaries (avoids duplicates by word).
     */
    private void mergeDictionaries(HashMap<Integer, HashMap<String, Integer>> targetIdx,
                                    HashMap<Integer, HashMap<Integer, ArrayList<String>>> targetWords,
                                    HashMap<Integer, HashMap<String, Integer>> sourceIdx,
                                    HashMap<Integer, HashMap<Integer, ArrayList<String>>> sourceWords) {
        for (Map.Entry<Integer, HashMap<String, Integer>> lengthEntry : sourceIdx.entrySet()) {
            int len = lengthEntry.getKey();
            targetIdx.putIfAbsent(len, new HashMap<>());
            for (Map.Entry<String, Integer> wordEntry : lengthEntry.getValue().entrySet()) {
                if (!targetIdx.get(len).containsKey(wordEntry.getKey())) {
                    targetIdx.get(len).put(wordEntry.getKey(), wordEntry.getValue());
                    int diff = wordEntry.getValue();
                    targetWords.putIfAbsent(len, new HashMap<>());
                    targetWords.get(len).putIfAbsent(diff, new ArrayList<>());
                    targetWords.get(len).get(diff).add(wordEntry.getKey());
                }
            }
        }
    }

    /**
     * Try to locate a CSV file by checking common relative paths.
     */
    private String findCsvPath(String filename) {
        String[] candidates = new String[]{
            "./Word Sources/" + filename,
            "./src/Word Sources/" + filename
        };
        for (String c : candidates)
            if (new File(c).exists())
                return c;
        // Shallow recursive search
        try (Stream<Path> stream = Files.walk(Paths.get("."), 5)) {
            Path found = stream.filter(p -> p.getFileName().toString().equalsIgnoreCase(filename)).findFirst().orElse(null);
            if (found != null)
                return found.toFile().getPath();
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * Try to locate the Trimmed.csv file by checking common relative paths and
     * performing a shallow search (max depth 5) from current working directory.
     *
     * @return the path to Trimmed.csv or null if not found
     */
    private String findTrimmedCsvPath() {
        return findCsvPath("Trimmed.csv"); // Este es el metodo que busca el camino del archivo csv de nuestras palabras @ByGamer01
    }

    /**
     * Returns the appropriate index map for a given word source.
     */
    private HashMap<Integer, HashMap<String, Integer>> getIndexForSource(String wordSource) {
        if ("Català".equals(wordSource)) return indexByLengthCatalan;
        if (thematicIndex.containsKey(wordSource)) return thematicIndex.get(wordSource);
        return indexByLength;
    }

    /**
     * Returns the appropriate words map for a given word source.
     */
    private HashMap<Integer, HashMap<Integer, ArrayList<String>>> getWordsForSource(String wordSource) {
        if ("Català".equals(wordSource)) return wordByLengthThenDifficultyCatalan;
        if (thematicWords.containsKey(wordSource)) return thematicWords.get(wordSource);
        return wordByLengthThenDifficulty;
    }

    /**
     * Returns a string representation of checking results.
     *
     * <p>
     * If the returning String is empty, the word given is valid. Otherwise, the
     * returning String will contain error details.
     *
     * <p>
     * If the word entered is empty, the return will also be empty as it's a representation of a random word from the
     * word source.
     *
     * @param word       an uppercase String to be checked in the difficulty level of <var>wordSource</var>.
     * @param wordSource a String representing the difficulty level of current setting.
     * @return a string representation of checking results.
     */
    public String checkExistence(String word, String wordSource) {
        if (word.length() == 0) return "";
        HashMap<Integer, HashMap<String, Integer>> idx = getIndexForSource(wordSource);
        int length = word.length();
        if (idx == null || !idx.containsKey(length) || !idx.get(length).containsKey(word))
            return "No trobat";
        // For thematic sources, all difficulties are valid.
        if (thematicIndex.containsKey(wordSource) || "Català".equals(wordSource))
            return "";
        int difficulty = difficultyByWordSource.get(wordSource);
        if (idx.get(length).get(word) > difficulty)
            return "La paraula és massa difícil";
        return "";
    }

    /**
     * Returns a random word with {@code O(1)} time complexity under given restrictions.
     *
     * <p>
     * If the returning String is not {@code "No trobat"}, the word returns is valid.
     *
     * @param wordLength an int describing the length restriction.
     * @param wordSource a String representing the difficulty level of current setting.
     * @return a random word or {@code "No trobat"} under given conditions.
     */
    public String generateRandomWord(int wordLength, String wordSource) {
        HashMap<Integer, HashMap<Integer, ArrayList<String>>> wordsMap = getWordsForSource(wordSource);
        if (wordsMap == null) return "No trobat";
        HashMap<Integer, ArrayList<String>> wordByDifficulty = wordsMap.get(wordLength);
        if (wordByDifficulty == null) return "No trobat";

        // For thematic sources and Català, use all difficulty levels available.
        int maxDifficulty;
        if (thematicWords.containsKey(wordSource) || "Català".equals(wordSource)) {
            maxDifficulty = 3; // Max difficulty in CSVs
        } else {
            maxDifficulty = difficultyByWordSource.get(wordSource);
        }

        int total = 0;
        for (int i = 1; i <= maxDifficulty; i++)
            if (wordByDifficulty.containsKey(i))
                total += wordByDifficulty.get(i).size();
        if (total == 0) return "No trobat";
        int randomIndex = new Random().nextInt(total);
        for (int i = 1; i <= maxDifficulty; i++)
            if (wordByDifficulty.containsKey(i)) {
                int size = wordByDifficulty.get(i).size();
                if (randomIndex < size)
                    return wordByDifficulty.get(i).get(randomIndex);
                randomIndex -= size;
            }
        return "No trobat";
    }

    /**
     * This method returns the result of helper input checking and matched results. Word length and word source
     * configuration is attained directly from {@code Settings}.
     *
     * @param helperInput a String describing the input from the helper input text field.
     * @return a String containing error reason, which will be empty if no error found, and matched results. The error
     * reason and matched results are separated by "$".
     */
    public String validateHelperInput(String helperInput) {
        // Initialize variables.
        helperInput = helperInput.toUpperCase();
        boolean isInsideRoundBracket = false;
        boolean isInsideSquareBracket = false;
        boolean isContainedRoundBracket = false;
        HashMap<Character, Integer> mustExistCount = new HashMap<>();
        HashSet<Character> mustNotExist = new HashSet<>();
        boolean eligibilityMatchAll = false;
        StringBuilder patternString = new StringBuilder();
        // Scan and check the input string.
        for (int i = 0; i < helperInput.length(); i++) {
            char ch = helperInput.charAt(i);
            if (ch == '(') {
                isContainedRoundBracket = true;
                if (isInsideRoundBracket || isInsideSquareBracket)
                    return "Claudàtors niuats no suportats$";
                else
                    isInsideRoundBracket = true;
            } else if (ch == ')') {
                if (isInsideRoundBracket)
                    isInsideRoundBracket = false;
                else
                    return "Claudàtor sense parella$";
            } else if (ch == '[') {
                if (isInsideSquareBracket || isInsideRoundBracket)
                    return "Claudàtors niuats no suportats$";
                else
                    isInsideSquareBracket = true;
            } else if (ch == ']') {
                if (isInsideSquareBracket)
                    isInsideSquareBracket = false;
                else
                    return "Claudàtor sense parella$";
            } else if (Character.isAlphabetic(ch)) {
                if (isInsideRoundBracket)
                    mustExistCount.put(ch, mustExistCount.getOrDefault(ch, 0) + 1);
                else if (isInsideSquareBracket)
                    mustNotExist.add(ch);
                else
                    patternString.append(ch);
            } else if (ch == '*') {
                if (isInsideRoundBracket)
                    eligibilityMatchAll = true;
                else if (isInsideSquareBracket)
                    return "* dins de [] no permès$";
                else
                    patternString.append(ch);
            } else
                return "Entrada no vàlida$";
        }
        final int initWordLength = Settings.getInitWord().length();
        if (patternString.length() != initWordLength)
            return "Longitud massa " + (patternString.length() < initWordLength ? "curta" : "llarga") + "$";
        if (isInsideRoundBracket || isInsideSquareBracket)
            return "Claudàtor sense parella$";
        // Scan the database to filter out valid candidate words.
        if (!isContainedRoundBracket)
            eligibilityMatchAll = true;

        HashMap<Integer, HashMap<Integer, ArrayList<String>>> wordsMap = getWordsForSource(Settings.getWordSource());
        if (wordsMap == null)
            return "$" + ("S'han trobat 0 resultat(s).") + "\n";
        HashMap<Integer, ArrayList<String>> wordByDifficulty = wordsMap.get(initWordLength);
        if (wordByDifficulty == null)
            return "$" + ("S'han trobat 0 resultat(s).") + "\n";

        // For thematic sources, use all difficulty levels.
        int difficultyLevel;
        if (thematicWords.containsKey(Settings.getWordSource()) || "Català".equals(Settings.getWordSource())) {
            difficultyLevel = 3;
        } else {
            difficultyLevel = difficultyByWordSource.get(Settings.getWordSource());
        }

        StringBuilder results = new StringBuilder();
        int candidateCount = 0;
        for (int currentDifficulty = 1; currentDifficulty <= difficultyLevel; currentDifficulty++) {
            ArrayList<String> currentWordList = wordByDifficulty.get(currentDifficulty);
            if (currentWordList == null) continue;
            for (String word : currentWordList) {
                boolean ok = true;
                HashMap<Character, Integer> existCount = new HashMap<>();
                for (int i = 0; i < initWordLength; i++) {
                    char ch = word.charAt(i);
                    if (ch != patternString.charAt(i)) {
                        if (patternString.charAt(i) != '*') {
                            ok = false;
                            break;
                        }
                        else if (mustNotExist.contains(ch)) {
                            ok = false;
                            break;
                        } else if (existCount.getOrDefault(ch, 0) <
                                mustExistCount.getOrDefault(ch, 0)) {
                            existCount.put(ch, existCount.getOrDefault(ch, 0) + 1);
                        } else if (!eligibilityMatchAll) {
                            ok = false;
                            break;
                        }
                    }
                }
                // Check must exist characters validity.
                for (Map.Entry<Character, Integer> pair : mustExistCount.entrySet()) {
                    if (existCount.getOrDefault(pair.getKey(), 0) < pair.getValue()) {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    ++candidateCount;
                    results.append(word).append("\n");
                }
            }
        }
        return "$" + ("S'han trobat " + candidateCount + " resultat(s)" + (candidateCount > 0 ? ":" : ".")) + "\n" + results;
    }
}