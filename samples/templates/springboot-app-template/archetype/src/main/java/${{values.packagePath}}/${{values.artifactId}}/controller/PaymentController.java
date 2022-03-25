package ${{ values.packageName }}.${{ values.artifactId }}.controller;

import ${{ values.packageName }}.${{ values.artifactId }}.controller.dto.PaymentDto;
import ${{ values.packageName }}.${{ values.artifactId }}.controller.dto.PaymentsPageDto;
import ${{ values.packageName }}.${{ values.artifactId }}.exceptions.HttpException;
import ${{ values.packageName }}.${{ values.artifactId }}.exceptions.PaymentConflictException;
import ${{ values.packageName }}.${{ values.artifactId }}.exceptions.PaymentNotFoundException;
import ${{ values.packageName }}.${{ values.artifactId }}.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController()
@RequiredArgsConstructor
public class PaymentController implements PaymentsApi {

    private final PaymentService paymentService;

    @Override
    public ResponseEntity<Void> createPayment(PaymentDto paymentDto) throws Exception {
        try {
            paymentService.create(paymentDto);
            return ResponseEntity.created(URI.create(paymentDto.getId())).build();
        } catch (Exception ex) {
            throw fromServiceException(ex);
        }
    }

    @Override
    public ResponseEntity<Void> deletePaymentById(String id) throws Exception {
        try {
            paymentService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            throw fromServiceException(ex);
        }
    }

    @Override
    public ResponseEntity<PaymentsPageDto> findAllPaymentsByClientId(String id, Integer pageNumber, Integer pageSize)
            throws Exception{
        try {
            return ResponseEntity.ok(paymentService.findAllByClientId(id, pageNumber, pageSize));
        } catch (Exception ex) {
            throw fromServiceException(ex);
        }
    }

    @Override
    public ResponseEntity<PaymentDto> findPaymentById(String id) throws Exception{
        try {
            return ResponseEntity.ok(paymentService.findById(id));
        } catch (Exception ex) {
            throw fromServiceException(ex);
        }
    }

    @Override
    public ResponseEntity<PaymentsPageDto> findAllPayments(Integer pageNumber, Integer pageSize) throws Exception {
        try {
            return ResponseEntity.ok(paymentService.findAll(pageNumber, pageSize));
        } catch (Exception ex) {
            throw fromServiceException(ex);
        }
    }

    private Exception fromServiceException(Exception ex) {
        if (ex instanceof PaymentNotFoundException) return new HttpException(HttpStatus.NOT_FOUND, ex);
        else if (ex instanceof PaymentConflictException) return new HttpException(HttpStatus.CONFLICT, ex);
        else return new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

}
