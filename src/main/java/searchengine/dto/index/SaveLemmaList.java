package searchengine.dto.index;    /*
 *created by WerWolfe on SaveLemmaList
 */

import lombok.Data;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;

@Data
public class SaveLemmaList {
    private Site site;
    private List<Lemma> lemmaList;
}
