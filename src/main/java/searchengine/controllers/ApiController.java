package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.SiteConnection;
import searchengine.dto.AppSettingCollector;
import searchengine.dto.RepositoryCollector;
import searchengine.dto.index.IndexResponse;
import searchengine.dto.search.SearchRequest;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.IndexPageService;
import searchengine.services.IndexService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private final StatisticsService statisticsService;
    @Autowired
    private final SearchService searchService;
    @Autowired
    private final IndexService indexService;
    @Autowired
    private final IndexPageService indexPageService;
    @Autowired
    private final PageRepository pageRepository;
    @Autowired
    private final SiteRepository siteRepository;
    @Autowired
    private final LemmaRepository lemmaRepository;
    @Autowired
    private final SiteConnection siteConnection;
    @Autowired
    private final IndexRepository indexRepository;

    public ApiController(StatisticsService statisticsService, SearchService searchService, IndexService indexService, IndexPageService indexPageService, PageRepository pageRepository, SiteRepository siteRepository, LemmaRepository lemmaRepository, SiteConnection siteConnection, IndexRepository indexRepository) {
        this.statisticsService = statisticsService;
        this.searchService = searchService;
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
//        pageRepository.delete.deleteByIdIn()
//        indexRepository.deleteByPageInAllIgnoreCase()
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexResponse> startIndexing() {
        System.out.println("indexing started");
        return ResponseEntity.ok(indexService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexResponse> stopIndexing() {
        System.out.println("indexing stopped");
        return ResponseEntity.ok(indexService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexResponse> startIndexingPage(@RequestBody String url) {
        System.out.println("page " + url + " indexing started ");
        return ResponseEntity.ok(indexPageService.startIndexingPage(url));
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> searchWithSite(@RequestParam("query") String query,
                                                         @RequestParam(defaultValue = "all") String site,
                                                         @RequestParam("offset") int offset,
                                                         @RequestParam("limit") int limit) {
        System.out.println("search started");
        return ResponseEntity.ok(searchService.search(new SearchRequest(query, site, offset, limit)));
    }
}
