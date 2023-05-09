package searchengine.services.dataservice;    /*
 *created by WerWolfe on IndexDataService
 */

import searchengine.dto.RepositoryCollector;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repositories.IndexRepository;

import java.util.*;

public abstract class IndexDataService {

    private static IndexRepository getRepository() {
        return RepositoryCollector.getIndexRepository();
    }

    public static void saveAll(HashMap<Lemma, Integer> lemmas, Page page) {
        synchronized (page) {
            Set<Index> indexList = getIndexList(lemmas, page);
            saveAll(indexList);
        }
    }

    public static void saveAll(Set<Index> indexList) {
        getRepository().saveAll(indexList);
    }

    private static Set<Index> getIndexList(HashMap<Lemma, Integer> lemmas, Page page) {
        Set<Index> indexMap = new HashSet<>();
        for (Lemma lemma : lemmas.keySet()) {
            indexMap.add(new Index(page, lemma, (float) lemmas.get(lemma)));
        }
        return indexMap;
    }

    public synchronized static void deleteByPageInAllIgnoreCase(List<Page> pageList) {
        getRepository().deleteByPageInAllIgnoreCase(pageList);
    }

    public static List<Index> findIndexListByLemmaList(Collection<Lemma> LemmaList) {
        return getRepository().findByLemmaInOrderByPageAscRankDesc(LemmaList);
    }
}
