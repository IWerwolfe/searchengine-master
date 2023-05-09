package searchengine.services;    /*
 *created by WerWolfe on IndexServiceImpl
 */

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.RepositoryCollector;
import searchengine.dto.index.HTTPResponse;
import searchengine.dto.index.IndexResponse;
import searchengine.model.IndexStatus;
import searchengine.services.dataservice.SiteDataService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {

    private final SitesList sitesList;
    private ForkJoinPool pool;
    private Map<searchengine.model.Site, ForkJoinTask<HTTPResponse>> tasks;
    private boolean isIndexing;
    private final long delay = 5000L;
    private ScheduledExecutorService service;
    private boolean isStarted;

    @Override
    public IndexResponse startIndexing() {

        if (isIndexing) {
            return new IndexResponse(false, "Индексация уже запущена");
        }

        if (sitesList == null) {
            return new IndexResponse(false, "Индексация не запущена");
        }

        init();
        isIndexing = true;
        service.scheduleAtFixedRate(this::checkTaskList
                , delay * 2
                , delay
                , TimeUnit.MILLISECONDS);

        parsingSite(sitesList.getSites());
        return new IndexResponse(true, "");
    }

    @Override
    public IndexResponse stopIndexing() {

        if (!isIndexing) {
            return new IndexResponse(false, "Индексация не запущена");
        }
        SiteParser.stop();
        isIndexing = false;
        return new IndexResponse(true, "");
    }

    private void init() {
        if (isStarted) {
            System.out.println("Инициализация пропущена");
            return;
        }
        pool = new ForkJoinPool();
        tasks = new HashMap<>();
        service = Executors.newSingleThreadScheduledExecutor();
        isStarted = true;
    }

    private searchengine.model.Site getDTOSite(Site site) {
        searchengine.model.Site dtoSite = new searchengine.model.Site();
        dtoSite.setName(site.getName());
        dtoSite.setUrl(site.getUrl());
        dtoSite.setStatus(IndexStatus.INDEXING);
        dtoSite.setStatusTime(LocalDateTime.now());
        return dtoSite;
    }

    private void parsingSite(List<Site> siteList) {

        for (Site site : siteList) {
            SiteDataService.deleteSiteAndRelatedData(site.getUrl());
            searchengine.model.Site dtoSite = getDTOSite(site);
            RepositoryCollector.getSiteRepository().save(dtoSite);

            try {
                SiteParser siteParser = new SiteParser(dtoSite);
                tasks.put(dtoSite, pool.submit(siteParser));
            } catch (Exception e) {
                SiteDataService.updateSite(dtoSite, e);
            }
        }
    }

    private void checkTaskList() {

        if (isCompleteTask()) {
            return;
        }

        for (searchengine.model.Site site : tasks.keySet()) {

            ForkJoinTask<HTTPResponse> task = tasks.get(site);

            if (!task.isDone()) {
                SiteDataService.updateSite(site);
                continue;
            }

            try {
                HTTPResponse response = task.get();
                System.out.println("Site '" + site.getName() + "' completed. Error: " + response.getError());
                SiteDataService.updateSite(site, response);
            } catch (InterruptedException | ExecutionException e) {
                SiteDataService.updateSite(site, IndexStatus.FAILED, e.getMessage());
                System.out.println("Error: " + e.getMessage());
            }
            tasks.remove(site);
        }
    }

    private boolean isCompleteTask() {
        if (tasks.isEmpty()) {
            isIndexing = false;
            System.out.println("All tasks completed.");
            service.shutdown();
        }
        return !isIndexing;
    }
}
