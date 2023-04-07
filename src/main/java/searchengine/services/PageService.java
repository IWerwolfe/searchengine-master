package searchengine.services;    /*
 *created by WerWolfe on PageService
 */

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.SiteConnection;
import searchengine.dto.AppSettingCollector;
import searchengine.dto.index.PageServiceResponse;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Thread.sleep;

public abstract class PageService {

    private static String regexPath = "^\\/.*";
    private static String regexMask = "https?://(?:www\\.|)";
    private static String regexFile = ".*\\.([a-zA-Z0-1]{1,4})[^\bphp|html\b]$";

    public static Document getHTMLPage(String url) throws InterruptedException, IOException {

        SiteConnection setting = AppSettingCollector.getSettConnection();
        if (setting == null) {
            throw new IOException("An error occurred while getting the settings");
        }

        sleep((long) ((Math.random() * 2000) + (1000 * Math.random()) + 400));
        return Jsoup.connect(url)
                .userAgent(setting.getUserAgent())
                .referrer(setting.getReferrer())
                .get();
    }

    public static Document getHTMLPage(Page page) throws InterruptedException, IOException {
        return getHTMLPage(getAbsolutePath(page));
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
        return getPage(new Page(site, url));
    }

    public static Set<String> getRef(Document document, String regex) {
        HashSet<String> refList = new HashSet<>();
        Elements elements = document.select("a[href]");
        elements.forEach(hrefObject -> addRef(hrefObject, regex, refList));
        return refList;
    }

    private static void addRef(Element hrefObject, String regex, HashSet<String> refList) {
        String href = hrefObject.attr("href").trim();
        if (href.matches(regex) && !href.matches(regexFile)) {
            refList.add(href);
        }
    }

    public static String getAbsolutePath(Page page) {
        Site site = page.getSite();
        String path = page.getPath();
        boolean isConcat = !site.getUrl().matches(regexPath) && !path.matches(regexPath);
        String siteName = isConcat ? site.getUrl().concat("/") : site.getUrl();
        return siteName + path + "/";
    }

    public static String getUrl(String href, Site site) {
        href = href.replaceAll(regexMask + getDomainName(site), "");
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
}
