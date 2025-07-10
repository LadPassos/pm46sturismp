package utfpr.edu.br.pm46sturismo.ui.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import utfpr.edu.br.pm46sturismo.DetalhesPontoActivity
import utfpr.edu.br.pm46sturismo.R
import utfpr.edu.br.pm46sturismo.adapter.AdapterPonto
import utfpr.edu.br.pm46sturismo.data.AppDatabase
import utfpr.edu.br.pm46sturismo.data.PontoTuristicoDao
import utfpr.edu.br.pm46sturismo.model.PontoTuristico

/**
 * Activity responsável por listar os pontos turísticos cadastrados.
 * Permite favoritar, buscar, editar, ver detalhes e excluir pontos.
 *
 * - Busca dinâmica por nome ou descrição
 * - Listagem paginada com favoritos no topo
 * - Ações rápidas via Adapter: Favoritar, Editar, Detalhar, Excluir
 */
class ListaPontosActivity : AppCompatActivity() {

    // RecyclerView que exibe a lista de pontos turísticos
    private lateinit var recyclerView: RecyclerView

    // Adapter da lista (personalizado para as ações do app)
    private lateinit var adapter: AdapterPonto

    // DAO para operações no banco de dados
    private lateinit var dao: PontoTuristicoDao

    // Campo de busca (filtra lista conforme o texto)
    private lateinit var busca: EditText

    /**
     * onCreate - Inicializa views, adapter e listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_pontos)

        // Inicializa DAO
        dao = AppDatabase.getDatabase(this).pontoTuristicoDao()

        // Referências às views
        busca = findViewById(R.id.inputBusca)
        recyclerView = findViewById(R.id.recyclerViewPontos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializa AdapterPonto, define ações e listeners dos botões na lista
        adapter = AdapterPonto(
            context = this,
            onFavoritar = { ponto ->
                // Marca/desmarca favorito e atualiza no banco de dados
                CoroutineScope(Dispatchers.IO).launch {
                    val atualizado = ponto.copy(favorito = !ponto.favorito)
                    dao.atualizar(atualizado)
                    carregarDados(busca.text.toString())
                }
            },
            onEditar = { ponto ->
                // Abre tela de edição
                val intent = Intent(this, EditarPontoActivity::class.java)
                intent.putExtra("ponto", ponto)
                startActivity(intent)
            },
            onDetalhes = { ponto ->
                // Abre tela de detalhes
                val intent = Intent(this, DetalhesPontoActivity::class.java)
                intent.putExtra("ponto", ponto)
                startActivity(intent)
            },
            onExcluir = { ponto ->
                // Exibe confirmação e exclui
                AlertDialog.Builder(this)
                    .setTitle("Excluir ponto turístico")
                    .setMessage("Tem certeza que deseja excluir este ponto turístico?")
                    .setPositiveButton("Sim") { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            dao.excluir(ponto)
                            carregarDados(busca.text.toString())
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        recyclerView.adapter = adapter

        // Listener de busca dinâmica (ao digitar, recarrega lista)
        busca.addTextChangedListener { editable ->
            carregarDados(editable?.toString().orEmpty())
        }
    }

    /**
     * Carrega dados do banco de dados e atualiza o Adapter.
     * Favoritos aparecem primeiro na lista.
     *
     * @param filtro Filtro de busca por nome ou descrição.
     */
    private fun carregarDados(filtro: String = "") {
        CoroutineScope(Dispatchers.IO).launch {
            // Busca os dados conforme o filtro
            val todos = if (filtro.isEmpty()) {
                dao.listarTodos()
            } else {
                dao.buscarPorNomeOuDescricao("%$filtro%")
            }
            // Separa favoritos e outros pontos
            val favoritos = todos.filter { it.favorito }
            val outros    = todos.filter { !it.favorito }
            val listaOrdenada = favoritos + outros

            // Atualiza adapter na thread principal (UI)
            withContext(Dispatchers.Main) {
                adapter.atualizarLista(listaOrdenada)
            }
        }
    }

    /**
     * Sempre que a tela voltar a ser exibida, recarrega os dados para refletir alterações.
     */
    override fun onResume() {
        super.onResume()
        carregarDados()
    }
}