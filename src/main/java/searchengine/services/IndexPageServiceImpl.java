package searchengine.services;    /*
 *created by WerWolfe on IndexPageServiceImpl
 */

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SiteConnection;
import searchengine.config.SitesList;
import searchengine.dto.RepositoryCollector;
import searchengine.dto.index.CheckSiteResponse;
import searchengine.dto.index.IndexResponse;
import searchengine.dto.index.PageServiceResponse;
import searchengine.model.IndexStatus;
import searchengine.model.Lemma;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.dataservice.IndexDataService;
import searchengine.services.dataservice.LemmaDataService;
import searchengine.services.dataservice.PageDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IndexPageServiceImpl implements IndexPageService {

    private final SitesList sitesList;
    private String regex = "url=.*";

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

        PageServiceResponse response = PageService.getPage(url, checkResponse.getSite());

        if (!response.isResult()) {
            return new IndexResponse(false, response.getError());
        }

        PageDataService.deletePageAllAndRelatedDataByUrl(response.getPage().getPath());
        PageDataService.save(response.getPage());
        HashMap<String, Integer> words = ExtractText.getWords(response.getPage().getContent());
        HashMap<Lemma, Integer> lemmaList = LemmaDataService.saveLemmas(words, checkResponse.getSite());
        IndexDataService.SaveAll(lemmaList, response.getPage());

        return new IndexResponse(true, "");
    }

    private String convertUrl(String url) {
        url = url.replaceAll("%2F", "/");
        url = url.replaceAll("%3A", ":");
        url = url.replaceAll("url=", "");
        return url;
    }

    private CheckSiteResponse checkAndGetSite(String url) {

        SiteRepository repository = RepositoryCollector.getSiteRepository();
        Site site = searchSite(url);

        if (site == null) {
            return new CheckSiteResponse(false, null);
        }

        Optional<searchengine.model.Site> optional = repository.findByUrlLikeIgnoreCase(site.getUrl());

        if (optional.isEmpty()) {
            searchengine.model.Site siteDTO = new searchengine.model.Site(IndexStatus.INDEXED, site.getUrl(), site.getName());
            repository.save(siteDTO);
            return new CheckSiteResponse(true, siteDTO);
        }

        return new CheckSiteResponse(true, optional.get());
    }

    private Site searchSite(String url) {
        List<Site> sites = sitesList.getSites();
        String siteRegex = "https?://(?:www\\.|)" + PageService.getDomainName(url);

        for (Site value : sites) {
            if (value.getUrl().matches(siteRegex)) {
                return value;
            }
        }
        return null;
    }
}
