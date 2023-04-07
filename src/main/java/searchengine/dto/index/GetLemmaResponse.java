package searchengine.dto.index;    /*
 *created by WerWolfe on GetLemmaResponse
 */

import lombok.Data;
import searchengine.model.Lemma;

@Data
public class GetLemmaResponse {
    private boolean result;
    private Lemma lemma;
}
