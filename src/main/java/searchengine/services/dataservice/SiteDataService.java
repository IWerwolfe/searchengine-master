package searchengine.services.dataservice;    /*
 *created by WerWolfe on SiteDataService
 */

import searchengine.dto.RepositoryCollector;
import searchengine.dto.index.HTTPResponse;
import searchengine.model.IndexStatus;
import searchengine.model.Site;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public abstract class SiteDataService {

    private static SiteRepository getRepository() {
        return RepositoryCollector.getSiteRepository();
    }

    public static List<Site> getAllSite() {
        return getRepository().findAll();
    }

    public static void deleteSiteAndRelatedData(String url) {
        Optional<Site> optionalSite = getRepository().findByUrlIgnoreCase(url);
        if (optionalSite.isPresent()) {
            Site site = optionalSite.get();
            PageDataService.deletePageAllAndRelatedDataBySite(site);
            LemmaDataService.deleteBySite(site);
            synchronized (site) {
                getRepository().delete(site);
            }
        }
    }

    public static void updateSite(searchengine.model.Site site, IndexStatus status, String error) {
        synchronized (site) {
            getRepository().updateStatusAndStatusTimeAndLastErrorById(status, LocalDateTime.now(), error, site.getId());
        }
    }

    public static void updateSite(searchengine.model.Site site, HTTPResponse result) {
        updateSite(site, result.isResult() ? IndexStatus.INDEXED : IndexStatus.FAILED, result.getError());
    }

    public static void updateSite(searchengine.model.Site site, Exception result) {
        updateSite(site, IndexStatus.FAILED, result.getMessage());
    }

    public static void updateSite(searchengine.model.Site site) {
        updateSite(site, IndexStatus.INDEXING, "");
    }
}
