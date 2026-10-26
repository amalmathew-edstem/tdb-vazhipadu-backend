package com.edstem.tdbvazhipadu.order.entity;

import com.edstem.tdbvazhipadu.offering.entity.Offering;
import com.edstem.tdbvazhipadu.order.exception.OrderNotCancellableException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VazhipaduOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String devoteeName;

    @Column(nullable = false, length = 15)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Nakshatra nakshatra;

    @Column(nullable = false)
    private LocalDate offeringDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private Instant createdAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public VazhipaduOrder(
            String devoteeName, String phoneNumber, Nakshatra nakshatra, LocalDate offeringDate, Instant createdAt) {
        this.devoteeName = devoteeName;
        this.phoneNumber = phoneNumber;
        this.nakshatra = nakshatra;
        this.offeringDate = offeringDate;
        this.createdAt = createdAt;
        this.status = OrderStatus.BOOKED;
    }

    public void cancel() {
        if (status != OrderStatus.BOOKED) {
            throw new OrderNotCancellableException(id, status);
        }
        status = OrderStatus.CANCELLED;
    }

    public void addItem(Offering offering, int quantity) {
        OrderItem item = new OrderItem(this, offering, quantity);
        items.add(item);
        totalAmount = totalAmount.add(item.getLineTotal());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof VazhipaduOrder order)) {
            return false;
        }
        return id != null && id.equals(order.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
