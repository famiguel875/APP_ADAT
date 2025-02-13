package com.es.aplicacion.service

import com.es.aplicacion.dto.DireccionDTO
import com.es.aplicacion.dto.UsuarioDTO
import com.es.aplicacion.dto.UsuarioRegisterDTO
import com.es.aplicacion.error.exception.UnauthorizedException
import com.es.aplicacion.model.Direccion
import com.es.aplicacion.model.Usuario
import com.es.aplicacion.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UsuarioService(
    private val externalApiService: ExternalApiService,
    @Autowired private val usuarioRepository: UsuarioRepository,
    @Autowired private val passwordEncoder: PasswordEncoder
) : UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails {
        val usuario: Usuario = usuarioRepository.findByUsername(username!!)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "$username no existe") }

        return User.builder()
            .username(usuario.username)
            .password(usuario.password)
            .roles(usuario.roles)
            .build()
    }

    fun insertUser(usuarioInsertadoDTO: UsuarioRegisterDTO): UsuarioDTO {
        // Validar que las contraseñas coincidan
        if (usuarioInsertadoDTO.password != usuarioInsertadoDTO.passwordRepeat) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden")
        }

        // Comprobar si el usuario ya existe
        if (usuarioRepository.findByUsername(usuarioInsertadoDTO.username).isPresent) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario ya existe")
        }

        // Validaciones básicas para la dirección
        if (usuarioInsertadoDTO.direccion.municipio.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El municipio es obligatorio")
        }
        if (usuarioInsertadoDTO.direccion.provincia.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "La provincia es obligatoria")
        }

        // Validar la provincia consultando la API externa
        val provincias = externalApiService.obtenerProvinciasDesdeApi()
        val listaProvincias = provincias?.data
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No se han obtenido datos de provincias")
        // Buscamos la provincia cuyo nombre (campo PRO) coincida con el introducido
        val provinciaEncontrada = listaProvincias.find {
            it.PRO.equals(usuarioInsertadoDTO.direccion.provincia, ignoreCase = true)
        } ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "La provincia no es válida")

        // Usamos el código de provincia (CPRO) para consultar los municipios de esa provincia
        val municipios = externalApiService.obtenerMunicipiosDesdeApi(provinciaEncontrada.CPRO)
        val listaMunicipios = municipios?.data
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No se han obtenido datos de municipios")
        // Buscamos el municipio cuyo nombre (campo DMUN50) coincida
        if (listaMunicipios.none { it.DMUN50.equals(usuarioInsertadoDTO.direccion.municipio, ignoreCase = true) }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El municipio no es válido")
        }

        // Crear la entidad Usuario codificando la contraseña
        val usuario = Usuario(
            username = usuarioInsertadoDTO.username,
            email = usuarioInsertadoDTO.email,
            password = passwordEncoder.encode(usuarioInsertadoDTO.password),
            roles = usuarioInsertadoDTO.rol ?: "USER",
            direccion = Direccion(
                calle = usuarioInsertadoDTO.direccion.calle,
                num = usuarioInsertadoDTO.direccion.num,
                municipio = usuarioInsertadoDTO.direccion.municipio,
                provincia = usuarioInsertadoDTO.direccion.provincia,
                cp = usuarioInsertadoDTO.direccion.cp
            )
        )
        usuarioRepository.save(usuario)

        // Devolver el DTO de usuario (sin la contraseña)
        return UsuarioDTO(
            username = usuario.username,
            email = usuario.email,
            rol = usuario.roles,
            direccion = DireccionDTO(
                calle = usuario.direccion.calle,
                num = usuario.direccion.num,
                municipio = usuario.direccion.municipio,
                provincia = usuario.direccion.provincia,
                cp = usuario.direccion.cp
            )
        )
    }
}
