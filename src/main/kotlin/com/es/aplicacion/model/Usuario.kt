package com.es.aplicacion.model

import org.bson.codecs.pojo.annotations.BsonId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document("Usuario")
data class Usuario(
    @Id
    val _id: String? = null,
    val username: String,
    val password: String,
    val email: String,
    val roles: String = "USER",
    val direccion: Direccion
) {



}