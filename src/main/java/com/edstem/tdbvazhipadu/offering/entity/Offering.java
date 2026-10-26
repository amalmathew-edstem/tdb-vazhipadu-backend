package com.edstem.tdbvazhipadu.offering.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean active;

    @Version
    private Long version;

    public Offering(String name, BigDecimal price, boolean active) {
        this.name = name;
        this.price = price;
        this.active = active;
    }

    public void revise(String name, BigDecimal price, boolean active) {
        this.name = name;
        this.price = price;
        this.active = active;
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
