package ${{ values.packageName }}.${{ values.artifactId }}.service;

import ${{ values.packageName }}.${{ values.artifactId }}.controller.dto.PaymentDto;
import ${{ values.packageName }}.${{ values.artifactId }}.controller.dto.PaymentsPageDto;
import ${{ values.packageName }}.${{ values.artifactId }}.exceptions.PaymentException;
import ${{ values.packageName }}.${{ values.artifactId }}.exceptions.PaymentNotFoundException;

/**
 * Payment Service Interface
 */
public interface PaymentService {

    /**
     * Create New Payment
     *
     * @param newPayment
     * @return
     */
    PaymentDto create(PaymentDto newPayment) throws PaymentException;

    /**
     * Delete Payment By Id
     *
     * @param id
     * @throws PaymentNotFoundException
     */
    void deleteById(String id) throws PaymentException;

    /**
     * Find Payment By Id
     *
     * @param id
     * @return
     * @throws PaymentNotFoundException
     */
    PaymentDto findById(String id) throws PaymentException;

    /**
     * Find All Payments using Pagination
     *
     * @param pageNumber
     * @param pageSize
     * @return
     */
    PaymentsPageDto findAll(Integer pageNumber, Integer pageSize) throws PaymentException;

    /**
     * Find All Payments by Client Id using Pagination
     *
     * @param id
     * @param pageNumber
     * @param pageSize
     * @return
     */
    PaymentsPageDto findAllByClientId(String id, Integer pageNumber, Integer pageSize) throws PaymentException;
}
