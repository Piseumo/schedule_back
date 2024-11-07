package com.example.dailyLog.entity;

import com.example.dailyLog.constant.Status;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@Table(name = "friend",uniqueConstraints = @UniqueConstraint(columnNames = {"req_idx", "rec_idx"}))
public class Friend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "f_idx")
    private Long idx;

    @ManyToOne
    @JoinColumn(name = "req_idx", nullable = false)
    private User requester;

    @ManyToOne
    @JoinColumn(name = "rec_idx", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(name = "f_status", nullable = false)
    private Status status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
