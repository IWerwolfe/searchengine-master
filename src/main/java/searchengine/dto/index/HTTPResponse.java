package searchengine.dto.index;    /*
 *created by WerWolfe on HTTPResponse
 */

import lombok.Data;

@Data
public class HTTPResponse {
    private boolean result;
    private String error;

    public HTTPResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }

    public HTTPResponse(boolean result) {
        this.result = result;
    }
}
