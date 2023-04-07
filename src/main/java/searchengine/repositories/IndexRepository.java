package searchengine.repositories;    /*
 *created by WerWolfe on IndexRepository
 */

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Index;
import searchengine.model.Page;

import java.util.Collection;

@Repository
public interface IndexRepository extends JpaRepository<Index, Long> {
    @Transactional
    @Modifying
    @Query("delete from Index i where i.page in ?1")
    int deleteByPageIn(Collection<Page> pages);

}
