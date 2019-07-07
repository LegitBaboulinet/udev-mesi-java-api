package com.udev.mesi.models;

import main.java.com.udev.mesi.entities.Reservation;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement
public class WsReservation {
    public Long id;
    public Date reservationDate;
    public char reservationClass;
    public boolean isActive;

    public WsReservation() {
    }

    public WsReservation(Long id, Date reservationDate, char reservationClass, boolean isActive) {
        this.id = id;
        this.reservationDate = reservationDate;
        this.reservationClass = reservationClass;
        this.isActive = isActive;
    }

    public static WsReservation[] getArrayFromList(List<Reservation> reservations) {
        try {
            WsReservation[] reservation_array = new WsReservation[reservations.size()];
            for (int i = 0; i < reservations.size(); i++) {
                reservation_array[i] = reservations.get(i).toWs();
            }
            return reservation_array;
        } catch (NullPointerException e) {
            return null;
        }
    }
}
