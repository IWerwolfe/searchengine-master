package searchengine.services;    /*
 *created by WerWolfe on SiteParser
 */

import lombok.Getter;
import lombok.Setter;
import searchengine.dto.index.HTTPResponse;
import searchengine.dto.index.PageServiceResponse;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.services.dataservice.PageDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

@Getter
@Setter
public class SiteParser extends RecursiveTask<HTTPResponse> {

    private static boolean stop;
    private Site site;
    private Page page;
    private long start;
    private static long count;
    private long number;

    public SiteParser(Page page) {
        this.page = page;
        this.site = page.getSite();
        init();
    }

    public SiteParser(Site site) {
        this.page = new Page(site);
        this.site = site;
        WebSiteService.initCache(site);
        PageDataService.save(page);
        init();
    }

    private void init() {
        start = System.currentTimeMillis();
        number = count++;
    }

    @Override
    protected HTTPResponse compute() {

        if (stop) {
            return new HTTPResponse(false, "Индексация остановлена пользователем");
        }

        WebSiteService.delUrlByCache(page);
        PageServiceResponse response = WebSiteService.getPage(page);
        PageDataService.update(response.getPage());

        if (response.isResult()) {
            List<Page> pages = WebSiteService.getPages(response.getDocument(), site);
            PageDataService.saveAll(pages);
            PageDataService.saveRelatedData(response.getPage());
            startJoin(pages);
        }
        return new HTTPResponse(response.isResult(), response.getError());
    }

    private void startJoin(List<Page> pages) {
        List<SiteParser> taskList = new ArrayList<>();
        pages.forEach(page -> addJoin(taskList, page));
        taskList.forEach(ForkJoinTask::join);
    }

    private void addJoin(List<SiteParser> taskList, Page page) {
        SiteParser parser = new SiteParser(page);
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
