package searchengine.services;    /*
 *created by WerWolfe on SearchService
 */

import searchengine.dto.search.SearchRequest;
import searchengine.dto.search.SearchResponse;

public interface SearchService {
    SearchResponse search(SearchRequest request);
}
