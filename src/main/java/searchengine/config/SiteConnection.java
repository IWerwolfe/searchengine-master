package searchengine.config;    /*
 *created by WerWolfe on SiteConnection
 */

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "site-connection")
public class SiteConnection {
    private String userAgent;
    private String referrer;
}
