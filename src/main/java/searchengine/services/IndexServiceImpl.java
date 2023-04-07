package searchengine.services;    /*
 *created by WerWolfe on IndexServiceImpl
 */

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.*;
import searchengine.dto.RepositoryCollector;
import searchengine.dto.index.HTTPResponse;
import searchengine.dto.index.IndexResponse;
import searchengine.model.IndexStatus;
import searchengine.repositories.*;
import searchengine.services.dataservice.SiteDataService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {

    private final SitesList sitesList;
    private final ForkJoinPool pool = new ForkJoinPool();
    private final Map<searchengine.model.Site, ForkJoinTask<HTTPResponse>> tasks = new HashMap<>();
    private boolean isIndexing;
    private final long delay = 5000L;
    private ScheduledExecutorService service;

    @Override
    public IndexResponse startIndexing() {

        if (isIndexing) {
            return new IndexResponse(false, "Индексация уже запущена");
        }

        if (sitesList == null) {
            return new IndexResponse(false, "Индексация не запущена");
        }

        init();
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

    private searchengine.model.Site getDTOSite(Site site) {
        searchengine.model.Site dtoSite = new searchengine.model.Site();
        dtoSite.setName(site.getName());
        dtoSite.setUrl(site.getUrl());
        dtoSite.setStatus(IndexStatus.INDEXING);
        dtoSite.setStatusTime(LocalDateTime.now());
        return dtoSite;
    }

    private void init() {

        isIndexing = true;
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
                    try {
                        checkTaskList();
                    } catch (ExecutionException | InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                }
                , delay * 2
                , delay
                , TimeUnit.MILLISECONDS);
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

    private void checkTaskList() throws ExecutionException, InterruptedException {

        if (tasks.size() == 0) {
            isIndexing = false;
            System.out.println("!!! = OK");
            service.shutdown();
        }

        for (searchengine.model.Site site : tasks.keySet()) {

            ForkJoinTask<HTTPResponse> task = tasks.get(site);

            if (!task.isDone()) {
                SiteDataService.updateSite(site);
                continue;
            }

            System.out.println("complete: " + site.getName() + " " + task.get().getError());
            SiteDataService.updateSite(site, task.get());
            tasks.remove(site);
        }
    }
}
