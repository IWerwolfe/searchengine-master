package searchengine.model;    /*
 *created by WerWolfe on Lemma
 */

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;
    private String lemma;
    private Integer frequency;

    public Lemma(Site site, String lemma, Integer frequency) {
        this.site = site;
        this.lemma = lemma;
        this.frequency = frequency;
    }
}
