package searchengine.dto.index;    /*
 *created by WerWolfe on CheckSiteResponse
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import searchengine.model.Site;

@Data
@AllArgsConstructor
public class CheckSiteResponse {
    private boolean result;
    private Site site;
}
