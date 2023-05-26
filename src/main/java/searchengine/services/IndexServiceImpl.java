package searchengine.services;    /*
 *created by WerWolfe on IndexServiceImpl
 */

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.index.CheckSiteResponse;
import searchengine.dto.index.HTTPResponse;
import searchengine.dto.index.IndexResponse;
import searchengine.dto.index.PageServiceResponse;
import searchengine.model.Index;
import searchengine.model.IndexStatus;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexServiceImpl implements IndexService {

    private final SitesList sitesList;
    private ForkJoinPool pool;
    private Map<searchengine.model.Site, ForkJoinTask<HTTPResponse>> tasks;
    private boolean isIndexing;
    private final long delay = 5000L;
    private ScheduledExecutorService service;
    private boolean isStarted;
    private final WebSiteService webSiteService;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final ExtractText extractText;

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

    @Override
    public IndexResponse startIndexingPage(String url) {

        url = convertUrl(url);

        if (url.isEmpty()) {
            return new IndexResponse(false, "Не указан адрес страницы");
        }

        CheckSiteResponse checkResponse = checkAndGetSite(url);
        if (!checkResponse.isResult()) {
            return new IndexResponse(false, "Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }

        PageServiceResponse response = webSiteService.getPage(url, checkResponse.getSite());

        if (!response.isResult()) {
            return new IndexResponse(false, response.getError());
        }

        deletePageAllAndRelatedData(response.getPage());
        savePageAndRelatedData(response.getPage());

        log.info("Site '{}' completed. Error: {}", response.getPage().getPath(), response.getError());

        return new IndexResponse(true, "");
    }

    private void init() {
        if (isStarted) {
            log.info("Инициализация пропущена");
            return;
        }
        pool = new ForkJoinPool();
        tasks = new HashMap<>();
        service = Executors.newSingleThreadScheduledExecutor();
        isStarted = true;
    }

    private String convertUrl(String url) {
        url = url.replace("%2F", "/");
        url = url.replace("%3A", ":");
        url = url.replace("url=", "");
        return url;
    }

    private CheckSiteResponse checkAndGetSite(String url) {

        Site site = searchSite(url);

        if (site == null) {
            return new CheckSiteResponse(false, null);
        }

        Optional<searchengine.model.Site> optional = siteRepository.findByUrlLikeIgnoreCase(site.getUrl());

        if (optional.isEmpty()) {
            searchengine.model.Site siteDTO = new searchengine.model.Site(IndexStatus.INDEXED, site.getUrl(), site.getName());
            siteRepository.save(siteDTO);
            return new CheckSiteResponse(true, siteDTO);
        }

        return new CheckSiteResponse(true, optional.get());
    }

    private Site searchSite(String url) {
        List<Site> sites = sitesList.getSites();
        String siteRegex = "https?://(?:www\\.|)" + webSiteService.getDomainName(url);

        for (Site value : sites) {
            if (value.getUrl().matches(siteRegex)) {
                return value;
            }
        }
        return null;
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
            deleteSiteAndRelatedData(site.getUrl());
            searchengine.model.Site dtoSite = getDTOSite(site);
            siteRepository.save(dtoSite);

            try {
                SiteParser siteParser = new SiteParser(this);
                siteParser.setSite(dtoSite);
                tasks.put(dtoSite, pool.submit(siteParser));
            } catch (Exception e) {
                updateSite(dtoSite, e);
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
                updateSite(site);
                continue;
            }

            try {
                HTTPResponse response = task.get();
                log.info("Site '{}' completed. Error: {}", site.getName(), response.getError());
                updateSite(site, response);
            } catch (InterruptedException | ExecutionException e) {
                updateSite(site, IndexStatus.FAILED, e.getMessage());
                log.error("Error: {}", e.getMessage());
            }
            tasks.remove(site);
        }
    }

    private boolean isCompleteTask() {
        if (tasks.isEmpty()) {
            isIndexing = false;
            log.info("All tasks completed.");
            service.shutdown();
        }
        return !isIndexing;
    }

    private void deleteSiteAndRelatedData(String url) {
        Optional<searchengine.model.Site> optionalSite = siteRepository.findByUrlIgnoreCase(url);
        if (optionalSite.isPresent()) {
            searchengine.model.Site site = optionalSite.get();
            deletePageAllAndRelatedData(site);
            lemmaRepository.deleteBySite(site);
            ;
            siteRepository.delete(site);
        }
    }

    private void deletePageAllAndRelatedData(searchengine.model.Site site) {
        deleteRelatedData(pageRepository.findBySite(site));
    }

    private void deletePageAllAndRelatedData(Page page) {
        deleteRelatedData(pageRepository.findBySiteAndPathIgnoreCase(page.getSite(), page.getPath()));
    }

    private void deleteRelatedData(List<Page> pageList) {
        if (pageList.isEmpty()) {
            return;
        }
        indexRepository.deleteByPageInAllIgnoreCase(pageList);
        pageRepository.deleteByIdIn(getIdList(pageList));
    }

    private static List<Integer> getIdList(List<Page> pageList) {
        List<Integer> idList = new ArrayList<>();
        pageList.forEach(page -> idList.add(page.getId()));
        return idList;
    }

    private void updateSite(searchengine.model.Site site, IndexStatus status, String error) {
        siteRepository.updateStatusAndStatusTimeAndLastErrorById(status, LocalDateTime.now(), error, site.getId());
    }

    private void updateSite(searchengine.model.Site site, HTTPResponse result) {
        updateSite(site, result.isResult() ? IndexStatus.INDEXED : IndexStatus.FAILED, result.getError());
    }

    private void updateSite(searchengine.model.Site site, Exception result) {
        updateSite(site, IndexStatus.FAILED, result.getMessage());
    }

    private void updateSite(searchengine.model.Site site) {
        updateSite(site, IndexStatus.INDEXING, "");
    }

    public void savePageAndRelatedData(Page page) {
        pageRepository.save(page);
        saveRelatedData(page);
    }

    public void saveRelatedData(Page page) {
        String text = Jsoup.parse(page.getContent()).text();
        HashMap<String, Integer> words = extractText.getWords(text);
        HashMap<Lemma, Integer> lemmaList = saveAllLemma(words, page.getSite());
        Set<Index> indexList = getIndexList(lemmaList, page);
        indexRepository.saveAll(indexList);
    }

    private Set<Index> getIndexList(HashMap<Lemma, Integer> lemmas, Page page) {
        Set<Index> indexMap = new HashSet<>();
        for (Lemma lemma : lemmas.keySet()) {
            indexMap.add(new Index(page, lemma, (float) lemmas.get(lemma)));
        }
        return indexMap;
    }

    public HashMap<Lemma, Integer> saveAllLemma(HashMap<String, Integer> words, searchengine.model.Site site) {

        HashMap<String, Lemma> toSave = new HashMap<>();
        HashMap<Lemma, Integer> lemmaList = new HashMap<>();

        if (words.isEmpty()) {
            return lemmaList;
        }

        synchronized (site) {
            List<Lemma> lemmasToBD = lemmaRepository.findBySiteAndLemmaIn(site, words.keySet());
            lemmasToBD.forEach(l -> toSave.put(l.getLemma(), l));

            for (String word : words.keySet()) {
                Lemma lemma = createOrUpdateLemmaFrequency(toSave, word, site);
                lemmaList.put(lemma, words.get(word));
            }
        }

        lemmaRepository.saveAll(toSave.values());
        return lemmaList;
    }

    private Lemma createOrUpdateLemmaFrequency(HashMap<String, Lemma> toSave, String word, searchengine.model.Site site) {
        Lemma lemma = toSave.get(word);
        if (lemma == null) {
            lemma = new Lemma(site, word, 1);
            toSave.put(word, lemma);
        } else {
            lemma.setFrequency(lemma.getFrequency() + 1);
        }
        return lemma;
    }

    public PageRepository getPageRepository() {
        return pageRepository;
    }

    public WebSiteService getWebSiteService() {
        return webSiteService;
    }
}
