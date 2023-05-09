package searchengine.dto;    /*
 *created by WerWolfe on AppSettingCollector
 */

import lombok.Data;
import searchengine.config.SiteConnection;

@Data
public abstract class AppSettingCollector {

    private static SiteConnection settConnection;

    public static SiteConnection getSettConnection() {
        return settConnection;
    }

    public static void setSettConnection(SiteConnection settConnection) {
        AppSettingCollector.settConnection = settConnection;
    }
}
