package mma.it.soft.skvorechnik.hotelbot.repository;

import mma.it.soft.skvorechnik.hotelbot.entity.Review;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ReviewRepository extends CrudRepository<Review, Long> {
}
