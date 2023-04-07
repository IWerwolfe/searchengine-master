package searchengine.services.dataservice;    /*
 *created by WerWolfe on PageDataService
 */

import searchengine.dto.RepositoryCollector;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

public abstract class PageDataService {

    public synchronized static void deletePageAllAndRelatedDataBySite(Site site) {
        List<Page> pages = RepositoryCollector.getPageRepository().removeBySite(site);
        IndexDataService.deleteByPageIn(pages);
    }

    public synchronized static void deletePageAllAndRelatedDataByPage(Page page) {
        List<Page> pages = RepositoryCollector.getPageRepository().removeById(page.getId());
        IndexDataService.deleteByPageIn(pages);
    }

    public synchronized static void deletePageAllAndRelatedDataByUrl(String url) {
        List<Page> pages = RepositoryCollector.getPageRepository().removeByPathIgnoreCase(url);
        IndexDataService.deleteByPageIn(pages);
    }

    public synchronized static void save(Page page) {
        if (page != null) {
            RepositoryCollector.getPageRepository().save(page);
        }
    }
}
