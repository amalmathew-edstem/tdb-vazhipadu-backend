package com.edstem.tdbvazhipadu.offering.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "offering")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Offering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "offering_type", nullable = false, length = 30)
    private OfferingType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "daily_limit")
    private Integer dailyLimit;

    @Version
    private Long version;

    public Offering(OfferingType type, BigDecimal price, boolean active, Integer dailyLimit) {
        this.type = type;
        this.price = price;
        this.active = active;
        this.dailyLimit = dailyLimit;
    }

    public void revise(OfferingType type, BigDecimal price, boolean active, Integer dailyLimit) {
        this.type = type;
        this.price = price;
        this.active = active;
        this.dailyLimit = dailyLimit;
    }

    public boolean hasDailyLimit() {
        return dailyLimit != null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Offering offering)) {
            return false;
        }
        return id != null && id.equals(offering.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
