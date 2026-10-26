package com.edstem.tdbvazhipadu.order.entity;

import com.edstem.tdbvazhipadu.offering.entity.Offering;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private VazhipaduOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offering_id")
    private Offering offering;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    OrderItem(VazhipaduOrder order, Offering offering, int quantity) {
        this.order = order;
        this.offering = offering;
        this.quantity = quantity;
        this.unitPrice = offering.getPrice();
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OrderItem item)) {
            return false;
        }
        return id != null && id.equals(item.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
