package eu.europa.ec.fisheries.uvms.movement.service.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.vividsolutions.jts.geom.Point;
import eu.europa.ec.fisheries.uvms.movement.model.MovementInstantDeserializer;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.time.Instant;

/**
 **/
@Entity
@Table(name = "movement")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = MicroMovement.FIND_ALL, query = "SELECT m FROM MicroMovement m"),
        @NamedQuery(name = MicroMovement.FIND_BY_GUID, query = "SELECT m FROM MicroMovement m WHERE m.guid = :guid"),
        @NamedQuery(name = MicroMovement.FIND_ALL_AFTER_DATE, query = "SELECT m FROM MicroMovement m WHERE m.timestamp > :date")
})
@DynamicUpdate
@DynamicInsert
public class MicroMovement implements Serializable{

        private static final long serialVersionUID = 1L;

        public static final String FIND_ALL = "MicroMovement.findAll";
        public static final String FIND_BY_GUID = "MicroMovement.findByGUID";
        public static final String FIND_ALL_AFTER_DATE = "MicroMovement.findAllAfterDate";

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO, generator = "minimal_mov_seq")
        @Basic(optional = false)
        @Column(name = "move_id")
        private Long id;

        @NotNull
        @Type(type = "org.hibernate.spatial.GeometryType")
        @Column(name = "move_location", columnDefinition = "Geometry")
        private Point location;


        @Column(name = "move_heading")
        private Double heading;

        @NotNull
        @Size(max = 36)
        @Column(name = "move_guid", nullable = false)
        private String guid;

        @NotNull
        @Fetch(FetchMode.JOIN)
        @JoinColumn(name = "move_moveconn_id", referencedColumnName = "moveconn_id")
        @ManyToOne(cascade = CascadeType.PERSIST)
        private MovementConnect movementConnect;

        @JsonSerialize(using = InstantSerializer.class)
        @JsonDeserialize(using = MovementInstantDeserializer.class)
        @Column(name = "move_timestamp")
        private Instant timestamp;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Point getLocation() {
            return location;
        }

        public void setLocation(Point location) {
            this.location = location;
        }

        public Double getHeading() {
            return heading;
        }

        public void setHeading(Double heading) {
            this.heading = heading;
        }

        public String getGuid() {
            return guid;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }

        public MovementConnect getMovementConnect() {
            return movementConnect;
        }

        public void setMovementConnect(MovementConnect movementConnect) {
            this.movementConnect = movementConnect;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }
}
