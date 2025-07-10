package utfpr.edu.br.pm46sturismo.data

import androidx.room.*
import utfpr.edu.br.pm46sturismo.model.PontoTuristico

/**
 * Interface DAO (Data Access Object) que define as operações de acesso
 * ao banco de dados para a entidade PontoTuristico.
 *
 * Cada método representa uma operação (CRUD) que pode ser realizada
 * na tabela ponto_turistico do banco Room.
 */
@Dao
interface PontoTuristicoDao {

    /**
     * Insere um novo ponto turístico na tabela.
     *
     * @param ponto O objeto PontoTuristico a ser inserido no banco.
     */
    @Insert
    fun inserir(ponto: PontoTuristico)

    /**
     * Atualiza as informações de um ponto turístico já existente.
     *
     * @param ponto O objeto PontoTuristico com os dados atualizados.
     */
    @Update
    fun atualizar(ponto: PontoTuristico)

    /**
     * Lista todos os pontos turísticos cadastrados, ordenando os favoritos primeiro
     * e depois por nome em ordem alfabética.
     *
     * @return Lista de todos os pontos turísticos, com favoritos no topo.
     */
    @Query("SELECT * FROM ponto_turistico ORDER BY favorito DESC, nome ASC")
    fun listarTodos(): List<PontoTuristico>

    /**
     * Busca um ponto turístico pelo seu ID.
     *
     * @param id O ID único do ponto turístico.
     * @return O ponto turístico correspondente ao ID, ou null se não encontrado.
     */
    @Query("SELECT * FROM ponto_turistico WHERE id = :id")
    fun buscarPorId(id: Int): PontoTuristico?

    /**
     * Remove um ponto turístico do banco de dados.
     *
     * @param ponto O objeto PontoTuristico a ser excluído.
     */
    @Delete
    fun excluir(ponto: PontoTuristico)

    /**
     * Busca pontos turísticos cujo nome ou descrição contenha o filtro informado.
     *
     * @param filtro Texto usado para filtrar por nome ou descrição.
     *               Exemplo de uso: "%praia%"
     * @return Lista de pontos turísticos que correspondem ao filtro.
     */
    @Query("SELECT * FROM ponto_turistico WHERE nome LIKE :filtro OR descricao LIKE :filtro")
    fun buscarPorNomeOuDescricao(filtro: String): List<PontoTuristico>
}