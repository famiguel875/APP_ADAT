package com.es.aplicacion.controller

import com.es.aplicacion.domain.DatosMunicipios
import com.es.aplicacion.domain.DatosProvincias
import com.es.aplicacion.service.ExternalApiService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/geo")
class GeoController(@Autowired private val externalApiService: ExternalApiService) {

    @GetMapping("/provincias")
    fun obtenerProvincias(): ResponseEntity<DatosProvincias> {
        val provincias = externalApiService.obtenerProvinciasDesdeApi()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No se han encontrado provincias")
        return ResponseEntity.ok(provincias)
    }

    // Para obtener municipios se espera un parámetro "provincia" (nombre)
    // Se busca el código de la provincia y luego se consultan sus municipios
    @GetMapping("/municipios")
    fun obtenerMunicipios(@RequestParam provincia: String): ResponseEntity<DatosMunicipios> {
        val provincias = externalApiService.obtenerProvinciasDesdeApi()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No se han obtenido datos de provincias")
        val provinciaEncontrada = provincias.data?.find {
            it.PRO.equals(provincia, ignoreCase = true)
        } ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "La provincia no es válida")
        val municipios = externalApiService.obtenerMunicipiosDesdeApi(provinciaEncontrada.CPRO)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No se han encontrado municipios")
        return ResponseEntity.ok(municipios)
    }
}
