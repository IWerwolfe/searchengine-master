package searchengine;    /*
 *created by WerWolfe on Main
 */

import searchengine.services.ExtractText;
import java.io.IOException;


public class Main {

    public static void main(String[] args) throws IOException {
        ExtractText.getWords(
                "Повторное появление леопарда в Осетии позволяет предположить, что леопард постоянно обитает в некоторых районах Северного Кавказа."
        );
    }
}
