package com.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.mapping.Join;

import java.time.ZonedDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "bookname")
    private String bookname;

    @Column(name = "time", nullable = false)
    private ZonedDateTime time;

    @ManyToOne
    private User user;
}
