package searchengine.model;    /*
 *created by WerWolfe on Site
 */

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    //    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
    private IndexStatus status;
    private LocalDateTime statusTime;
    private String lastError;
    private String url;
    private String name;

    public Site(IndexStatus status, String url, String name) {
        this.status = status;
        this.url = url;
        this.name = name;
    }
}
