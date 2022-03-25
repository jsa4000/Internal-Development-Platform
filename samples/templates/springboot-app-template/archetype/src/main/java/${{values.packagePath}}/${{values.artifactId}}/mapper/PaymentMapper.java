package ${{ values.packageName }}.${{ values.artifactId }}.mapper;

import ${{ values.packageName }}.${{ values.artifactId }}.controller.dto.PaymentDto;
import ${{ values.packageName }}.${{ values.artifactId }}.domain.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    Payment toDomain(PaymentDto charge);

    PaymentDto fromDomain(Payment charge);

}
