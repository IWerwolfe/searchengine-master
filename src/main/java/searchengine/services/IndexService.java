package searchengine.services;    /*
 *created by WerWolfe on IndexingService
 */

import searchengine.dto.index.*;

import java.util.concurrent.ExecutionException;

public interface IndexService {

    IndexResponse startIndexing();
    IndexResponse stopIndexing();

}
