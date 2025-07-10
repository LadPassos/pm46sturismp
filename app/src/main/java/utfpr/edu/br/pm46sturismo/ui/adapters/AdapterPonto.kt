package utfpr.edu.br.pm46sturismo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import utfpr.edu.br.pm46sturismo.R
import utfpr.edu.br.pm46sturismo.model.PontoTuristico

/**
 * Adapter personalizado para exibir a lista de pontos turísticos em um RecyclerView.
 * Permite favoritar, editar, ver detalhes e excluir pontos diretamente da lista.
 *
 * @property context Contexto da aplicação
 * @property onFavoritar Função chamada ao favoritar/desfavoritar
 * @property onEditar Função chamada ao editar um ponto
 * @property onDetalhes Função chamada ao ver detalhes do ponto
 * @property onExcluir Função chamada ao excluir um ponto
 */
class AdapterPonto(
    private val context: Context,
    private val onFavoritar: (PontoTuristico) -> Unit,
    private val onEditar: (PontoTuristico) -> Unit,
    private val onDetalhes: (PontoTuristico) -> Unit,
    private val onExcluir: (PontoTuristico) -> Unit
) : RecyclerView.Adapter<AdapterPonto.ViewHolder>() {

    // Lista interna que armazena os pontos turísticos exibidos
    private var lista: MutableList<PontoTuristico> = mutableListOf()

    /**
     * Atualiza a lista de pontos turísticos exibida pelo adapter.
     *
     * @param novaLista Nova lista a ser exibida
     */
    fun atualizarLista(novaLista: List<PontoTuristico>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }

    /**
     * ViewHolder que referencia todos os componentes do item da lista.
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textoNome: TextView = view.findViewById(R.id.textoNome)
        val textoDescricao: TextView = view.findViewById(R.id.textoDescricao)
        val btnFavoritar: ImageButton = view.findViewById(R.id.btnFavoritar)
        val favoritoIndicador: View = view.findViewById(R.id.favoritoIndicador)
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
        val btnVerDetalhes: Button = view.findViewById(R.id.btnVerDetalhes)
        val btnExcluir: Button = view.findViewById(R.id.btnExcluir)
    }

    /**
     * Cria o ViewHolder inflando o layout do item da lista.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(context)
            .inflate(R.layout.item_ponto, parent, false)
        return ViewHolder(layout)
    }

    /**
     * Retorna o total de itens da lista.
     */
    override fun getItemCount() = lista.size

    /**
     * Realiza o binding dos dados do ponto turístico para a ViewHolder.
     * Define o comportamento dos botões (favoritar, editar, detalhes, excluir).
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ponto = lista[position]

        // Nome e descrição do ponto
        holder.textoNome.text = ponto.nome
        holder.textoDescricao.text = ponto.descricao

        // Exibe indicador de favorito (círculo colorido)
        holder.favoritoIndicador.visibility = if (ponto.favorito) View.VISIBLE else View.GONE
        holder.favoritoIndicador.setBackgroundResource(R.drawable.circulo_favorito)

        // Define ícone da estrela do botão favorito
        holder.btnFavoritar.setImageResource(
            if (ponto.favorito) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )

        // Listener para favoritar/desfavoritar
        holder.btnFavoritar.setOnClickListener {
            onFavoritar(ponto)
        }

        // Listener para editar
        holder.btnEditar.setOnClickListener {
            onEditar(ponto)
        }

        // Listener para ver detalhes
        holder.btnVerDetalhes.setOnClickListener {
            onDetalhes(ponto)
        }

        // Listener para excluir
        holder.btnExcluir.setOnClickListener {
            onExcluir(ponto)
        }
    }
}