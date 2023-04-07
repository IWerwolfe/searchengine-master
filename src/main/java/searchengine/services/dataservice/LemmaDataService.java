package searchengine.services.dataservice;    /*
 *created by WerWolfe on LemmaService
 */

import searchengine.dto.RepositoryCollector;
import searchengine.model.Lemma;
import searchengine.model.Site;
import searchengine.repositories.LemmaRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public abstract class LemmaDataService {

    public synchronized static void deleteBySite(Site site) {
        RepositoryCollector.getLemmaRepository().deleteBySite(site);
    }

    public static HashMap<Lemma, Integer> saveLemmas(HashMap<String, Integer> words, Site site) {
        HashMap<Lemma, Integer> lemmaList = new HashMap<>();
        if (words.isEmpty()) {
            return lemmaList;
        }
        for (String word : words.keySet()) {
            int count = words.get(word);
            lemmaList.put(updateOrSaveLemma(site, word, count), count);
        }
        return lemmaList;
    }

    public synchronized static Lemma updateOrSaveLemma(Site site, String word, Integer count) {
        LemmaRepository repository = RepositoryCollector.getLemmaRepository();
        Optional<Lemma> optional = repository.findBySiteAndLemma(site, word);
        Lemma lemma = null;
        if (optional.isPresent()) {
            lemma = optional.get();
            repository.updateFrequencyById(count + lemma.getFrequency(), lemma.getId());
        } else {
            lemma = new Lemma(site, word, count);
            repository.save(lemma);
        }
        return lemma;
    }
}
