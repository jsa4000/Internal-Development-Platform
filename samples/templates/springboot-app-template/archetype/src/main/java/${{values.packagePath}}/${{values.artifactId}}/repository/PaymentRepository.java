package ${{ values.packageName }}.${{ values.artifactId }}.repository;

import ${{ values.packageName }}.${{ values.artifactId }}.domain.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for the Payment entity.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    /**
     * Find all payments by client id
     *
     * @param clientId
     * @param page
     * @return
     */
    Page<Payment> findAllByClientId(String clientId, Pageable page);

}
