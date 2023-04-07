package searchengine.repositories;    /*
 *created by WerWolfe on LemmaRepository
 */

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository  extends JpaRepository<Lemma, Long> {
    @Transactional
    List<Lemma> deleteBySite(Site site);
    @Transactional
    Optional<Lemma> findBySiteAndLemma(Site site, String lemma);
    @Transactional
    @Modifying
    @Query("update Lemma l set l.frequency = ?1 where l.id = ?2")
    void updateFrequencyById(Integer frequency, Integer id);

}
