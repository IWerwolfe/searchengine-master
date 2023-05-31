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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"site_id", "lemma"}))
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;
    private String lemma;
    private Integer frequency;

    public Lemma(Site site, String word) {
        this.site = site;
        this.lemma = word;
        this.frequency = 0;
    }

    public void incrementFrequency() {
        this.frequency++;
    }
}
