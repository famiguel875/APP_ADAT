package com.es.aplicacion.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloWorldController {

    @GetMapping(value = ["/hello"], produces = [MediaType.TEXT_HTML_VALUE])
    fun helloWorld(): String {
        return "<h1>HOLA MUNDO</h1>"
    }
}