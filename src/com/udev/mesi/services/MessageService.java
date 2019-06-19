package com.udev.mesi.services;

import com.udev.mesi.Database;
import com.udev.mesi.exceptions.MessageException;
import com.udev.mesi.exceptions.MessageNotFoundException;
import com.udev.mesi.models.WsMessage;
import main.java.com.udev.mesi.entities.Message;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

public class MessageService {
    public static WsMessage getMessageFromCode(String messageCode, String languageCode) throws MessageException {
        try {
            // Création du gestionnaire d'entités
            EntityManagerFactory emf = Persistence.createEntityManagerFactory(Database.UNIT_NAME);
            EntityManager em = emf.createEntityManager();

            // Récupération du message
            Query query = em.createQuery("SELECT m FROM Message m, Language l WHERE l.id = m.language AND m.code = :messageCode AND l.code = :languageCode");
            query.setParameter("messageCode", messageCode);
            query.setParameter("languageCode", languageCode);
            List<Message> messages = query.getResultList();
            if (messages.size() > 0) {
                return messages.get(0).toWs(false);
            } else {
                throw new MessageNotFoundException("Le message '" + messageCode + "' pour le langage '" + languageCode + "' n'existe pas");
            }
        } catch (ClassCastException e) {
            throw new MessageException("Une erreur inconnue est survenue lors de la lecture du message '" + messageCode + "' pour le langage '" + languageCode + "'");
        }
    }
}
