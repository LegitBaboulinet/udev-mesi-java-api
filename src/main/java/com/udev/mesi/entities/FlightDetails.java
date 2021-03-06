package main.java.com.udev.mesi.entities;

import com.udev.mesi.models.WsFlightDetails;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class FlightDetails implements IEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    public Long id;

    @Column(nullable = false, columnDefinition = "bool default true")
    public boolean isActive;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    @org.hibernate.annotations.Index(name = "flightIndex")
    public Flight flight;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    @org.hibernate.annotations.Index(name = "planeIndex")
    public Plane plane;

    @Column(nullable = false)
    public Date departureDateTime;

    @Column(nullable = false)
    public Date arrivaleDateTime;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "flightDetails")
    public List<Reservation> reservations;

    @Override
    public WsFlightDetails toWs() {
        return new WsFlightDetails(id, departureDateTime, arrivaleDateTime, isActive, flight.toWs(), plane.toWs());
    }
}
