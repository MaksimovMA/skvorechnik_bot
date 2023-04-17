package mma.it.soft.skvorechnik.hotelbot.repository;

import mma.it.soft.skvorechnik.hotelbot.entity.HotelUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
@Repository
public interface HotelUserRepository extends CrudRepository<HotelUser, Long> {

    Set<HotelUser> findAll();
}
