package com.fiap.clyvocaresc.entity;

import com.fiap.clyvocaresc.entity.enums.CatalogItemType;
import jakarta.persistence.*;
import lombok.Data;
//import org.springframework.data.annotation.Id;

@Entity
@Table(name = "catalog_items")
@Data
public class CatalogItem {

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_catalog_items")
        @SequenceGenerator(name = "seq_catalog_items", sequenceName = "SEQ_CATALOG_ITEMS", allocationSize = 1)
        @Column(name = "id")
        private Long id;

        @Column(nullable = false, length = 150)
        private String name;

        @Column(length = 100)
        private String manufacturer;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        private CatalogItemType type;

        @Column(name = "active_ingredient", length = 150)
        private String activeIngredient;

        @Column(name = "diseases_prevented", length = 300)
        private String diseasesPrevented;

        @Column(name = "booster_interval_days")
        private Integer boosterIntervalDays;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "species_id")
        private Species species;
}
