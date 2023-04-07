package searchengine.services;    /*
 *created by WerWolfe on ExtractText
 */

import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Getter
@Setter
public class ExtractText {

    private static String regexHTMLTag = "(\\<.*?\\>)";
    private static String regexNonWordCharacter = "[^а-яА-ЯёЁ\s]";
    private static String regexNonWord = ".*(СОЮЗ|МЕЖД|ПРЕДЛ|ЧАСТ)|.*\sМС(-|\s).*";
    private static String regexSplit = "\s+";
    private static LuceneMorphology luceneMorph;
    private static HashSet<String> nonCheckWord;

    public synchronized static void init() throws IOException {
        if (luceneMorph == null) {
            luceneMorph = new RussianLuceneMorphology();
            nonCheckWord = new HashSet<>();
        }
    }

    public static String clearText(String string) {
        String text = string.replaceAll(regexHTMLTag, " ");
        return text.replaceAll(regexNonWordCharacter, " ");
    }

    public static String[] getStrings(String string) {
        return string.split(regexSplit);
    }

    public static HashMap<String, Integer> getWords(String text) {

        if (luceneMorph == null) {
            try {
                init();
            } catch (IOException e) {
                return new HashMap<>();
            }
        }

        String[] strings = getStrings(clearText(text));
        HashMap<String, Integer> words = new HashMap<>();

        for (String word : strings) {
            String lowWord = word.toLowerCase();
            if (!checkWord(lowWord)) {
                continue;
            }
            List<String> wordBaseForms = luceneMorph.getNormalForms(lowWord);
            wordBaseForms.forEach(w -> words.put(w, words.getOrDefault(w, 0) + 1));
        }
        return words;
    }

    private static boolean checkWord(String word) {
        if (word.isEmpty()) {
            return false;
        }
        if (nonCheckWord.contains(word)) {
            return false;
        }
        List<String> wordForms = luceneMorph.getMorphInfo(word);
        for (String checkWord : wordForms) {
            if (isNonWord(checkWord)) {
                nonCheckWord.add(word);
                return false;
            }
        }
        return true;
    }

    private static boolean isNonWord(String word) {
        return word.matches(regexNonWord);
    }

}
