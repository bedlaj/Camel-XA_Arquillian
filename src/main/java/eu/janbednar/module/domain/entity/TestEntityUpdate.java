package eu.janbednar.module.domain.entity;

import javax.persistence.*;

@Entity
public class TestEntityUpdate {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Column
    private Long value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "TestEntityUpdate{" +
                "id=" + id +
                ", value=" + value +
                '}';
    }
}
