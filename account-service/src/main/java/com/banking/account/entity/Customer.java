package com.banking.account.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
@Getter @Setter
@NoArgsConstructor
public class Customer {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "customer_id")
        private Long customerId;

        @Column(name = "first_name", nullable = false, length = 50)
        private String firstName;

        @Column(name = "last_name", nullable = false, length = 50)
        private String lastName;

        @Column(name = "email", nullable = false, length = 100)
        private String email;

        @Column(name = "phone_number", nullable = false)
        private Long phoneNumber;

        @Column(name = "created_by", nullable = false, length = 50)
        private String createdBy;

        @Column(name = "created_at", insertable = false, updatable = false)
        private LocalDateTime createdAt;
}