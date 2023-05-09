package searchengine.services;    /*
 *created by WerWolfe on WebSiteService
 */

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.SiteConnection;
import searchengine.dto.AppSettingCollector;
import searchengine.dto.RepositoryCollector;
import searchengine.dto.index.PageServiceResponse;
import searchengine.dto.index.PageSet;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static java.lang.Thread.sleep;

public abstract class WebSiteService {

    static final ConcurrentHashMap<Site, PageSet> cacheUrls = new ConcurrentHashMap<>();
    private static final String regexPath = "^\\/.*";
    private static final String regexUrl = "^(https?://)?[^/]+";
    private static final String regexFile = ".*\\.([a-zA-Z0-1]{1,4})[^\bphp|html\b]$";

    public static Document getHTMLPage(String url) throws InterruptedException, IOException {

        SiteConnection setting = AppSettingCollector.getSettConnection();
        if (setting == null) {
            throw new IOException("An error occurred while getting the settings");
        }

        sleep((long) ((Math.random() * 2000) + (1000 * Math.random()) + 400));
        return Jsoup.connect(url)
                .timeout(5000)
                .userAgent(setting.getUserAgent())
                .referrer(setting.getReferrer())
                .get();
    }

    public static Document getHTMLPage(Page page) throws InterruptedException, IOException {
        return getHTMLPage(getAbsolutePath(page));
    }

    public static Elements clearDocumentAtFooterAndButton(Document document) {
        return document.body().select(":not([class*=footer]):not([class*=btn]):not([class*=button]):not([class*=header])");
    }

    public static PageServiceResponse getPage(Page page) {
        try {
            Document document = getHTMLPage(page);
            page.setCode(200);
            page.setContent(document.html());
            return new PageServiceResponse(page, document);
        } catch (InterruptedException | IOException e) {
            page.setCode(e.getClass() == HttpStatusException.class ? ((HttpStatusException) e).getStatusCode() : 400);
            return new PageServiceResponse(page, e.getMessage());
        }
    }

    public static PageServiceResponse getPage(String url, Site site) {
        return getPage(new Page(site, getUrl(url)));
    }

    public static HashSet<String> getUrlFromPage(Document document, String regex) {
        HashSet<String> refList = new HashSet<>();
        Elements elements = document.select("a[href]");
        elements.forEach(hrefObject -> addRef(hrefObject, regex, refList));
        return refList;
    }

    public static List<Page> getPages(Document document, Site site) {

        HashSet<String> urls = getUrlFromPage(document, getDomainRegex(site));
        delCachedUrls(urls, site);
        List<Page> urlToBD = RepositoryCollector.getPageRepository().findBySiteAndPathInOrderByPathAsc(site, urls);
        urlToBD.forEach(page -> urls.remove(page.getPath()));

        List<Page> pages = new ArrayList<>();
        urls.forEach(url -> {
            pages.add(new Page(site, url));
            cacheUrls.get(site).addUrl(url);
        });
        return pages;
    }

    public static void delCachedUrls(HashSet<String> urls, Site site) {
        ConcurrentSkipListSet<String> cache = cacheUrls.get(site).getUrls();
        cache.forEach(urls::remove);
    }

    public static void delUrlByCache(Site site, String url) {
        cacheUrls.get(site).remoteUrl(url);
    }

    public static void delUrlByCache(Page page) {
        cacheUrls.get(page.getSite()).remoteUrl(page.getPath());
    }

    private static void addRef(Element hrefObject, String regex, HashSet<String> refList) {
        String href = hrefObject.attr("href").trim();
        if (href.matches(regex) && !href.matches(regexFile)) {
            refList.add(getUrl(href));
        }
    }

    public static String getAbsolutePath(Page page) {
        Site site = page.getSite();
        String path = page.getPath();
        boolean isConcat = !site.getUrl().matches(regexPath) && !path.matches(regexPath);
        String siteName = isConcat ? site.getUrl().concat("/") : site.getUrl();
        return siteName + path + "/";
    }

    public static String getUrl(String href) {
        href = href.replaceAll(regexUrl, "");
        if (href.length() == 0) {
            return "/";
        }
        if (href.charAt(href.length() - 1) == '/') {
            return href.substring(0, href.length() - 1);
        }
        return href;
    }

    public static String getDomainName(Site site) {
        return getDomainName(site.getUrl());
    }

    public static String getDomainName(String url) {
        String regex = "https?:\\/\\/(?:www\\.|)";
        String regexFor = "/.*";
        url = url.replaceAll(regex, "");
        return url.replaceAll(regexFor, "");
    }

    public static String getDomainRegex(String url) {
        return "(https?:\\/\\/(?:www\\.|)" + getDomainName(url) + ")?\\/[A-Za-z0-9_\\-\\/\\.]+";
    }

    public static String getDomainRegex(Site site) {
        return getDomainRegex(site.getUrl());
    }

    public static void initCache(Site site) {
        cacheUrls.put(site, new PageSet());
    }
}
