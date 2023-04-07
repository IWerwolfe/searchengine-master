package searchengine.model;    /*
 *created by WerWolfe on Index
 */

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.security.PrivateKey;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "index_search")
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "page_id")
    private Page page;
    @ManyToOne
    @JoinColumn(name = "lemma_id")
    private Lemma lemma;
    private Float rank;

    public Index(Page page, Lemma lemma, Float rank) {
        this.page = page;
        this.lemma = lemma;
        this.rank = rank;
    }
}
