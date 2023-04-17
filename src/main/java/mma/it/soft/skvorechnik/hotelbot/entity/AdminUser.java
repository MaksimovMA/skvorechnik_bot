package mma.it.soft.skvorechnik.hotelbot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class AdminUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long chatID;
    private String username;
    private String firstName;
    private String lastName;
    private String text;
    private LocalDateTime lastLogTime;
    private Boolean adminActive;

    public AdminUser() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getLastLogTime() {
        return lastLogTime;
    }

    public void setLastLogTime(LocalDateTime lastLogTime) {
        this.lastLogTime = lastLogTime;
    }

    public Boolean getAdminActive() {
        return adminActive;
    }

    public void setAdminActive(Boolean adminActive) {
        this.adminActive = adminActive;
    }

    public AdminUser(Long chatID, String username, String firstName, String lastName, String text, LocalDateTime lastLogTime, Boolean adminActive) {
        this.chatID = chatID;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.text = text;
        this.lastLogTime = lastLogTime;
        this.adminActive = adminActive;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getChatID() {
        return chatID;
    }
}
