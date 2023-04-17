package mma.it.soft.skvorechnik.hotelbot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long chatID;
    private String username;
    private String firstName;
    private String lastName;
    private String text;
    private LocalDateTime date;

    public Review(Long id, String username, String firstName, String lastName, String text, LocalDateTime date) {
        this.chatID = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.text = text;
        this.date = date;
    }

    public Review() {

    }
}
