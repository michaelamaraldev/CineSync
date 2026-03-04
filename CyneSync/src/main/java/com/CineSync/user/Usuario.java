package com.CineSync.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document

public class Usuario {
    private String nickName;
    private String nomeCompleto;
    private Status status;
}
