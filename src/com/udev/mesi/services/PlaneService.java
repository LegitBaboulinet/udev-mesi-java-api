package com.udev.mesi.services;

import com.udev.mesi.config.Database;
import com.udev.mesi.exceptions.MessageException;
import com.udev.mesi.messages.WsGetFlightDetails;
import com.udev.mesi.messages.WsGetPlanes;
import com.udev.mesi.messages.WsGetSinglePlane;
import com.udev.mesi.messages.WsResponse;
import main.java.com.udev.mesi.entities.FlightDetails;
import main.java.com.udev.mesi.entities.Model;
import main.java.com.udev.mesi.entities.Plane;
import org.hibernate.Session;
import org.json.JSONException;

import javax.persistence.Query;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

public class PlaneService {
    public static WsGetPlanes read() throws JSONException {

        // Initialisation de la réponse
        Session session = null;
        WsGetPlanes response;
        String status = "KO";
        String message = null;
        int code = 500;

        List<Plane> planes = null;

        try {
            session = Database.sessionFactory.openSession();

            // Récupération des constructeurs depuis la base de données
            Query query = session.createQuery("SELECT p FROM Plane p, Model m WHERE m.id = p.model AND p.isActive = true AND m.isActive = true ORDER BY p.ARN");
            planes = query.getResultList();

            // Création de la réponse JSON
            status = "OK";
            code = 200;
            response = new WsGetPlanes(status, message, code, planes);
        } catch (Exception e) {
            message = e.getMessage();
            response = new WsGetPlanes(status, message, code, null);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return response;
    }

    public static WsGetSinglePlane readOne(final String ARN, final String acceptLanguage) throws JSONException {

        // Initialisation de la réponse
        Session session = null;
        WsGetSinglePlane response;
        String status = "KO";
        String message;
        int code = 500;

        // Récupération de la langue de l'utilisateur
        String languageCode = MessageService.processAcceptLanguage(acceptLanguage);

        try {
            session = Database.sessionFactory.openSession();

            // Récupération de l'avion depuis la base de données
            Query query = session.createQuery("From Plane WHERE isActive = true AND ARN = :ARN");
            query.setParameter("ARN", ARN);
            List<Plane> planes = query.getResultList();

            // Vérification de l'existence du modèle
            if (planes.size() != 1) {
                code = 400;
                throw new Exception(MessageService.getMessageFromCode("plane_does_not_exist", languageCode).text);
            }

            // Création de la réponse JSON
            status = "OK";
            code = 200;
            response = new WsGetSinglePlane(status, null, code, planes.get(0));
        } catch (Exception e) {
            message = e.getMessage();
            response = new WsGetSinglePlane(status, message, code, null);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return response;
    }

    public static WsGetFlightDetails readFlightDetails(final String ARN, final String acceptLanguage) throws JSONException {

        // Initialisation de la réponse
        Session session = null;
        WsGetFlightDetails response;
        String status = "KO";
        String message = null;
        int code = 500;

        List<FlightDetails> flightDetails = null;

        try {
            session = Database.sessionFactory.openSession();

            // Récupération des constructeurs depuis la base de données
            Query query = session.createQuery("SELECT fd FROM FlightDetails fd, Plane p, Flight f WHERE p.isActive = true AND fd.isActive = true AND f.isActive = true AND fd.plane = p AND fd.flight = f AND p.ARN = :ARN AND fd.arrivaleDateTime >= NOW() ORDER BY fd.departureDateTime, fd.arrivaleDateTime");
            query.setParameter("ARN", ARN);
            flightDetails = query.getResultList();

            // Création de la réponse JSON
            status = "OK";
            code = 200;
            response = new WsGetFlightDetails(status, message, code, flightDetails);
        } catch (Exception e) {
            message = e.getMessage();
            response = new WsGetFlightDetails(status, message, code, null);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return response;
    }

    public static WsResponse create(final String acceptLanguage, final MultivaluedMap<String, String> formParams) throws JSONException {

        // Initialisation de la réponse
        Session session = null;
        String status = "KO";
        String message = null;
        int code = 500;

        // Récupération de la langue de l'utilisateur
        String languageCode = MessageService.processAcceptLanguage(acceptLanguage);

        Plane plane;

        try {
            session = Database.sessionFactory.openSession();

            // Vérification des paramètres
            if (!isValidPlane(formParams, false)) {
                code = 400;
                throw new Exception(MessageService.getMessageFromCode("invalid_plane", languageCode).text + " 'ARN', 'model'");
            }

            String ARN = formParams.get("ARN").get(0);
            long model_id = Long.parseLong(formParams.get("model").get(0));

            // Vérification de l'existence du modèle
            Model model = session.find(Model.class, model_id);
            if (model == null || !model.isActive) {
                code = 400;
                throw new Exception(MessageService.getMessageFromCode("model_does_not_exist", languageCode).text);
            }

            // Vérification de l'existence de l'avion
            Query query = session.createQuery("FROM Plane WHERE ARN = :arn");
            query.setParameter("arn", ARN);
            List<Plane> planes = query.getResultList();

            session.getTransaction().begin();

            if (planes.size() == 1) {
                plane = planes.get(0);
                if (plane.isActive) {
                    code = 400;
                    throw new Exception(MessageService.getMessageFromCode("plane_already_exists", languageCode).text);
                } else {
                    plane.model = model;
                    plane.isUnderMaintenance = false;
                }
            } else {
                // Création de l'avion
                plane = new Plane();
                plane.ARN = ARN;
                plane.model = model;
            }
            plane.isActive = true;

            // Validation des changements
            session.persist(plane);
            session.flush();
            session.getTransaction().commit();

            status = "OK";
            code = 201;
        } catch (NumberFormatException e) {
            try {
                message = "'model' " + MessageService.getMessageFromCode("is_not_an_integer", languageCode).text;
                code = 400;
                if (message == null) {
                    code = 500;
                }
            } catch (MessageException me) {
                message = null;
            }

        } catch (Exception e) {
            message = e.getMessage();
            session.getTransaction().rollback();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return new WsResponse(status, message, code);
    }

    public static WsResponse update(final String acceptLanguage, final MultivaluedMap<String, String> formParams) throws JSONException {

        // Initialisation de la réponse
        Session session = null;
        String status = "KO";
        String message = null;
        int code = 500;
        int conversion_step = 0;

        // Récupération de la langue de l'utilisateur
        String languageCode = MessageService.processAcceptLanguage(acceptLanguage);

        try {
            session = Database.sessionFactory.openSession();

            // Vérification des paramètres
            if (!isValidPlane(formParams, true)) {
                code = 400;
                throw new Exception(MessageService.getMessageFromCode("invalid_plane", languageCode).text + " 'ARN'");
            }

            // Récupération des paramètres
            String ARN = formParams.get("ARN").get(0);
            long model_id = -1;
            if (formParams.containsKey("model")) {
                model_id = Long.parseLong(formParams.get("model").get(0));
            }
            Boolean isUnderMaintenance = null;
            if (formParams.containsKey("isUnderMaintenance")) {
                String isUnderMaintenanceStr = formParams.get("isUnderMaintenance").get(0).trim().toLowerCase();
                if (isUnderMaintenanceStr.equals("true") || isUnderMaintenanceStr.equals("false")) {
                    isUnderMaintenance = Boolean.parseBoolean(formParams.get("isUnderMaintenance").get(0));
                } else {
                    code = 400;
                    throw new Exception("'isUnderMaintenance' " + MessageService.getMessageFromCode("is_not_a_boolean", languageCode).text);
                }
            }

            // Récupération de l'avion
            Plane plane = session.find(Plane.class, ARN);

            if (plane == null || !plane.isActive) {
                code = 400;
                throw new Exception(MessageService.getMessageFromCode("plane_does_not_exist", languageCode).text);
            }

            // Modification de l'avion
            if (model_id > 0) {
                // Vérification de l'existence du modèle
                Model model = ModelService.exists(model_id);
                if (model == null) {
                    throw new Exception(MessageService.getMessageFromCode("model_does_not_exist", languageCode).text + " 'id'");
                }
                plane.model = model;
            }
            if (isUnderMaintenance != null) plane.isUnderMaintenance = isUnderMaintenance;

            // Persistence du constructeur
            session.getTransaction().begin();
            session.persist(plane);
            session.flush();
            session.getTransaction().commit();

            status = "OK";
            code = 200;
        } catch (NumberFormatException e) {
            try {
                message = "'id': " + MessageService.getMessageFromCode("is_not_an_integer", languageCode).text;
                code = 400;
            } catch (MessageException me) {
                message = me.getMessage();
            }
        } catch (Exception e) {
            message = e.getMessage();
            session.getTransaction().rollback();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        return new WsResponse(status, message, code);
    }

    public static WsResponse delete(final String acceptLanguage, final String ARN) throws JSONException {

        // Initialisation de la réponse
        Session session = null;
        String status = "KO";
        String message = null;
        int code = 500;

        // Récupération de la langue de l'utilisateur
        String languageCode = MessageService.processAcceptLanguage(acceptLanguage);

        Plane plane = null;

        try {
            session = Database.sessionFactory.openSession();

            plane = session.find(Plane.class, ARN);

            // Vérification de l'existence de l'avion
            if (plane == null || !plane.isActive) {
                code = 400;
                throw new Exception(MessageService.getMessageFromCode("plane_does_not_exist", languageCode).text);
            }
            plane.isActive = false;

            // Persistence du model
            session.getTransaction().begin();
            session.persist(plane);
            session.flush();
            session.getTransaction().commit();

            // Création de la réponse JSON
            status = "OK";
            code = 200;
        } catch (Exception e) {
            message = e.getMessage();
            session.getTransaction().rollback();
        }

        return new WsResponse(status, message, code);
    }

    private static boolean isValidPlane(final MultivaluedMap<String, String> formParams, boolean isUpdateOrDelete) {
        if (!isUpdateOrDelete && !formParams.containsKey("model")) {
            return false;
        }
        return formParams.containsKey("ARN");
    }

    public static Plane exists(String pk) {
        Session session = null;

        try {
            session = Database.sessionFactory.openSession();

            // Récupération du vol
            Query query = session.createQuery("FROM Plane WHERE ARN = :ARN");
            query.setParameter("ARN", pk);
            List<Plane> planes = query.getResultList();
            if (planes.size() != 1 || !planes.get(0).isActive) {
                return null;
            }
            return planes.get(0);
        } catch (Exception e) {
            return null;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
