package searchengine.services;

import junit.framework.TestCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;

/*
 *created by WerWolfe on
 */class ExtractTextTest extends TestCase {

    private final String separator = System.lineSeparator();

    @Test
    @DisplayName("Проверяем удаление цифр, знаков препинания, латинских букв")
    void clearTextSymbol() {
        String nonWordSymbol = "Ник пользователя: '56_Ark, а?'. Хотя он уже 5 раз повторяется";
        String clear = ExtractText.clearText(nonWordSymbol);
        assertEquals(clear, "Ник пользователя           а    Хотя он уже   раз повторяется");
    }

    @Test
    @DisplayName("Получаем текст без HTML тегов")
    void clearTextHTMLTags() {
        String nonWordSymbol = "<div class=\"chatMessageContainer\">\n" +
                "\t<div class=\"chatMessage aiResponse  \">\n" +
                "\t\t<div class=\"messageContainer \">\n" +
                "\t\t\t<div class=\"messageContent\">\n" +
                "\t\t\t<pre class=\"plainText\">Ник пользователя: '56_Ark, а?'.</b>Хотя он уже 5 раз повторяется</pre>\n" +
                "\t\t\t</div>\n" +
                "\t\t</div>\n" +
                "\t</div>\n" +
                "</div>";
        String clear = ExtractText.clearText(nonWordSymbol);
        assertEquals(clear, "Ник пользователя           а    Хотя он уже   раз повторяется");
    }

    @Test
    @DisplayName("Передаем пустую строку")
    void clearTextIsEmpty() {
        String clear = ExtractText.clearText("");
        assertEquals(clear, "");
    }

    @Test
    @DisplayName("Передаем число")
    void clearTextNumber() {
        String clear = ExtractText.clearText("123456");
        assertEquals(clear, "");
    }

    @Test
    @DisplayName("Передаем спецсимволы")
    void clearTextSpecSymbol() {
        String clear = ExtractText.clearText("@+//_*");
        assertEquals(clear, "");
    }

    @Test
    @DisplayName("Получаем массив из пустой строки")
    void getStringsIsEmpty() {
        String[] words = ExtractText.getStrings("");
        assertEquals(Arrays.toString(words), Arrays.toString(getSampleArrayIsEmpty()));
    }

    @Test
    @DisplayName("Получаем массив из набора пустых строк")
    void getStringsIsEmptyStrings() {
        String[] words = ExtractText.getStrings("" + separator + "" + separator);
        assertEquals(Arrays.toString(words), Arrays.toString(getSampleArrayIsEmpty()));
    }

    @Test
    @DisplayName("Получаем массив из набора спец символов и чисел и строк разделенных пробелом")
    void getStringsSpecSymbolAndNumberSeparatedSpace() {
        String[] words = ExtractText.getStrings("1234 #@$!%^ фгфг tututu");
        assertEquals(Arrays.toString(words), Arrays.toString(getSampleArray()));
    }

    @Test
    @DisplayName("Получаем массив из набора спец символов и чисел и строк разделенных переносом строки")
    void getStringsSpecSymbolAndNumberSeparated() {
        String[] words = ExtractText.getStrings("1234 " + separator + " #@$!%^" + separator + " фгфг" + separator + " tututu");
        assertEquals(Arrays.toString(words), Arrays.toString(getSampleArray()));
    }

    @Test
    @DisplayName("Получаем массив из набора спец символов и чисел и строк разделенных длинным пробелом")
    void getStringsSpecSymbolAndNumberSeparatedLongSpace() {
        String[] sample = new String[2];
        sample[0] = "1234";
        sample[1] = "#@$!%^";
        String[] words = ExtractText.getStrings("1234        #@$!%^");
        assertEquals(Arrays.toString(words), Arrays.toString(sample));
    }
//
//    @Test
//    @DisplayName("Получаем массив из Null")
//    void getStringsIsNull() {
//        String[] sample = new String[1];
//        sample[0] = "";
//        String[] words = ExtractText.getStrings(a);
//        assertEquals(Arrays.toString(words), Arrays.toString(sample));
//    }

    @Test
    @DisplayName("Получаем список лемм из пустой строки")
    void getWordsIsEmpty() {
        HashMap<String, Integer> words = ExtractText.getWords("");
        assertTrue(words.isEmpty());
    }

    @Test
    @DisplayName("Получаем список лемм из текста")
    void getWords() {
        HashMap<String, Integer> words = ExtractText.getWords("Повторное появление леопарда в Осетии позволяет " +
                "предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа");
        assertEquals(words.toString(), getSampleLemmas().toString());
    }

    @Test
    @DisplayName("Получаем список лемм из текста разделенных спец символами")
    void getWordsSeparatedSpecSymbol() {
        HashMap<String, Integer> words = ExtractText.getWords("Повторное;появление(леопарда)в Осетии/позволяет " +
                "предположить,что_леопард-постоянно обитает в некоторых районах Северного Кавказа");
        assertEquals(words.toString(), getSampleLemmas().toString());
    }

    @Test
    @DisplayName("Получаем список лемм из текста разделенных переносом строки")
    void getWordsSeparated() {
        HashMap<String, Integer> words = ExtractText.getWords("Повторное появление леопарда в Осетии позволяет " +
                separator + "предположить, что леопард постоянно обитает " +
                separator + "в некоторых районах Северного Кавказа");
        assertEquals(words.toString(), getSampleLemmas().toString());
    }

    @Test
    @DisplayName("Проверем фильтрацию служебных частей речи")
    void getWordsNonWord() {
        HashMap<String, Integer> words = ExtractText.getWords("Мне бы вот так как бы эх да Я бы ох когда-то");
        assertTrue(words.isEmpty());
    }

    @Test
    @DisplayName("Проверем фильтрацию символов не относящиеся к кириллице")
    void getWordsNonСyrillic() {
        HashMap<String, Integer> words = ExtractText.getWords("The reappearance of the leopard in Ossetia " +
                "suggests that the leopard permanently inhabits some areas of the North Caucasus in 1988");
        assertTrue(words.isEmpty());
    }

    private String[] getSampleArray() {
        String[] sample = new String[4];
        sample[0] = "1234";
        sample[1] = "#@$!%^";
        sample[2] = "фгфг";
        sample[3] = "tututu";
        return sample;
    }

    private String[] getSampleArrayIsEmpty() {
        String[] sample = new String[1];
        sample[0] = "";
        return sample;
    }

    private HashMap<String, Integer> getSampleLemmas() {
        HashMap<String, Integer> words = new HashMap<>();
        words.put("повторный", 1);
        words.put("появление", 1);
        words.put("осетия", 1);
        words.put("постоянно", 1);
        words.put("позволять", 1);
        words.put("предположить", 1);
        words.put("северный", 1);
        words.put("район", 1);
        words.put("кавказ", 1);
        words.put("леопард", 2);
        words.put("обитать", 1);
        return words;
    }
}