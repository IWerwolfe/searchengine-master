package searchengine.services;    /*
 *created by WerWolfe on IndexingService
 */

import searchengine.dto.index.IndexResponse;

public interface IndexService {

    IndexResponse startIndexing();
    IndexResponse stopIndexing();

    IndexResponse startIndexingPage(String url);

}
