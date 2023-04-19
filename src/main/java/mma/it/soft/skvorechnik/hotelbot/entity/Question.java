package mma.it.soft.skvorechnik.hotelbot.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long chatGestID;
    private Long chatAdminID;
    private String username;
    private String firstName;
    private String lastName;
    @Column(length = 10000)
    private String question;
    @Column(length = 10000)
    private String answer;
    private LocalDateTime lastLogTime;
    private Boolean processed;

    public Question() {
    }

    public Question(Long chatGestID, Long chatAdminID, String username, String firstName, String lastName, String question, String answer, LocalDateTime lastLogTime, Boolean processed) {
        this.chatGestID = chatGestID;
        this.chatAdminID = chatAdminID;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.question = question;
        this.answer = answer;
        this.lastLogTime = lastLogTime;
        this.processed = processed;
    }

    public Long getChatGestID() {
        return chatGestID;
    }

    public void setChatGestID(Long chatGestID) {
        this.chatGestID = chatGestID;
    }

    public Long getChatAdminID() {
        return chatAdminID;
    }

    public void setChatAdminID(Long chatAdminID) {
        this.chatAdminID = chatAdminID;
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

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public LocalDateTime getLastLogTime() {
        return lastLogTime;
    }

    public void setLastLogTime(LocalDateTime lastLogTime) {
        this.lastLogTime = lastLogTime;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
