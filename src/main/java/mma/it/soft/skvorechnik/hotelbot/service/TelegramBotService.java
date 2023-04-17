package mma.it.soft.skvorechnik.hotelbot.service;


import mma.it.soft.skvorechnik.hotelbot.HotelBot;
import mma.it.soft.skvorechnik.hotelbot.repository.AdminUserRepository;
import mma.it.soft.skvorechnik.hotelbot.repository.HotelUserRepository;
import mma.it.soft.skvorechnik.hotelbot.repository.QuestionRepository;
import mma.it.soft.skvorechnik.hotelbot.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Service
public class TelegramBotService {

    private ReviewRepository reviewRepository;
    private HotelUserRepository userRepository;
    private AdminUserRepository adminUserRepository;
    private QuestionRepository questionRepository;

    public TelegramBotService(ReviewRepository reviewRepository, HotelUserRepository userRepository, AdminUserRepository adminUserRepository, QuestionRepository questionRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.adminUserRepository = adminUserRepository;
        this.questionRepository = questionRepository;
        TelegramBotsApi telegramBotsApi = null;
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        try {
            telegramBotsApi.registerBot(new HotelBot(this.reviewRepository, this.userRepository, this.adminUserRepository, this.questionRepository));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }
}