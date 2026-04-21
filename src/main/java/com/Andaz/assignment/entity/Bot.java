package com.Andaz.assignment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@Table(name = "bots")
public class Bot {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bot_seq_gen")
    @SequenceGenerator(name = "bot_seq_gen", sequenceName = "bot_seq", allocationSize = 1)
    private Long id;


    @Column(unique = true, nullable = false, length = 100)
    private String name;
    

    @Column(nullable = false, columnDefinition = "TEXT")
    private String personaDescription;
}
