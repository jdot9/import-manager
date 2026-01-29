package com.dotwavesoftware.importscheduler.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="user_security_questions")  
public class UserSecurityQuestionEntity extends BaseEntity {
    @Column(name="question")
    private String question;

    @Column(name="answer")
    private String answer;

    @ManyToOne
    @JoinColumn(name="user_id")
    private UserEntity user;
}
