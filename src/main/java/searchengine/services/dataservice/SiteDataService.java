package searchengine.services.dataservice;    /*
 *created by WerWolfe on SiteDataService
 */

import searchengine.dto.RepositoryCollector;
import searchengine.dto.index.HTTPResponse;
import searchengine.model.IndexStatus;
import searchengine.model.Site;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public abstract class SiteDataService {

    public synchronized static void deleteSiteAndRelatedData(String url) {
        SiteRepository siteRepository = RepositoryCollector.getSiteRepository();
        Optional<Site> optionalSite = siteRepository.findByUrlIgnoreCase(url);
        if (optionalSite.isPresent()) {
            Site site = optionalSite.get();
            LemmaDataService.deleteBySite(site);
            PageDataService.deletePageAllAndRelatedDataBySite(site);
            siteRepository.delete(site);
        }
    }

    public synchronized static void updateSite(searchengine.model.Site site, IndexStatus status, String error) {
        SiteRepository repository = RepositoryCollector.getSiteRepository();
        repository.updateStatusAndStatusTimeAndLastErrorById(status, LocalDateTime.now(), error, site.getId());
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
