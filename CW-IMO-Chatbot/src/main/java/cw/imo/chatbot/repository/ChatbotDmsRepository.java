package cw.imo.chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cw.imo.chatbot.model.ChatbotDms;

public interface ChatbotDmsRepository extends JpaRepository<ChatbotDms, String> {

	boolean existsByDocumentName(String string);

	ChatbotDms findByDocumentName(String folderName);

}
