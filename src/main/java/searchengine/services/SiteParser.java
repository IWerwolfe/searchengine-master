package searchengine.services;    /*
 *created by WerWolfe on SiteParser
 */

import lombok.Getter;
import lombok.Setter;
import searchengine.dto.RepositoryCollector;
import searchengine.dto.index.HTTPResponse;
import searchengine.dto.index.PageServiceResponse;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.services.dataservice.LemmaDataService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import static java.lang.Thread.sleep;

@Getter
@Setter
public class SiteParser extends RecursiveTask<HTTPResponse> {

    private static boolean stop;
    private Site site;
    private Page page;

    public SiteParser(Page page) {
        this.page = page;
        this.site = page.getSite();
    }

    public SiteParser(Site site) {
        this.page = new Page(site);
        this.site = site;
    }

    @Override
    protected HTTPResponse compute() {

        PageRepository pageRepository = RepositoryCollector.getPageRepository();

        if (stop) {
           return new HTTPResponse(false, "Индексация остановлена пользователем");
        }
        if (pageRepository.findBySiteAndPathIgnoreCase(site, page.getPath()).isPresent()) {
            return null;
        }

        pageRepository.save(page);
        PageServiceResponse response = PageService.getPage(page);

        if (response.isResult()) {
            HashMap<String, Integer> words = ExtractText.getWords(response.getPage().getContent());
            LemmaDataService.saveLemmas(words, site);
            Set<String> refList = PageService.getRef(response.getDocument(), PageService.getDomainRegex(site));
            startJoin(refList);
        }

        page = response.getPage();
        pageRepository.updateCodeAndContentById(page.getCode(), page.getContent(), page.getId());
        return new HTTPResponse(response.isResult(), response.getError());
    }

    private void startJoin(Set<String> refList) {
        List<SiteParser> taskList = new ArrayList<>();
        refList.forEach(ref -> addJoin(taskList, ref));
        taskList.forEach(ForkJoinTask::join);
    }

    private void addJoin(List<SiteParser> taskList, String ref) {
        SiteParser parser = new SiteParser(new Page(site, ref));
        parser.fork();
        taskList.add(parser);
    }

    public static boolean isStop() {
        return stop;
    }

    public static void stop() {
        SiteParser.stop = true;
    }
}
