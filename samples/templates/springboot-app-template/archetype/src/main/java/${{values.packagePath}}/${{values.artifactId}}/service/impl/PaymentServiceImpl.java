package ${{ values.packageName }}.${{ values.artifactId }}.service.impl;

import ${{ values.packageName }}.${{ values.artifactId }}.controller.dto.PaymentDto;
import ${{ values.packageName }}.${{ values.artifactId }}.controller.dto.PaymentsPageDto;
import ${{ values.packageName }}.${{ values.artifactId }}.domain.Payment;
import ${{ values.packageName }}.${{ values.artifactId }}.exceptions.PaymentConflictException;
import ${{ values.packageName }}.${{ values.artifactId }}.exceptions.PaymentException;
import ${{ values.packageName }}.${{ values.artifactId }}.exceptions.PaymentNotFoundException;
import ${{ values.packageName }}.${{ values.artifactId }}.mapper.PaymentMapper;
import ${{ values.packageName }}.${{ values.artifactId }}.repository.PaymentRepository;
import ${{ values.packageName }}.${{ values.artifactId }}.service.PaymentService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    @Timed(value = "${{ values.packageName }}.${{ values.artifactId }}.service.payment")
    public PaymentDto create(PaymentDto newPayment) throws PaymentException {
        try {
            if (paymentRepository.existsById(newPayment.getId()))
                throw new PaymentConflictException(String.format("Payment %s already exists", newPayment.getId()));
            Payment payment = paymentRepository.save(paymentMapper.toDomain(newPayment));
            return paymentMapper.fromDomain(payment);
        }
        catch (Exception ex) {
            if (ex instanceof PaymentConflictException) throw ex;
            else throw new PaymentException(ex);
        }
    }

    @Timed(value = "${{ values.packageName }}.${{ values.artifactId }}.service.payment")
    public void deleteById(String id) throws PaymentException {
        try {
            if (!paymentRepository.existsById(id))
                throw new PaymentNotFoundException(String.format("Payment not found %s", id));
            paymentRepository.deleteById(id);
        }
        catch (Exception ex) {
            if (ex instanceof PaymentNotFoundException) throw ex;
            else throw new PaymentException(ex);
        }
    }

    @Transactional(readOnly = true)
    @Timed(value = "${{ values.packageName }}.${{ values.artifactId }}.service.payment")
    public PaymentDto findById(String id) throws PaymentException {
        try {
            Optional<Payment> payment = paymentRepository.findById(id);
            if (payment.isEmpty())
                throw new PaymentNotFoundException(String.format("Payment not found %s", id));
            return paymentMapper.fromDomain(payment.get());
        }
        catch (Exception ex) {
            if (ex instanceof PaymentNotFoundException) throw ex;
            else throw new PaymentException(ex);
        }
    }

    @Transactional(readOnly = true)
    @Timed(value = "${{ values.packageName }}.${{ values.artifactId }}.service.payment")
    public PaymentsPageDto findAll(Integer pageNumber, Integer pageSize) throws PaymentException {
        try {
            Pageable pages = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate").descending());
            Page<Payment> page = paymentRepository.findAll(pages);
            List<PaymentDto> items = page
                    .getContent().stream()
                    .map(paymentMapper::fromDomain)
                    .collect(Collectors.toList());
            return new PaymentsPageDto()
                    .results(items)
                    .pageNumber(pageNumber)
                    .pageSize(items.size())
                    .totalPages(page.getTotalPages());
        }
        catch (Exception ex) {throw new PaymentException(ex);}
    }

    @Transactional(readOnly = true)
    @Timed(value = "${{ values.packageName }}.${{ values.artifactId }}.service.payment")
    public PaymentsPageDto findAllByClientId(String id, Integer pageNumber, Integer pageSize) throws PaymentException {
        try {
            Pageable pages = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate").descending());
            Page<Payment> page = paymentRepository.findAllByClientId(id, pages);
            List<PaymentDto> items = page
                    .getContent().stream()
                    .map(paymentMapper::fromDomain)
                    .collect(Collectors.toList());
            return new PaymentsPageDto()
                    .results(items)
                    .pageNumber(pageNumber)
                    .pageSize(items.size())
                    .totalPages(page.getTotalPages());
        }
        catch (Exception ex) {throw new PaymentException(ex);}
    }

}
