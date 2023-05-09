package searchengine.services;    /*
 *created by WerWolfe on ExtractText
 */

import lombok.Getter;
import lombok.NonNull;
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
    private static String regexNonWord = ".*(СОЮЗ|МЕЖД|ПРЕДЛ|ЧАСТ|ПРЕДК)\s?.*|.*\sМС(-|\s).*";
    private static String regexSplit = "[\s\r\n]+";
    private static final String regexDelEndWord = "(а|я|о|е|ь|ы|и|а|ая|ое|ой|ые|ие|ый|ий|ать|ять|оть|еть|уть|у|ю|ем|им|ешь|ишь|ете|ите|ет|ит|ут|ют|ят|ал|ял|ала|яла|али|яли|ол|ел|ола|ела|оли|ели|ул|ула|ули)$";
    private static LuceneMorphology luceneMorph;
    private static HashSet<String> nonCheckWord;

    public synchronized static void init() throws IOException {
        if (luceneMorph == null) {
            luceneMorph = new RussianLuceneMorphology();
            nonCheckWord = new HashSet<>();
        }
    }

    public static String clearText(@NonNull String string) {
        String text = string.replaceAll(regexHTMLTag, " ");
        return text.replaceAll(regexNonWordCharacter, " ").trim();
    }

    public static String[] getStrings(@NonNull String string) {
        return string.trim().split(regexSplit);
    }

    public static HashMap<String, Integer> getWords(@NonNull String text) {

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
            String baseForms = wordBaseForms.size() > 0 ? wordBaseForms.get(0) : "";
            words.put(baseForms, words.getOrDefault(baseForms, 0) + 1);
        }
        return words;
    }

    public static String delEndWord(String word) {
        return word.replaceAll(regexDelEndWord, "");
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
