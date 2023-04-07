package searchengine.dto;    /*
 *created by WerWolfe on RepositoryCollector
 */

import lombok.Data;
import searchengine.repositories.*;
@Data
public abstract class RepositoryCollector {

    private static LemmaRepository lemmaRepository;
    private static PageRepository pageRepository;
    private static SiteRepository siteRepository;
    private static IndexRepository indexRepository;

    public static IndexRepository getIndexRepository() {
        return indexRepository;
    }

    public static void setIndexRepository(IndexRepository indexRepository) {
        RepositoryCollector.indexRepository = indexRepository;
    }

    public static LemmaRepository getLemmaRepository() {
        return lemmaRepository;
    }

    public static void setLemmaRepository(LemmaRepository lemmaRepository) {
        RepositoryCollector.lemmaRepository = lemmaRepository;
    }

    public static PageRepository getPageRepository() {
        return pageRepository;
    }

    public static void setPageRepository(PageRepository pageRepository) {
        RepositoryCollector.pageRepository = pageRepository;
    }

    public static SiteRepository getSiteRepository() {
        return siteRepository;
    }

    public static void setSiteRepository(SiteRepository siteRepository) {
        RepositoryCollector.siteRepository = siteRepository;
    }
}
