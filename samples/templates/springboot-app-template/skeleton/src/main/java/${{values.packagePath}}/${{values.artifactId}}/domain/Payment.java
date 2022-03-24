package ${{ values.packageName }}.${{ values.artifactId }}.domain;

import ${{ values.packageName }}.${{ values.artifactId }}.converter.OffsetDateTimeConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Data
@Builder
@Table(name = "payment",
        indexes = {
                @Index(name = "idx_payment_iid", columnList = "invoice_id"),
                @Index(name = "idx_payment_cid", columnList = "client_id") })
@NoArgsConstructor
@AllArgsConstructor
public class Payment implements Serializable {

    @Id
    @NotNull
    @Size(max = 64)
    @Column(name = "payment_id", length = 64, nullable = false)
    private String id;

    @NotNull
    @Size(max = 64)
    @Column(name = "invoice_id", length = 64, nullable = false)
    private String invoiceId;

    @NotNull
    @Size(max = 64)
    @Column(name = "client_id", length = 64, nullable = false)
    private String clientId;

    @NotNull
    @Column(name = "created_date", nullable = false)
    @Convert(converter = OffsetDateTimeConverter.class)
    private OffsetDateTime createdDate;

    @NotNull
    @Column(name = "total_cost", nullable = false)
    private Double totalCost;

    @Column(name = "total_cost_no_vap")
    private Double totalCostNoVap;

    @Size(max = 256)
    @Column(name = "agency", length = 256)
    private String agency;

    @Size(max = 256)
    @Column(name = "method", length = 256)
    private String method;

    @Size(max = 256)
    @Column(name = "provider", length = 256)
    private String provider;

}

