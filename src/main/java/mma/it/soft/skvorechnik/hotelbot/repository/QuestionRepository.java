package mma.it.soft.skvorechnik.hotelbot.repository;

import mma.it.soft.skvorechnik.hotelbot.entity.Question;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionRepository extends CrudRepository<Question, Long> {
    Question findFirstByChatGestID(Long gestId);
    Question findQuestionsByChatGestIDAndChatAdminID(Long gestId, Long chatAdminId);

    Optional<Question> findById (Long chatId);

    Question findByChatAdminIDAndProcessedEquals (Long chatAdminId, Boolean Processed);


}
