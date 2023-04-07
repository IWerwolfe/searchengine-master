package searchengine.model;    /*
 *created by WerWolfe on Page
 */

import lombok.*;
import searchengine.services.PageService;
import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site;
    private String path;
    private Integer code;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    public Page(Site site, String path, Integer code, String content) {
        this(site, path, code);
        this.content = content;
    }

    public Page(Site site, String path, Integer code) {
        this(site, path);
        this.code = code;
    }

    public Page(Site site) {
        this(site, site.getUrl());
    }

    public Page(Site site, String path) {
        this.site = site;
        this.path = PageService.getUrl(path, site);
    }

    @Override
    public String toString() {
        String separator = "; ";
        return "Page: " + path + separator + "site: " + site.getUrl() + separator + "code: " + code;
    }
}
