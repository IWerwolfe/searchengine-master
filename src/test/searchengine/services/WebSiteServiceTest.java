package searchengine.services;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import searchengine.dto.index.PageServiceResponse;
import searchengine.model.IndexStatus;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
 *created by WerWolfe on
 */class WebSiteServiceTest {

//    @Test
//    void getHTMLPage() {
//    }
//
//    @Test
//    void testGetHTMLPage() {
//    }
//
//    @Test
//    void clearDocumentAtFooterAndButton() {
//    }
//
//    @Test
//    void getPage() {
//    }
//
//    @Test
//    void testGetPage() {
//    }
//
//    @Test
//    void getUrlFromPage() {
//    }
//
//    @Test
//    void getPages() {
//    }
//
//    @Test
//    void delCachedUrls() {
//    }
//
//    @Test
//    void delUrlByCache() {
//    }
//
//    @Test
//    void testDelUrlByCache() {
//    }
//
//    @Test
//    void getAbsolutePath() {
//    }
//
//    @Test
//    void getUrl() {
//    }
//
//    @Test
//    void getDomainName() {
//    }
//
//    @Test
//    void testGetDomainName() {
//    }
//
//    @Test
//    void getDomainRegex() {
//    }
//
//    @Test
//    void testGetDomainRegex() {
//    }
//
//    @Test
//    void initCache() {
//    }

    @Test
    void getHTMLPageThrowsException() {
        assertThrows(IOException.class, () -> WebSiteService.getHTMLPage("invalid_url"));
    }

    @Test
    void getHTMLPageReturnsDocument() throws InterruptedException, IOException {
        String validUrl = "https://www.example.com";
        Document document = WebSiteService.getHTMLPage(validUrl);
        assertNotNull(document);
    }

    @Test
    void clearDocumentAtFooterAndButton() {
        Document document = new Document("");
        Elements elements = WebSiteService.clearDocumentAtFooterAndButton(document);
        assertNotNull(elements);
    }

    @Test
    void getPageReturnsPageServiceResponse() {
        String url = "https://www.example.com";
        Site site = new Site(IndexStatus.INDEXED, "https://www.example.com", "example");
        Page page = new Page(site, WebSiteService.getUrl(url));
        PageServiceResponse pageServiceResponse = WebSiteService.getPage(page);
        assertNotNull(pageServiceResponse);
    }

    @Test
    void getUrlFromPageReturnsHashSet() throws IOException, InterruptedException {
        String url = "https://www.example.com";
        Site site = new Site(IndexStatus.INDEXED, "https://www.example.com", "example");
        Page page = new Page(site, WebSiteService.getUrl(url));
        Document document = WebSiteService.getHTMLPage(page);
        HashSet<String> urls = WebSiteService.getUrlFromPage(document, WebSiteService.getDomainRegex(site));
        assertNotNull(urls);
    }

    @Test
    void getPagesReturnsListOfPages() throws IOException, InterruptedException {
        String url = "https://www.example.com";
        Site site = new Site(IndexStatus.INDEXED, "https://www.example.com", "example");
        Page page = new Page(site, WebSiteService.getUrl(url));
        Document document = WebSiteService.getHTMLPage(page);
        List<Page> pages = WebSiteService.getPages(document, site);
        assertNotNull(pages);
    }

    @Test
    void delCachedUrls() {
        Site site = new Site(IndexStatus.INDEXED, "https://www.example.com", "example");
        WebSiteService.initCache(site);
        WebSiteService.delCachedUrls(new HashSet<>(), site);
        assertTrue(WebSiteService.cacheUrls.get(site).getUrls().isEmpty());
    }

    @Test
    void delUrlByCacheWithSite() {
        Site site = new Site(IndexStatus.INDEXED, "https://www.example.com", "example");
        WebSiteService.initCache(site);
        WebSiteService.delUrlByCache(site, "https://www.example.com/about");
        assertTrue(WebSiteService.cacheUrls.get(site).getUrls().isEmpty());
    }

    @Test
    void delUrlByCacheWithPage() {
        Site site = new Site(IndexStatus.INDEXED, "https://www.example.com", "example");
        WebSiteService.initCache(site);
        Page page = new Page(site, "/about");
        WebSiteService.delUrlByCache(page);
        assertTrue(WebSiteService.cacheUrls.get(site).getUrls().isEmpty());
    }

    @Test
    void getAbsolutePath() {
        Site site = new Site(IndexStatus.INDEXED, "https://www.example.com", "example");
        Page page = new Page(site, "/about");
        String absolutePath = WebSiteService.getAbsolutePath(page);
        assertEquals("https://www.example.com/about/", absolutePath);
    }

    @Test
    void getUrl() {
        // Test case 1: URL with protocol and domain
        String input1 = "https://www.example.com/path/to/page/";
        String expected1 = "/path/to/page";
        String actual1 = WebSiteService.getUrl(input1);
        assertEquals(expected1, actual1);

        // Test case 2: URL with only domain
        String input2 = "http://example.com";
        String expected2 = "/";
        String actual2 = WebSiteService.getUrl(input2);
        assertEquals(expected2, actual2);

        // Test case 3: URL with only path
        String input3 = "/path/to/page";
        String expected3 = "/path/to/page";
        String actual3 = WebSiteService.getUrl(input3);
        assertEquals(expected3, actual3);

        // Test case 4: URL with trailing slash
        String input4 = "https://www.example.com/path/to/page/";
        String expected4 = "/path/to/page";
        String actual4 = WebSiteService.getUrl(input4);
        assertEquals(expected4, actual4);

        // Test case 5: URL with query string
        String input5 = "https://www.example.com/path/to/page/?param=value";
        String expected5 = "/path/to/page";
        String actual5 = WebSiteService.getUrl(input5);
        assertEquals(expected5, actual5);

        // Test case 6: URL with fragment
        String input6 = "https://www.example.com/path/to/page/#section";
        String expected6 = "/path/to/page";
        String actual6 = WebSiteService.getUrl(input6);
        assertEquals(expected6, actual6);

        // Test case 7: URL with protocol, domain and path
        String input7 = "https://www.example.com";
        String expected7 = "/";
        String actual7 = WebSiteService.getUrl(input7);
        assertEquals(expected7, actual7);

        // Test case 8: URL with non-alphanumeric characters in path
        String input8 = "https://www.example.com/path/to/page-123/";
        String expected8 = "/path/to/page-123";
        String actual8 = WebSiteService.getUrl(input8);
        assertEquals(expected8, actual8);
    }
}