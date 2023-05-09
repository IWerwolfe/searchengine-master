package searchengine.repositories;    /*
 *created by WerWolfe on IndexRepository
 */

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.Collection;
import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<Index, Long> {
    @Transactional
    @Modifying
    @Query("delete from Index i where upper(i.page) in ?1")
    void deleteByPageInAllIgnoreCase(Collection<Page> pages);

    @Query("select i from Index i where i.lemma in ?1 order by i.page, i.rank DESC")
    List<Index> findByLemmaInOrderByPageAscRankDesc(Collection<Lemma> lemmata);
}
