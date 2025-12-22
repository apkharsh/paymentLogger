package com.apkharsh.paymentLogger.auth.repository;

import com.apkharsh.paymentLogger.auth.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends MongoRepository<User, User> {

    Optional<User> signUp(User user);

}
