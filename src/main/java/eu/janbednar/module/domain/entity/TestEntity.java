package eu.janbednar.module.domain.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
public class TestEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    @Column
    private String value;

    @Column
    private String correlationId;

    @Column
    private String fromEndpoint;

    @Column
    private String routeId;

    @Column
    private Date created;

    @Column
    private Date updated;

    @PrePersist
    protected void onCreate() {
        created = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updated = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getFromEndpoint() {
        return fromEndpoint;
    }

    public void setFromEndpoint(String fromEndpoint) {
        this.fromEndpoint = fromEndpoint;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    @Override
    public String toString() {
        return "TestEntity{" +
                "id=" + id +
                ", value='" + value + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", fromEndpoint='" + fromEndpoint + '\'' +
                ", routeId='" + routeId + '\'' +
                ", created=" + created +
                ", updated=" + updated +
                '}';
    }
}
