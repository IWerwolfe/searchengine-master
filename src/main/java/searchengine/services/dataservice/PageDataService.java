package searchengine.services.dataservice;    /*
 *created by WerWolfe on PageDataService
 */

import lombok.NonNull;
import org.jsoup.Jsoup;
import searchengine.dto.RepositoryCollector;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.services.ExtractText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class PageDataService {

    private static PageRepository getRepository() {
        return RepositoryCollector.getPageRepository();
    }

    public static long getCountBySite(Site site) {
        return getRepository().countBySite(site);
    }

    public static long getCount() {
        return getRepository().countFirstBy();
    }

    public synchronized static void saveAll(List<Page> pages) {
        getRepository().saveAllAndFlush(pages);
    }

    public static void update(Page page) {
        synchronized (page) {
            getRepository().updateCodeAndContentById(page.getCode(), page.getContent(), page.getId());
        }
    }

    public static void savePageAndRelatedData(Page page) {
        save(page);
        saveRelatedData(page);
    }

    public static void saveRelatedData(Page page) {
        String text = Jsoup.parse(page.getContent()).text();
        HashMap<String, Integer> words = ExtractText.getWords(text);
        HashMap<Lemma, Integer> lemmaList = LemmaDataService.saveAllLemma(words, page.getSite());
        IndexDataService.saveAll(lemmaList, page);
    }

    public static void deletePageAllAndRelatedDataBySite(Site site) {
        synchronized (site) {
            List<Page> pageList = getRepository().findBySite(site);
            deleteRelatedData(pageList);
        }
    }

    public static void deletePageAndRelatedDataByPage(Page page) {
        synchronized (page) {
            List<Page> pageList = getRepository().findBySiteAndPathIgnoreCase(page.getSite(), page.getPath());
            deleteRelatedData(pageList);
        }
    }

    public static void save(@NonNull Page page) {
        synchronized (page) {
            getRepository().save(page);
        }
    }

    private static void deleteRelatedData(List<Page> pageList) {
        if (pageList.isEmpty()) {
            return;
        }
        IndexDataService.deleteByPageInAllIgnoreCase(pageList);
        getRepository().deleteByIdIn(getIdList(pageList));
    }

    private static List<Integer> getIdList(List<Page> pageList) {
        List<Integer> idList = new ArrayList<>();
        pageList.forEach(page -> idList.add(page.getId()));
        return idList;
    }
}
