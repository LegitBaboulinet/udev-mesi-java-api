package com.udev.mesi.models;

import main.java.com.udev.mesi.entities.Plane;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class WsPlane {
    public String ARN;
    public WsModel model;
    public boolean isUnderMaintenance;
    private boolean isActive;

    public WsPlane() {
    }

    public WsPlane(String ARN, WsModel model, boolean isUnderMaintenance, boolean isActive) {
        this.ARN = ARN;
        this.model = model;
        this.isUnderMaintenance = isUnderMaintenance;
        this.isActive = isActive;
    }

    public static WsPlane[] getArrayFromList(List<Plane> planes) {
        try {
            WsPlane[] planes_array = new WsPlane[planes.size()];
            for (int i = 0; i < planes.size(); i++) {
                planes_array[i] = planes.get(i).toWs();
            }
            return planes_array;
        } catch (NullPointerException e) {
            return null;
        }
    }
}
