package searchengine.services;    /*
 *created by WerWolfe on IndexingService
 */

import searchengine.dto.index.IndexResponse;

public interface IndexService {

    IndexResponse startIndexing();

    IndexResponse stopIndexing() throws InterruptedException;

    IndexResponse startIndexingPage(String url);

}
