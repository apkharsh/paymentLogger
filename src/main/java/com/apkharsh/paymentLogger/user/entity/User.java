package com.apkharsh.paymentLogger.user.entity;

import com.apkharsh.paymentLogger.auth.ROLE;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "users")
@Builder
public class User {

    @Id
    private String id;
    private String name;
    private String email;
    private String password;
    private ROLE role; // admin, user
}
