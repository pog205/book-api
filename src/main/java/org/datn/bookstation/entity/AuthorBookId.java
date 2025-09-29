package org.datn.bookstation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class AuthorBookId implements Serializable {
    private static final long serialVersionUID = -1480258986530885956L;
    @NotNull
    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @NotNull
    @Column(name = "author_id", nullable = false)
    private Integer authorId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AuthorBookId entity = (AuthorBookId) o;
        return Objects.equals(this.authorId, entity.authorId) &&
                Objects.equals(this.bookId, entity.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorId, bookId);
    }

}