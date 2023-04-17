package mma.it.soft.skvorechnik.hotelbot;

import mma.it.soft.skvorechnik.hotelbot.entity.AdminUser;
import mma.it.soft.skvorechnik.hotelbot.entity.HotelUser;
import mma.it.soft.skvorechnik.hotelbot.entity.Question;
import mma.it.soft.skvorechnik.hotelbot.entity.Review;
import mma.it.soft.skvorechnik.hotelbot.repository.AdminUserRepository;
import mma.it.soft.skvorechnik.hotelbot.repository.HotelUserRepository;
import mma.it.soft.skvorechnik.hotelbot.repository.QuestionRepository;
import mma.it.soft.skvorechnik.hotelbot.repository.ReviewRepository;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class HotelBot extends TelegramLongPollingBot {

    private Map<Long, BotState> userStates = new HashMap<>();
    private Map<Long, Long> adminToQuestion = new HashMap<>();

    String sendBroadcastPrefix = "Skvoreshniki-post";
    String adminONPrefix = "Skadminon";
    String adminAddPrefix = "Skadminadd";
    String adminOFFPrefix = "Skadminoff";

    private enum BotState {
        WAITING_FOR_COMMAND,
        WAITING_FOR_REVIEW,
        WAiTING_FOR_ADMIN,
        WAITING_FOR_ANSWER_TO_GEST
    }

    private final String BOT_TOKEN = "5961355034:AAHq881xoYrOQpqb1Lul8i7ObnG5PeYY9zI";

    private final ReviewRepository reviewRepository;
    private final HotelUserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final QuestionRepository questionRepository;

    public HotelBot(ReviewRepository reviewRepository, HotelUserRepository userRepository, AdminUserRepository adminUserRepository, QuestionRepository questionRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.adminUserRepository = adminUserRepository;
        this.questionRepository = questionRepository;
    }

    // Замените эти значения на свои данные
    private final String BOT_USERNAME = "skvoreshniki_bot";

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();

            BotState userState = userStates.getOrDefault(chatId, BotState.WAITING_FOR_COMMAND);

            switch (userState) {
                case WAITING_FOR_COMMAND:
                    handleWaitingForCommand(update);
                    break;
                case WAITING_FOR_REVIEW:
                    handleWaitingForReview(update);
                    break;
                case WAiTING_FOR_ADMIN:
                    handleWaitingForAdmin(update);
                case WAITING_FOR_ANSWER_TO_GEST:
                    handleWaitingForAnswerToGuest(update);
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleWaitingForCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        String userName = update.getMessage().getFrom().getUserName();
        String firstName = update.getMessage().getFrom().getFirstName();
        String lastName = update.getMessage().getFrom().getLastName();

        if (messageText.equals("Старт")) {
            message.setText("Добро пожаловать в наш отель! Выберите опцию:");
            saveUserToDatabase(chatId,userName, firstName, lastName, messageText);
            message.setReplyMarkup(getMainMenuKeyboard());
        } else {
            if (messageText.startsWith(adminAddPrefix)) {
                message.setText("Новый админ %s %s зарегестрирован(a)".formatted(firstName, lastName));
                saveAdminUserToDatabase(chatId, userName, firstName, lastName, messageText,true);
            } else if (messageText.startsWith(adminOFFPrefix)) {
                message.setText("Режим админа выключен");
                AdminUser adminUser = adminUserRepository.findByChatID(chatId);
                adminUser.setAdminActive(false);
                adminUserRepository.save(adminUser);
            } else if (messageText.startsWith(adminONPrefix)) {
                message.setText("Режим админа включен");
                AdminUser adminUser = adminUserRepository.findByChatID(chatId);
                adminUser.setAdminActive(true);
                adminUserRepository.save(adminUser);
            } else if (messageText.startsWith(sendBroadcastPrefix)) {
                handleSendBroadcast(update, messageText);
                message.setText("Команда Скворешников");
            } else if (messageText.equals("Отзыв")) {
                message.setText("Мы очень рады, что Вы нашли пару минут оставить отзыв, похвалить нас, а может и указать нам на наши недочеты) Напишите ваш отзыв ниже:");
                saveUserToDatabase(chatId, userName, firstName, lastName, messageText);
                userStates.put(chatId, BotState.WAITING_FOR_REVIEW);
            } else {
                message.setText("Извините, я не понимаю ваш запрос. Пожалуйста, выберите опцию из меню.");
                saveUserToDatabase(chatId, userName, firstName, lastName, messageText);
                message.setReplyMarkup(getMainMenuKeyboard());
            }
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private InlineKeyboardMarkup getMainMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        List<InlineKeyboardButton> row5 = new ArrayList<>();
        List<InlineKeyboardButton> row6 = new ArrayList<>();
        List<InlineKeyboardButton> row7 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton("\uD83D\uDCDEСвязаться");
        button1.setCallbackData("contact"); // добавляем callbackData
        row1.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton("\uD83D\uDCC5Забронировать");
        button2.setCallbackData("booking"); // добавляем callbackData
        row2.add(button2);

        InlineKeyboardButton button3 = new InlineKeyboardButton("\uD83D\uDCCCКак добраться?");
        button3.setCallbackData("route_info"); // добавляем callbackData
        row3.add(button3);

        InlineKeyboardButton button4 = new InlineKeyboardButton("\uD83D\uDCD6Важная информация по заезду");
        button4.setCallbackData("important_info"); // добавляем callbackData
        row4.add(button4);

        InlineKeyboardButton button5 = new InlineKeyboardButton("❓Популярные вопросы");
        button5.setCallbackData("top_question"); // добавляем callbackData
        row5.add(button5);

        InlineKeyboardButton button6 = new InlineKeyboardButton("\uD83D\uDCCBОставить отзыв");
        button6.setCallbackData("review"); // добавляем callbackData
        row6.add(button6);

        InlineKeyboardButton button7 = new InlineKeyboardButton("\uD83D\uDC69\u200D\uD83D\uDCBCЗадать вопрос администратору");
        button7.setCallbackData("ask_admin"); // добавляем callbackData
        row7.add(button7);



        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboard.add(row5);
        keyboard.add(row6);
        keyboard.add(row7);

        markup.setKeyboard(keyboard);
        return markup;
    }

    private InlineKeyboardMarkup getContactMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();


        InlineKeyboardButton button1 = new InlineKeyboardButton("\uD83D\uDCDEПозвонить");
        button1.setCallbackData("call"); // добавляем callbackData
        row1.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton("Написать WhatsApp");
        button2.setCallbackData("whatsApp"); // добавляем callbackData
        row1.add(button2);

        InlineKeyboardButton button3 = new InlineKeyboardButton("\uD83D\uDCE7Написать письмо");
        button3.setCallbackData("email"); // добавляем callbackData
        row2.add(button3);

        InlineKeyboardButton button4 = new InlineKeyboardButton("Мы Вконтакте");
        button4.setCallbackData("vk"); // добавляем callbackData
        row2.add(button4);

        InlineKeyboardButton button5 = new InlineKeyboardButton("Мы в Интсаграме");
        button5.setCallbackData("instagram"); // добавляем callbackData
        row3.add(button5);

        InlineKeyboardButton button6 = new InlineKeyboardButton("Оставить отзыв");
        button6.setCallbackData("review"); // добавляем callbackData
        row3.add(button6);

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        markup.setKeyboard(keyboard);
        return markup;
    }

    private InlineKeyboardMarkup getCallMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton("Отдел бронирования");
        button1.setCallbackData("call_booking"); // добавляем callbackData
        row1.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton("Администратор");
        button2.setCallbackData("call_admin"); // добавляем callbackData
        row1.add(button2);

        keyboard.add(row1);

        markup.setKeyboard(keyboard);
        return markup;
    }

    private InlineKeyboardMarkup getRouteMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton("\uD83D\uDEB6Своим ходом");
        button1.setCallbackData("walk_route"); // добавляем callbackData
        row1.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton("\uD83D\uDE97На машине");
        button2.setCallbackData("car_route"); // добавляем callbackData
        row1.add(button2);

        keyboard.add(row1);

        markup.setKeyboard(keyboard);
        return markup;
    }

    private InlineKeyboardMarkup getBookMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton("\uD83C\uDFE0Через наш сайт");
        button1.setCallbackData("sk_site"); // добавляем callbackData
        row1.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton("Через системы-посредники");
        button2.setCallbackData("book_systems"); // добавляем callbackData
        row2.add(button2);

        keyboard.add(row1);
        keyboard.add(row2);

        markup.setKeyboard(keyboard);
        return markup;

    }

    private InlineKeyboardMarkup getBookSystemsMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        List<InlineKeyboardButton> row5 = new ArrayList<>();
        List<InlineKeyboardButton> row6 = new ArrayList<>();
        List<InlineKeyboardButton> row7 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton("OneTwoTrip");
        button1.setCallbackData("oneTwoTrip"); // добавляем callbackData
        row1.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton("Оstrovok.ru");
        button2.setCallbackData("ostrovok"); // добавляем callbackData
        row2.add(button2);

        InlineKeyboardButton button3 = new InlineKeyboardButton("https://ostrovok.ru/");
        button3.setCallbackData("yandex"); // добавляем callbackData
        row3.add(button3);

        InlineKeyboardButton button4 = new InlineKeyboardButton("Bronevik.ru");
        button4.setCallbackData("bronevik"); // добавляем callbackData
        row4.add(button4);

        InlineKeyboardButton button5 = new InlineKeyboardButton("Alean");
        button5.setCallbackData("alean"); // добавляем callbackData
        row5.add(button5);

        InlineKeyboardButton button6 = new InlineKeyboardButton("Sutochno.ru");
        button6.setCallbackData("sutochno"); // добавляем callbackData
        row6.add(button6);

        InlineKeyboardButton button7 = new InlineKeyboardButton("Твил");
        button7.setCallbackData("tvil"); // добавляем callbackData
        row7.add(button7);



        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboard.add(row5);
        keyboard.add(row6);
        keyboard.add(row7);

        markup.setKeyboard(keyboard);
        return markup;
    }


    private ReplyKeyboard getBackQuestionMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton("\uD83D\uDD19Назад");
        button1.setCallbackData("top_question"); // добавляем callbackData
        row1.add(button1);


        InlineKeyboardButton button2 = new InlineKeyboardButton("Главное меню");
        button2.setCallbackData("MainMenu"); // добавляем callbackData
        row2.add(button2);

        keyboard.add(row1);
        keyboard.add(row2);

        markup.setKeyboard(keyboard);
        return markup;
    }

    private ReplyKeyboard getBackToMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton("Главное меню");
        button1.setCallbackData("MainMenu"); // добавляем callbackData
        row1.add(button1);

        keyboard.add(row1);

        markup.setKeyboard(keyboard);
        return markup;
    }




    private InlineKeyboardMarkup getAdminChatMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton("Ответить");
        button1.setCallbackData("answerToGuest"); // добавляем callbackData
        row1.add(button1);

        keyboard.add(row1);
        markup.setKeyboard(keyboard);
        return markup;
    }

    private ReplyKeyboard getQuestionMenuKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        List<InlineKeyboardButton> row5 = new ArrayList<>();
        List<InlineKeyboardButton> row6 = new ArrayList<>();
        List<InlineKeyboardButton> row7 = new ArrayList<>();
        List<InlineKeyboardButton> row8 = new ArrayList<>();
        List<InlineKeyboardButton> row9 = new ArrayList<>();
        List<InlineKeyboardButton> row10 = new ArrayList<>();
        List<InlineKeyboardButton> row11 = new ArrayList<>();
        List<InlineKeyboardButton> row12 = new ArrayList<>();
        List<InlineKeyboardButton> row13 = new ArrayList<>();
        List<InlineKeyboardButton> row14 = new ArrayList<>();
        List<InlineKeyboardButton> row15 = new ArrayList<>();
        List<InlineKeyboardButton> row16 = new ArrayList<>();
        List<InlineKeyboardButton> row17 = new ArrayList<>();
        List<InlineKeyboardButton> row18 = new ArrayList<>();
        List<InlineKeyboardButton> row19 = new ArrayList<>();
        List<InlineKeyboardButton> row20 = new ArrayList<>();
        List<InlineKeyboardButton> row21 = new ArrayList<>();
        List<InlineKeyboardButton> row22 = new ArrayList<>();



        InlineKeyboardButton button1 = new InlineKeyboardButton("Время выезда и заезда");
        button1.setCallbackData("question_1"); // добавляем callbackData
        row1.add(button1);


        InlineKeyboardButton button2 = new InlineKeyboardButton("Что включено в стоимость?");
        button2.setCallbackData("question_2"); // добавляем callbackData
        row2.add(button2);

        InlineKeyboardButton button3 = new InlineKeyboardButton("Парковка");
        button3.setCallbackData("question_3"); // добавляем callbackData
        row3.add(button3);

        InlineKeyboardButton button4 = new InlineKeyboardButton("Есть ли рядом магазин ?");
        button4.setCallbackData("question_4"); // добавляем callbackData
        row4.add(button4);

        InlineKeyboardButton button5 = new InlineKeyboardButton("А что есть в домике?");
        button5.setCallbackData("question_5"); // добавляем callbackData
        row5.add(button5);

        InlineKeyboardButton button6 = new InlineKeyboardButton("Какие различия между апартаментами?");
        button6.setCallbackData("question_6"); // добавляем callbackData
        row6.add(button6);

        InlineKeyboardButton button7 = new InlineKeyboardButton("Сдается весь домик или один этаж? ");
        button7.setCallbackData("question_7"); // добавляем callbackData
        row7.add(button7);

        InlineKeyboardButton button8 = new InlineKeyboardButton("Есть ли залог?");
        button8.setCallbackData("question_8"); // добавляем callbackData
        row8.add(button8);

        InlineKeyboardButton button9 = new InlineKeyboardButton("К вам можно с питомцем?");
        button9.setCallbackData("question_9"); // добавляем callbackData
        row9.add(button9);

        InlineKeyboardButton button10 = new InlineKeyboardButton("У вас есть где погулять?");
        button10.setCallbackData("question_10"); // добавляем callbackData
        row10.add(button10);

        InlineKeyboardButton button11 = new InlineKeyboardButton("У вас есть детские площадки?");
        button11.setCallbackData("question_11"); // добавляем callbackData
        row11.add(button11);

        InlineKeyboardButton button12 = new InlineKeyboardButton("У вас есть баня?");
        button12.setCallbackData("question_12"); // добавляем callbackData
        row12.add(button12);

        InlineKeyboardButton button13 = new InlineKeyboardButton("Кафе, рестораны, доставка еды?");
        button13.setCallbackData("question_13"); // добавляем callbackData
        row13.add(button13);

        InlineKeyboardButton button14 = new InlineKeyboardButton("Как добраться своим ходом?");
        button14.setCallbackData("question_14"); // добавляем callbackData
        row14.add(button14);

        InlineKeyboardButton button15 = new InlineKeyboardButton("Что такое дуплекс?");
        button15.setCallbackData("question_15"); // добавляем callbackData
        row15.add(button15);

        InlineKeyboardButton button16 = new InlineKeyboardButton("Есть ли мангалы? Решетки и шампура");
        button16.setCallbackData("question_16"); // добавляем callbackData
        row16.add(button16);

        InlineKeyboardButton button17 = new InlineKeyboardButton("Есть скидка на др?");
        button17.setCallbackData("question_17"); // добавляем callbackData
        row17.add(button17);

        InlineKeyboardButton button18 = new InlineKeyboardButton("А какие есть скидки?");
        button18.setCallbackData("question_18"); // добавляем callbackData
        row18.add(button18);

        InlineKeyboardButton button19 = new InlineKeyboardButton("У вас полная предоплата?");
        button19.setCallbackData("question_19"); // добавляем callbackData
        row19.add(button19);

        InlineKeyboardButton button20 = new InlineKeyboardButton("Где вас можно найти, ВК, инстаграм?");
        button20.setCallbackData("question_20"); // добавляем callbackData
        row20.add(button20);

        InlineKeyboardButton button21 = new InlineKeyboardButton("Я боюсь бронировать , вы точно не мошенники");
        button21.setCallbackData("question_21"); // добавляем callbackData
        row21.add(button21);

        InlineKeyboardButton button22 = new InlineKeyboardButton("Есть ли отдельное пространство для компаний");
        button22.setCallbackData("question_22"); // добавляем callbackData
        row22.add(button22);

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboard.add(row5);
        keyboard.add(row6);
        keyboard.add(row7);
        keyboard.add(row8);
        keyboard.add(row9);
        keyboard.add(row10);
        keyboard.add(row11);
        keyboard.add(row12);
        keyboard.add(row13);
        keyboard.add(row14);
        keyboard.add(row15);
        keyboard.add(row16);
        keyboard.add(row17);
        keyboard.add(row18);
        keyboard.add(row19);
        keyboard.add(row20);
        keyboard.add(row21);
        keyboard.add(row22);

        markup.setKeyboard(keyboard);
        return markup;
    }


    private void handleSendBroadcast(Update update, String messageText) {
        String broadcastMessage = messageText.substring(sendBroadcastPrefix.length());
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());

        if (broadcastMessage != null && !broadcastMessage.isEmpty()) {
            sendBroadcastMessage(broadcastMessage);
        } else {
            message.setText("Вы не ввели текст рассылки!");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleWaitingForReview(Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        User user = message.getFrom();
        String userName = user.getUserName();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String text = message.getText();
        LocalDateTime localDateTime = LocalDateTime.now();

        saveReviewToDatabase(chatId, userName, firstName, lastName, text, localDateTime);
        userStates.put(chatId, BotState.WAITING_FOR_COMMAND);
        sendMessage(950452865, lastName + " " + firstName + " Оставил новый отзыв: " + text);

        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        response.setText("Благодарим Вас за обратную связь!\n" +
                "Рады объявить Вас почетным гостем к оттеджей Скворешники и презентовать Вам промокод - \"СвояПтица\" на скидку 5% на все последующие бронирования! Чтобы получить скидку, используйте промокод при бронировании через модуль онлайн бронирования)");
        try {
            execute(response);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleWaitingForAdmin(Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        User user = message.getFrom();
        String userName = user.getUserName();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String text = message.getText();
        LocalDateTime localDateTime = LocalDateTime.now();
        Question question = new Question(chatId,null,userName,firstName,lastName,text,null,localDateTime,false);
        questionRepository.save(question);
        userStates.put(chatId, BotState.WAITING_FOR_COMMAND);
        message.setReplyMarkup(getAdminChatMenuKeyboard());
        sendMessageToAdmin(1702253908,
                question.getId() +"."+ lastName + " " + firstName + " Задал(а) вопрос администратору: " + text, message);

        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        response.setText("Администратор получил ваш вопрос и скоро он ответит вам.");
        try {
            execute(response);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }



    private void handleWaitingForAnswerToGuest(Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();
        User user = message.getFrom();
        String userName = user.getUserName();
        String text = message.getText();
        Long questionID = adminToQuestion.get(chatId);
        Optional<Question> questionOptional = questionRepository.findById(questionID);
        Question question = questionOptional.get();
        sendMessage(question.getChatGestID(), "Администратор " + userName +": "+ message.getText());
        question.setAnswer(text);
        question.setProcessed(true);
        questionRepository.save(question);
        userStates.put(chatId, BotState.WAITING_FOR_COMMAND);
        message.setReplyMarkup(getAdminChatMenuKeyboard());
        SendMessage response = new SendMessage();
        response.setChatId(chatId);
        try {
            execute(response);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendBroadcastMessage(String text) {
        Set<HotelUser> hotelUsers = userRepository.findAll();
        Set<Long> subscribers = hotelUsers.stream().map(HotelUser::getChatID).collect(Collectors.toSet());
        for (Long chatId : subscribers) {
            sendMessage(chatId, text);
        }
    }

    private void sendMessageToAdmin(long chatId, String text, Message messagetoAdmin) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(messagetoAdmin.getReplyMarkup());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }



        private void handleCallbackQuery(CallbackQuery callbackQuery) {
            String callbackData = callbackQuery.getData();
            long chatId = callbackQuery.getMessage().getChatId();

            SendMessage message = new SendMessage();
            message.setChatId(chatId);

            switch (callbackData) {

            case "ask_admin" -> {
                message.setText("Задайте вопрос администратору:");
                userStates.put(chatId, BotState.WAiTING_FOR_ADMIN);
            }

            case "answerToGuest" -> { message.setText("Ответ:");
                String text = (callbackQuery.getMessage().getText());
                String questionId = text.substring(0,text.indexOf('.'));
                Optional<Question> optionalQuestion = questionRepository.findById(Long.valueOf(questionId));
                Question question = optionalQuestion.get();
                question.setChatAdminID(chatId);
                questionRepository.save(question);
                adminToQuestion.put(chatId, Long.valueOf(questionId));
                userStates.put(chatId, BotState.WAITING_FOR_ANSWER_TO_GEST);
            }

            case "contact" -> {
                message.setText("Связаться");
                message.setReplyMarkup(getContactMenuKeyboard());
            }
            case "call" -> {
                message.setText("Позвонить");
                message.setReplyMarkup(getCallMenuKeyboard());
            }
            case "call_booking" -> message.setText("8 (931) 588-53-39");
            case "call_admin" -> message.setText("8 (921) 886-66-44");
            case "whatsApp" -> message.setText("8 (931) 588-53-39");
            case "email" -> message.setText("skvoreshniki-apart@yandex.ru");
            case "vk" -> message.setText("https://vk.com/skvoreshnikiapart");
            case "instagram" -> message.setText("https://instagram.com/skvoreshnikiapart");
            case "sk_site" -> message.setText("https://reservationsteps.ru/rooms/index/21690a52-1665-49cb-86e3-5061050c58ea");
            case "booking" -> {
                message.setText("Забронировать");
                message.setReplyMarkup(getBookMenuKeyboard());
            }
            case "book_systems" -> {
                    message.setText("Через системы-посредники");
                    message.setReplyMarkup(getBookSystemsMenuKeyboard());
                }
            case "oneTwoTrip" ->  message.setText("https://www.onetwotrip.com/");
            case "ostrovok" ->  message.setText("https://ostrovok.ru/");
            case "yandex" ->  message.setText("https://travel.yandex.ru/");
            case "bronevik" ->  message.setText("https://bronevik.com/");
            case "alean" ->  message.setText("https://www.alean.ru/");
            case "sutochno" ->  message.setText("https://sutochno.ru/");
            case "tvil" ->  message.setText("https://tvil.ru/");
            case "route_info" -> {
                    message.setText("Через системы-посредники");
                    message.setReplyMarkup(getRouteMenuKeyboard());
                }
            case "walk_route" -> message.setText("Без машины удобнее всего добираться от станции метро Беговая.\n" +
                    "Остановка Станция метро Беговая ( в сторону на Сестрорецк) - автобусы 101А, 211, 216, 216А, 303, 600 до остановки Военная улица. Лисий нос, и еще 5 минут пешком до нас.\n" +
                    "На такси от метро Беговая - около 10 минут езды. ( Приморское шоссе 96)\n" +
                    "На электричке от Финляндского вокзала, до станции Лисий нос . От нее около 18 минут ходьбы.");
            case "car_route" -> message.setText("https://yandex.ru/maps/-/CCU85Ovg0D");
            case "important_info" -> message.setText("Наш адрес: Санкт-Петербург, поселок Лисий Нос, Приморское шоссе, 96. Въезд на территорию - прямо с шоссе (серый каменный забор, деревянные ворота).\n" +
                    "На территории постоянно находится дежурный администратор. Телефон для связи 8 (921) 588-53-39. Звоните ей по прибытии, и она поможет с заселением. \n" +
                    "\n" +
                    "Взимается депозит 3000р\\на апартамент. \n" +
                    "\n" +
                    "Парковка на территории бесплатная при наличии мест. Заезд с 14.00 до 23.00, выезд до 12.00. Если вы не на машине, удобнее всего добраться от м.Беговая на автобусе № 101, 211, 216, 303, 600 или на маршрутках № 305, 400, 405, 417. Остановка \"Лисий нос. Военная улица\". Время в пути 18минут.\n" +
                    "\n" +
                    "На территории работает  Wi-Fi - логин и пароль Вы можете найти в папке гостя в вашем номере.\n" +
                    "\n" +
                    "В кухне имеется следующий набор посуды и бытовой техники на 4 чел: микроволновка, холодильник, однокомфорочная индукционная плита, кастрюля, сковорода, ковш, чайник, френчпресс для кофе/чая, тарелки плоские и глубокие, кружки, бокалы, столовые приборы, ножи, доска разделочная, штопор. В санузле имеется фен. В спальне - постельное белье и по 2 полотенца на каждого гостя.\n" +
                    "\n" +
                    "Рекомендуем покупать бутилированную питьевую воду.\n" +
                    "Мы ожидаем, что гости помоют за собой посуду при выезде.\n" +
                    "Убедительно просим не смывать в унитаз туалетную бумагу и другие предметы.\n" +
                    "Курить в апартаментах строго запрещено.\n" +
                    "Выгуливать собак на территории запрещено. \n" +
                    "Администратор при выезде проверяет апарт на предмет наличия и сохранности всего, что было передано гостю в аренду.\n" +
                    "\n" +
                    "К ВАШИМ УСЛУГАМ ТАКЖЕ :\n" +
                    "- Мангал и шампуры/решетка (входят в стоимость проживания)\n" +
                    "- Уголь 400 руб, розжиг 200 руб.\n" +
                    "- Садовая мебель рядом с жилыми модулями, настольные игры, бадминтон, дартс (бесплатно по запросу у администратора).\n" +
                    "- Зона отдыха с креслами-качелями и костровой чашей (дрова для костровой чаши по запросу у администратора, 200 руб. за связку).\n" +
                    "- Прокат велосипедов: 200 руб. первый час, 150 руб. последующие часы, день/ночь (с 10.00до 21.00,с 21.00 до 11.00 - 900руб. Сутки (24 часа) - 1200 руб. В залог необходимо оставить паспорт/права/свидетельство о рождении.\n" +
                    "\n" +
                    "Продуктовый магазин: Магнит и местный ресторанчик - 5 минут ходьбы налево из ворот. Пляж Морские Дубки- 7 минут на машине или 30 минут пешком, ул. Новоцентральная. Пляж оборудован местами для отдыха и детской площадкой.\n" +
                    "\n" +
                    "Приглашаем также вступить в нашу группу в Инстаграм https://instagram.com/skvoreshnikiapart?igshid=YmMyMTA2M2Y= и VK https://vk.com/skvoreshnikiapart \n" +
                    "\n" +
                    "Приятного отдыха! Всегда рады помочь!\n" +
                    "\n" +
                    "Команда Скворешников");



            case "review" -> {
                    message.setText("Пожалуйста, оставьте свой отзыв:");
                    userStates.put(chatId, BotState.WAITING_FOR_REVIEW);
                }
                case "top_question" -> {
                    message.setText("Популярные вопросы");
                    message.setReplyMarkup(getQuestionMenuKeyboard());
                }


                case "question_1" -> {
                    message.setText("Время заезда - с 14-00.\n" +
                            "Время выезда - до 12-00.\n" +
                            "На территории всегда находится администратор, если вы планируете приехать поздно, предупредите администратора по телефону 8(921)588-53-39");
                    message.setReplyMarkup(getBackQuestionMenuKeyboard());
                }
                case "question_2" -> {
                    message.setText("В стоимость включено проживание, пользование мангальной зоной и садовой мебелью, парковка на территории.\n" +
                            "У администратора можно бесплатно взять шампура и решетки для мангала.\n" +
                            "Большой ассортимент настольных игр - бесплатно");
                    message.setReplyMarkup(getBackQuestionMenuKeyboard());
                }
                case "question_3" -> {
                    message.setText("Парковка для гостей бесплатная и находится на территории. Въезд прямо с Приморского шоссе.");
                    message.setReplyMarkup(getBackQuestionMenuKeyboard());
                }
                case "question_4" -> {
                    message.setText("Рядом с нами расположен магазин «Магнит» - 5 минут ходьбы налево при выходе из наших ворот.\n" +
                            "\n" +
                            "Магазин \"Peaлъ\" - 800 метров (15 минут) направо при выходе из наших ворот.");
                    message.setReplyMarkup(getBackQuestionMenuKeyboard());
                }
                case "question_5" -> {
                    message.setText("Апартаменты состоят из спальни с двуспальной кроватью, совмещенной кухни-гостиной с диваном и собственной ванной комнаты с душем.\n" +
                            "В каждом апарте есть кухня, оборудованная всем необходимым для готовки и обеденный стол, за которым могут разместиться до 4х человек.\n" +
                            "В кухне имеется следующий набор посуды и бытовой техники на 4 чел: микроволновка, холодильник, однокомфорочная индукционная плита, кастрюля, сковорода, ковш, чайник, френчпресс для кофе/чая, тарелки плоские и глубокие, кружки, бокалы, столовые приборы, ножи, доска разделочная, штопор.\n" +
                            "Все апартаменты оснащены теплыми полами в кухне и спальне, и кондиционерами, которые так же могут работать на обогрев. \n" +
                            "В санузле имеется фен.\n" +
                            "Предоставляется постельное белье и полотенца для каждого гостя\n" +
                            "В номерах есть Wi-Fi, ТВ\n" +
                            "На улице возле домиков расположены террасные настилы с садовой мебелью и мангалами.");
                    message.setReplyMarkup(getBackQuestionMenuKeyboard());
                }
                case "question_6" -> {
                    message.setText("Все апартаменты имеют одинаковое оснащение, и незначительное отличие в цветовой гамме.\n" +
                            "Главное отличие апартов это вид из окна.\n" +
                            "У нас есть апарты:\n" +
                            "1) С видом на соседнюю территорию, где расположен сад. \n" +
                            "Преимущества: уединенно, не видно гостей на территории, потише.\n" +
                            "2) С видом на центральное патио, где расположены качели. \n" +
                            "Преимущества: видно что происходит на территории, видна парковка, вечером видны фонарики");
                    message.setReplyMarkup(getBackQuestionMenuKeyboard());
                }
                case "question_7" -> {
                    message.setText("У нас сдаются апартаменты, занимающие целый этаж.\n" +
                            "На данный момент у нас 6 домиков, где сдается только второй этаж, а первый является нежилым. \n" +
                            "В таких домиках внешняя лестница ведет сразу на 2 этаж в ваши апартаменты. \n" +
                            "И т.к 1 этаж является нежилым, то фактически в домике вы находитесь одни.\n" +
                            "Мангальная зона с мангалом и садовой мебелью в таком случае находится перед домиком, или же за ним. \n" +
                            "И только в 1 домике у нас по-отдельности сдаются апартаменты на 1 и на 2 этаже. Домик спроектирован таким образом, что гости не пересекаются вовсе. \n" +
                            "Гости, занимающие 1 этаж, имеют свою мангальную зону за домиком и отдельный вход в апартаменты.\n" +
                            "Гости, занимающие 2 этаж, поднимаются в апартаменты по внешней лестнице с другой стороны домика, и имеют личную мангальную зону перед домиком.\n");
                    message.setReplyMarkup(getBackQuestionMenuKeyboard());
                }
                case "question_8" -> {
                    message.setText("Взимается залог  3000р\\на апартамент. При размещении с собаками взимается повышенный залог 5000р\\на апартамент. При соблюдении правил размещения и отсутствии повреждений в апартаменте залог возвращается в полном размере при выезде.");
                    message.setReplyMarkup(getBackQuestionMenuKeyboard());
                }
                case "question_9" -> {
                    message.setText("Мы принимаем воспитаных собак до 35см в холке и весом не более 10кг. Необходимо сообщать породу собаки при бронировании.\n" +
                            "Доплата за питомца 500р\\сутки.\n" +
                            "Так же обращаем внимание, что при размещении с собачками взимается залог в размере 5000р, а выгул на территории запрещен.\n" +
                            "Проживание с кошками и иными животными, к сожалению, запрещено.");
                    message.setReplyMarkup(getBackQuestionMenuKeyboard());
                }

            case "question_10" -> {
                message.setText("Мы находимся в очень красивом поселке Лисий нос.\n" +
                        "Через поселок можно прогуляться до Финского залива к Лисьему пляжу, или до пляжа Морские Дубки, прогулка в одну сторону займет около 25-30 минут. \n" +
                        "В правой части поселка (ул.Военная) находится лесной массив, поселковые зоны отдыха. \n" +
                        "Если Вы на машине - открывается огромное количество возможностей. Т.к. мы находимся в центре Курортного района поблизости множество заповедников, эко-троп, пляжей и парков. \n" +
                        "Наша любимая эко-тропа называется Сестрорецкое болото, и находится в 20 минутах на авто.\n" +
                        "Так же рекомендуем парк культуры и отдыха Дубки, он подходит для отдыха с детьми.");
                message.setReplyMarkup(getBackQuestionMenuKeyboard());
            }
            case "question_11" -> {
                message.setText("К сожалению детской площадки на территории нет. Ближайшая площадка находится в центре поселка - около Стрелковой ул.4 ( 20 минут пешком), так же детская площадка есть на пляже Морские Дубки, и возле магазина Реалъ \n" +
                        "Для деток у нас есть широкий ассортимент настольных игр, их можно взять бесплатно у администратора. \n" +
                        "Можно взять в прокат велосипеды, бадминтон.");
                message.setReplyMarkup(getBackQuestionMenuKeyboard());
            }
            case "question_12" -> {
                message.setText("К сожалению бани на территории нет. Недалеко от нас находятся Лахтинские бани. Они не относятся к нам, по стоимости\\доступности - уточняйте у них напрямую 407-34-33. Так же многие гости положительно отзываются об акваклубе VODA  8 (812) 317-04-34. ");
                message.setReplyMarkup(getBackQuestionMenuKeyboard());
            }
            case "question_13" -> {
                message.setText("Неподалеку от нас находится хорошее вкусное кафе Fox inn rest (Приморское шоссе 44) \n" +
                        "А так же к нам доезжают практически все городские доставки.\n" +
                        "Если вы непротив поготовить во время отдыха, то во всех апартах есть кухни со всей необходимой посудой и техникой, а магазин находится в 5 минутах пешком.\n" +
                        "____\n" +
                        "Если поедете гулять в Сестрорецк, так же можем порекомендовать посетить кафе Incity с доступными ценами, большими порциями и вкусной едой ( Полевая ул.5)");
                message.setReplyMarkup(getBackQuestionMenuKeyboard());
            }
            case "question_14" -> {
                message.setText("Без машины удобнее всего добираться от станции метро Беговая.\n" +
                        "Остановка Станция метро Беговая ( в сторону на Сестрорецк) - автобусы 101А, 211, 216, 216А, 303, 600 до остановки Военная улица. Лисий нос, и еще 5 минут пешком до нас.\n" +
                        "На такси от метро Беговая - около 10 минут езды. ( Приморское шоссе 96)\n" +
                        "На электричке от Финляндского вокзала, до станции Лисий нос . От нее около 18 минут ходьбы.");
                message.setReplyMarkup(getBackQuestionMenuKeyboard());
            }
            case "question_15" -> {
                message.setText("Апартаменты Дуплекс - это два совмещенных домика с общей лестницей. Апартаменты находятся на втором этаже, первые этажи нежилые.\n" +
                        "У дуплекса соответственно 2 спальни, 2 санузла и 2 кухни-гостиных.\n" +
                        "Итого - 4 парных спальных места ( два дивана и две кровати), и общая вместимость до 8 человек.\n" +
                        "Обращаем ваше внимание, что апартаменты обособлены друг от друга лестничной площадкой. \n" +
                        "Гости дуплекса могут пользоваться двумя мангалами и мангальными зонами, и бесплатно взять у администратора 2 комплекта для барбекю ( шампура и решетки)\n" +
                        "Цена так же зависит от количества человек и даты.");
                message.setReplyMarkup(getBackQuestionMenuKeyboard());
            }
            case "question_16" -> {
                message.setText("У нас на территории 8 апартов и 8 мангалов.\n" +
                        "Решетки и шампура можно взять у администратора бесплатно.(6 шампуров и 1 решетка на апарт)\n" +
                        "Уголь и розжиг можно привезти с собой или приобрести у нас ( 400 руб. уголь, 200 руб. розжиг)\n" +
                        "Возле домиков есть деревянные патио и садовая мебель.");
                message.setReplyMarkup(getBackQuestionMenuKeyboard());
            }
            case "question_17" -> {
                message.setText("К сожалению у нас нет скидок на День Рождения.\n" +
                        "Но если соберетесь к нам, обязательно сообщите, что в Вашей компании есть именниник, это можно сделать в графе \"примечания\" при бронировании через модуль бронирования.");
                message.setReplyMarkup(getBackQuestionMenuKeyboard());
            }
            case "question_18" -> {
                message.setText("Если Вы впервые планируете приехать к нам, то рекомендуем заглянуть в нашу группу Вконтакте (https://vk.com/skvoreshnikiapart) и пообщаться с нашим ботом, он подарит Вам скидку 10% за подписку на группу и рассылку ВК. \n" +
                        "Если Вы уже бывали у нас, то советуем пройти небольшой опрос, который отправила Вам система после выезда, и получить статус \"Своей птицы\" с постоянным промокодом на скидку 5%. \n" +
                        "На данный момент больше никаких скидок нет.\n" +
                        "Если хотите в числе первых узнавать о скидках и акциях -  подпишитесь на наш инстаграм @skvoreshnikiapart . ");
                message.setReplyMarkup(getBackQuestionMenuKeyboard());
            }
            case "question_19" -> {
                message.setText("На будние дни ( с ПН-ЧТ) у нас открыто 2 тарифа - невозвратный и стандартный, с возможностью оплаты на месте. Вы можете выбрать подходящий Вам.\n" +
                        "А на выходные ( ПТ, СБ, ВСК) и праздничные дни, в связи с высоким спросом, у нас открыт только невозвратный тариф, со 100% оплатой при бронировании. В ПЕРИОД С 1 МАЯ ДО 31 АВГУСТА БРОНИРОВАНИЕ ОСУЩЕСТВЛЯЕТСЯ ТОЛЬКО ПО НЕВОЗВРАТНОМУ ТАРИФУ. ");
                message.setReplyMarkup(getBackQuestionMenuKeyboard());
            }
            case "question_20" -> {
                message.setText("Наш сайт: https://skvoreshniki.clients.site/\n" +
                        "Инстаграм https://instagram.com/skvoreshnikiapart?igshid=YmMyMTA2M2Y= \n" +
                        "Вконтакте https://vk.com/skvoreshnikiapart \n" +
                        "Мы на Яндекс. картах : https://yandex.ru/maps/-/CCUjQZf71B\n" +
                        "Наши контакты:\n" +
                        "skvoreshniki-apart@yandex.ru\n" +
                        "тел. 8(921) 886-66-44 служба бронирования 8(921)588-53-39 ресепшн");
                message.setReplyMarkup(getBackQuestionMenuKeyboard());
            }
            case "question_21" -> {
                message.setText("Вы можете самостоятельно найти нас на Яндекс картах, почитать отзывы, и забронировать по ссылке из профиля организации. \n" +
                        "Мы называемся Коттеджи Скворешники (ссылка на Яндекс карты https://yandex.ru/maps/-/CCUjQZf71B ) \n" +
                        "Так же Вы всегда можете сделать бронирование через систему бронирований яндекс.путешествия, островок.ру, суточно.ру, твил, OneTwoTrip, цены через системы бронирований будут повыше, но они так же успешно придут к нам, и мы будем ожидать Вас на заезд.");
                message.setReplyMarkup(getBackQuestionMenuKeyboard());
            }
            case "question_22" -> {
                message.setText("К сожалению, пока нет, но мы над этим работаем.");
                message.setReplyMarkup(getBackQuestionMenuKeyboard());
            }
            case "before_book_time" -> {
                message.setText("Да, у нас есть возможность раннего заезда, это будет стоить 250рчас.\n" +
                        "Стандартное время заезда - с 14-00. \n" +
                        "Если Вы решите что Вам необходим ранний заезд, требуется подтверждение администратора, зависящее от возможности на вашу дату.");
                message.setReplyMarkup(getBackQuestionMenuKeyboard());
            }
            case "night_time_leave" -> {
                message.setText("Если вы хотели бы выехать вечером, рекомендуем забронировать апартаменты на вторые сутки. Таким образом Вы сможете пребывать на территории сколько Вам нужно, и выехать в любое время до 12-00 следующего дня.\n" +
                        "Это удобно, когда Вам хотелось бы провести день загородом, но завтра на работу. \n" +
                        "Продление почасово к сожалению предложить не можем. (т.к оно позволяет продлить апарт не более чем до 3 часов дня)");
                message.setReplyMarkup(getBackQuestionMenuKeyboard());
            }
            case "later_leave" -> {
                message.setText("\n" +
                        "Возможность позднего выезда зависит от наличия последующего бронирования.\n" +
                        "Об этом можно будет узнать у администратора в день Вашего выезда. Заранее гарантировать поздний выезд в выходные мы, к сожалению, не можем. Стоимость позднего выезда -  250 руб\\ в час ");
                message.setReplyMarkup(getBackQuestionMenuKeyboard());
            }
            case "back_to_question_menu" -> {
                message.setText("Популярные вопросы: ");
                message.setReplyMarkup(getQuestionMenuKeyboard());
            }
            case "MainMenu" -> {
                message.setText("Добро пожаловать в наш отель! Выберите опцию:");
                message.setReplyMarkup(getMainMenuKeyboard());
            }
            default -> {
                message.setText("Извините, я не понимаю ваш запрос. Пожалуйста, выберите опцию из меню.");
                message.setReplyMarkup(getMainMenuKeyboard());
            }
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }




    private void saveReviewToDatabase(Long id, String username, String firstname, String lastName, String review, LocalDateTime localDateTime) {
        Review newReview = new Review(id, username, firstname, lastName, review, localDateTime);
        reviewRepository.save(newReview);
    }

    private void saveUserToDatabase(Long id, String username, String firstname, String lastName, String message) {
        HotelUser user = new HotelUser(id, username, firstname, lastName, message, LocalDateTime.now());
        userRepository.save(user);
    }

    private void saveAdminUserToDatabase(Long id, String username, String firstname, String lastName, String message, Boolean adminCative) {
        AdminUser adminUser = new AdminUser(id, username, firstname, lastName, message, LocalDateTime.now(), adminCative);
        adminUserRepository.save(adminUser);
    }

}

