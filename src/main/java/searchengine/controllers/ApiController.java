package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.SiteConnection;
import searchengine.dto.AppSettingCollector;
import searchengine.dto.RepositoryCollector;
import searchengine.dto.statistics.*;
import searchengine.dto.index.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.IndexPageService;
import searchengine.services.IndexService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexService indexService;
    private final IndexPageService indexPageService;
    @Autowired
    private final PageRepository pageRepository;
    @Autowired
    private final SiteRepository siteRepository;
    @Autowired
    private final LemmaRepository lemmaRepository;
    private final SiteConnection siteConnection;
    private final IndexRepository indexRepository;

    public ApiController(StatisticsService statisticsService, IndexService indexService, IndexPageService indexPageService, PageRepository pageRepository, SiteRepository siteRepository, LemmaRepository lemmaRepository, SiteConnection siteConnection, IndexRepository indexRepository) {
        this.statisticsService = statisticsService;
        this.indexService = indexService;
        this.indexPageService = indexPageService;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.lemmaRepository = lemmaRepository;
        this.siteConnection = siteConnection;
        this.indexRepository = indexRepository;
        init();
    }

    private void init() {
        RepositoryCollector.setLemmaRepository(lemmaRepository);
        RepositoryCollector.setPageRepository(pageRepository);
        RepositoryCollector.setSiteRepository(siteRepository);
        RepositoryCollector.setIndexRepository(indexRepository);
        AppSettingCollector.setSettConnection(siteConnection);
//        indexRepository.saveAllAndFlush()
//        pageRepository.removeByPathIgnoreCase()
//        siteRepository.
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexResponse> startIndexing() {
        System.out.println("startIndexing ");
        return ResponseEntity.ok(indexService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexResponse> stopIndexing() {
        System.out.println("stopIndexing ");
        return ResponseEntity.ok(indexService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexResponse> startIndexingPage(@RequestBody String url) {
        System.out.println("startIndexing " + url);
        return ResponseEntity.ok(indexPageService.startIndexingPage(url));
    }
}
