package searchengine.services.dataservice;    /*
 *created by WerWolfe on IndexDataService
 */

import searchengine.dto.RepositoryCollector;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repositories.IndexRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class IndexDataService {

    public synchronized static void SaveAll(HashMap<Lemma, Integer> lemmas, Page page) {
        List<Index> indexList = getIndexList(lemmas, page);
        RepositoryCollector.getIndexRepository().saveAllAndFlush(indexList);
    }

    public synchronized static void deleteByPageIn(List<Page> pages) {
        IndexRepository indexRepository = RepositoryCollector.getIndexRepository();
        if (pages.size() > 0) {
            indexRepository.deleteByPageIn(pages);
        }
    }

    private static List<Index> getIndexList(HashMap<Lemma, Integer> lemmas, Page page) {
        List<Index> indexMap = new ArrayList<>();
        for (Lemma lemma : lemmas.keySet()) {
            indexMap.add(new Index(page, lemma, (float) lemmas.get(lemma)));
        }
        return indexMap;
    }
}
