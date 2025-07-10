package utfpr.edu.br.pm46sturismo.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Entidade que representa um ponto turístico no banco de dados Room.
 *
 * Esta classe será mapeada para a tabela "ponto_turistico".
 *
 * @property id Identificador único do ponto turístico, gerado automaticamente.
 * @property nome Nome do ponto turístico.
 * @property descricao Descrição do local.
 * @property latitude Coordenada de latitude do ponto.
 * @property longitude Coordenada de longitude do ponto.
 * @property imagem Caminho absoluto ou URI da imagem capturada pelo usuário.
 * @property imagemMapa Caminho absoluto ou URI da imagem do mapa estático do ponto.
 * @property favorito Define se o ponto é marcado como favorito pelo usuário.
 * @property enderecoCoordenada Endereço convertido a partir das coordenadas (opcional).
 */
@Entity(tableName = "ponto_turistico")
data class PontoTuristico(
    /**
     * Chave primária, gerada automaticamente pelo Room.
     */
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    /**
     * Nome do ponto turístico.
     */
    val nome: String,

    /**
     * Descrição detalhada do ponto turístico.
     */
    val descricao: String,

    /**
     * Latitude geográfica do ponto turístico.
     */
    val latitude: Double,

    /**
     * Longitude geográfica do ponto turístico.
     */
    val longitude: Double,

    /**
     * Caminho absoluto da imagem capturada pelo usuário (pode ser nulo).
     */
    val imagem: String?,

    /**
     * Caminho absoluto da imagem do mapa estático correspondente (pode ser nulo).
     */
    val imagemMapa: String?,

    /**
     * Indica se o ponto é favorito do usuário. Padrão: false.
     */
    val favorito: Boolean = false,

    /**
     * Endereço textual obtido a partir das coordenadas, se disponível.
     */
    val enderecoCoordenada: String? = null
) : Serializable // Permite passar a entidade entre Activities via Intent