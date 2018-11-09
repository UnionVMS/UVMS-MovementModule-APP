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
        @NamedQuery(name = MicroMovement.FIND_ALL_AFTER_DATE, query = "SELECT m FROM MicroMovement m WHERE m.timestamp > :date ORDER BY m.timestamp ASC"),
        //@NamedQuery(name = MicroMovement.FIND_LAST_MOVEMENT_FOR_ALL_ASSETS, query = "SELECT m FROM MicroMovement m WHERE m.id in (SELECT id FROM MicroMovement b, (SELECT movementConnect, MAX(timestamp) FROM MicroMovement GROUP BY movementConnect) a WHERE a.movementConnect = b.movementConnect AND a.timestamp = b.timestamp)")
})

@DynamicUpdate
@DynamicInsert
public class MicroMovement implements Serializable{

        private static final long serialVersionUID = 1L;

        public static final String FIND_ALL = "MicroMovement.findAll";
        public static final String FIND_BY_GUID = "MicroMovement.findByGUID";
        public static final String FIND_ALL_AFTER_DATE = "MicroMovement.findAllAfterDate";
        public static final String FIND_LAST_MOVEMENT_FOR_ALL_ASSETS = "MicroMovement.findLastMovementForAllAssets";
        public static final String FIND_LAST_MOVEMENT_FOR_ALL_ASSETS_QUERY = "SELECT * FROM movement.movement m\n" +    //I could not get this query to work in HQL, the result of that attempt is commented out above, so a native sql query it is
                "        WHERE move_id IN\n" +
                "        (SELECT move_id as id\n" +
                "        FROM movement.movement b\n" +
                "        INNER JOIN (SELECT move_moveconn_id, MAX(move_timestamp) as MaxValue\n" +
                "        FROM movement.movement\n" +
                "        GROUP BY move_moveconn_id) a ON\n" +
                "        a.move_moveconn_id = b.move_moveconn_id AND a.MaxValue = b.move_timestamp)";

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
