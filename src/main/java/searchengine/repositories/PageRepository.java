package searchengine.repositories;    /*
 *created by WerWolfe on PageRepository
 */

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    List<Page> removeByPathIgnoreCase(String path);
    List<Page> removeById(Integer id);
    @Transactional
    List<Page> removeBySite(@NonNull Site site);
    List<Page> deleteBySiteIn(Collection<Site> sites);
    List<Page> findBySite(Site site);
    Optional<Page> findBySiteAndPathIgnoreCase(Site site, String path);

    @Transactional
    @Modifying
    @Query("update Page p set p.code = ?1, p.content = ?2 where p.id = ?3")
    void updateCodeAndContentById(Integer code, String content, Integer id);

}
