package searchengine.services;    /*
 *created by WerWolfe on IndexPageService
 */

import searchengine.dto.index.IndexResponse;

public interface IndexPageService {

    IndexResponse startIndexingPage(String url);

}
