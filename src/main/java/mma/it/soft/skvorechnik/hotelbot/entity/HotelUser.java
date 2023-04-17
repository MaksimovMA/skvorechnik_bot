package mma.it.soft.skvorechnik.hotelbot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class HotelUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long chatID;
    private String username;
    private String firstName;
    private String lastName;
    private String text;
    private LocalDateTime lastLogTime;

    public HotelUser() {
    }
    public HotelUser(Long chatID, String username, String firstName, String lastName, String text, LocalDateTime lastLogTime) {
        this.chatID = chatID;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.text = text;
        this.lastLogTime = lastLogTime;
    }


    public Long getChatID() {
        return chatID;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
