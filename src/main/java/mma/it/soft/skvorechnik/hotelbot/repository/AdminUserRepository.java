package mma.it.soft.skvorechnik.hotelbot.repository;

import mma.it.soft.skvorechnik.hotelbot.entity.AdminUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface AdminUserRepository extends CrudRepository<AdminUser, Long> {

    Set<AdminUser> findAll();

    AdminUser findByChatID (Long chatID);
}
