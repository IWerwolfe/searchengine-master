package searchengine.services.dataservice;    /*
 *created by WerWolfe on LemmaService
 */

import searchengine.dto.RepositoryCollector;
import searchengine.model.Lemma;
import searchengine.model.Site;
import searchengine.repositories.LemmaRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public abstract class LemmaDataService {

    private static LemmaRepository getRepository() {
        return RepositoryCollector.getLemmaRepository();
    }

    public static long getCountBySite(Site site) {
        return getRepository().countBySite(site);
    }

    public static void deleteBySite(Site site) {
        synchronized (site) {
            getRepository().deleteBySite(site);
        }
    }

    public static List<Lemma> getAllLemma(Collection<String> words) {
        return getRepository().findByLemmaInOrderByFrequencyAsc(words);
    }

    public static List<Lemma> getAllLemma(Collection<String> words, String sitePath) {
        return getRepository().findByLemmaInAndSite_UrlIgnoreCaseOrderByFrequencyAsc(words, sitePath);
    }

    public static List<Lemma> getAlLLemmaExcludingFrequent(Collection<String> words, String sitePath) {
        List<Lemma> lemmaList = sitePath.equals("all") ? getAllLemma(words) : getAllLemma(words, sitePath);
        return clearManyFrequent(lemmaList);
    }

    public static HashMap<Lemma, Integer> saveAllLemma(HashMap<String, Integer> words, Site site) {

        HashMap<String, Lemma> toSave = new HashMap<>();
        HashMap<Lemma, Integer> lemmaList = new HashMap<>();

        if (words.isEmpty()) {
            return lemmaList;
        }

        synchronized (site) {

            List<Lemma> lemmasToBD = getRepository().findBySiteAndLemmaIn(site, words.keySet());
            lemmasToBD.forEach(l -> toSave.put(l.getLemma(), l));

            for (String word : words.keySet()) {
                Lemma lemma = createOrUpdateLemmaFrequency(toSave, word, site);
                lemmaList.put(lemma, words.get(word));
            }
        }

        getRepository().saveAll(toSave.values());
        return lemmaList;
    }

    private static Lemma createOrUpdateLemmaFrequency(HashMap<String, Lemma> toSave, String word, Site site) {
        Lemma lemma = toSave.get(word);
        if (lemma == null) {
            lemma = new Lemma(site, word, 1);
            toSave.put(word, lemma);
        } else {
            lemma.setFrequency(lemma.getFrequency() + 1);
        }
        return lemma;
    }

    private static List<Lemma> clearManyFrequent(List<Lemma> lemmaList) {
        float count = PageDataService.getCount();
        List<Lemma> newLemmaList = new ArrayList<>();

        for (Lemma lemma : lemmaList) {
            if (lemma.getFrequency() / count < 0.7) {
                newLemmaList.add(lemma);
            } else {
                System.out.println("Remove: " + lemma.getLemma());
            }
        }
        return newLemmaList;
    }
}
