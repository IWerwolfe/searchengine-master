package searchengine.services;    /*
 *created by WerWolfe on SearchServiceImpl
 */

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchDataResponse;
import searchengine.dto.search.SearchRequest;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResult;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.services.dataservice.IndexDataService;
import searchengine.services.dataservice.LemmaDataService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private SearchRequest request;
    private String regexSearchBar;
    private String regexBold;
    private List<Lemma> lemmaList;

    @Override
    public SearchResponse search(SearchRequest request) {

        this.request = request;
        Set<String> words = ExtractText.getWords(request.getQuery()).keySet();

        if (words.isEmpty()) {
            return new SearchResponse(false, "Некорректно указаны параметры поиска", 400);
        }

        lemmaList = LemmaDataService.getAlLLemmaExcludingFrequent(words, request.getSite());

        if (lemmaList.isEmpty()) {
            return new SearchResponse(false, "Ошибка при обработке поискового запроса", 400);
        }

        List<Index> indexList = IndexDataService.findIndexListByLemmaList(lemmaList);
        List<SearchResult> searchResult = searchPageResult(indexList, lemmaList.size());

        if (searchResult.isEmpty()) {
            return new SearchResponse(false, "Ничего не найдено(", 400);
        }

        return new SearchResponse(true, searchResult.size(), getDataList(searchResult), "", 200);
    }

    private String getSnippet(String content) {

        Elements elements = Jsoup.parse(content).body().select(regexSearchBar);

        if (elements.isEmpty()) {
            return "";
        }

        String text = elements.text();
        text = text.length() < 240 ? text : text.substring(0, 237) + "...";
        text = text.replaceAll(regexBold, "<b>$1</b>");

        return "<div>" + text + "</div>";
    }

    private String getRegex() {
        List<String> wordsToBold = new ArrayList<>();
        lemmaList.forEach(i -> wordsToBold.add(ExtractText.delEndWord(i.getLemma())));
        return "(?iu)(" + String.join("|", wordsToBold) + ")";
    }

    private String getSearchBar() {
        ArrayList<String> searchBar = new ArrayList<>();
        lemmaList.forEach(i -> searchBar.add(":matchesOwn(" + "(?iu)(" + ExtractText.delEndWord(i.getLemma()) + "))"));
        return String.join(",", searchBar);
    }

    private List<SearchResult> searchPageResult(List<Index> collection, int countLemma) {

        List<SearchResult> searchResults = new ArrayList<>();
        SearchResult result = null;
        int count = 1;

        for (Index index : collection) {
            if (result == null) {
                result = new SearchResult(index.getPage(), index.getRank());
                continue;
            }
            if (result.getPage() == index.getPage()) {
                result.setAbsoluteRelevance(result.getAbsoluteRelevance() + index.getRank());
                count++;
                continue;
            }

//            if (count >= countLemma) {
            searchResults.add(result);
//                count = 1;
//            } else {
//                System.out.println("remove:\t" + result);
//            }
            result = new SearchResult(index.getPage(), index.getRank());
        }
        Collections.sort(searchResults);
        return searchResults;
    }

    private List<SearchDataResponse> getDataList(List<SearchResult> searchResults) {

        List<SearchDataResponse> dataList = new ArrayList<>();
        int end = request.getOffset() + request.getLimit();
        int start = request.getOffset();
        float maxRelevance = searchResults.get(0).getAbsoluteRelevance();

        regexSearchBar = getSearchBar();
        regexBold = getRegex();

        for (int i = start; i < searchResults.size() && i <= end; i++) {

            SearchDataResponse data = new SearchDataResponse();
            Page page = searchResults.get(i).getPage();
            Site site = page.getSite();

            data.setUri(page.getPath());
            data.setRelevance(searchResults.get(i).getAbsoluteRelevance() / maxRelevance);
            data.setTitle(Jsoup.parse(page.getContent()).title());
            data.setSnippet(getSnippet(page.getContent()));
            data.setSite(site.getUrl());
            data.setSiteName(site.getName());
            dataList.add(data);
        }
        return dataList;
    }
}
