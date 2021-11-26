package com.example.api.security.audit;

import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import java.util.Date;
import static javax.persistence.TemporalType.TIMESTAMP;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable<U> {

    @Getter
    @Column(name = "created_by", nullable = false, updatable = false)
    @CreatedBy
    protected U createdBy;

    @Getter
    @Column(name = "creation_date", nullable = false, updatable = false)
    @CreatedDate
    @Temporal(TIMESTAMP)
    protected Date creationDate;

    @Getter
    @LastModifiedBy
    protected U lastModifiedBy;

    @Getter
    @LastModifiedDate
    @Temporal(TIMESTAMP)
    protected Date lastModifiedDate;
}
