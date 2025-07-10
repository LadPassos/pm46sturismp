package utfpr.edu.br.pm46sturismo.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import utfpr.edu.br.pm46sturismo.DetalhesPontoActivity
import utfpr.edu.br.pm46sturismo.R
import utfpr.edu.br.pm46sturismo.data.AppDatabase
import utfpr.edu.br.pm46sturismo.model.PontoTuristico
import utfpr.edu.br.pm46sturismo.databinding.ActivityMapaBinding

/**
 * Activity responsável por exibir todos os pontos turísticos cadastrados no Google Maps.
 * Permite ver informações resumidas dos pontos, abrir detalhes ou editar via BottomSheet.
 *
 * Funcionalidades:
 * - Exibe marcadores no mapa para todos os pontos cadastrados.
 * - Permite escolher o tipo do mapa e nível de zoom (configurações persistidas).
 * - Mostra informações rápidas de cada ponto em uma InfoWindow customizada.
 * - Abre detalhes e edição a partir de um BottomSheet ao clicar na InfoWindow.
 */
class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    // ViewBinding da Activity
    private lateinit var binding: ActivityMapaBinding

    // Referência do GoogleMap
    private lateinit var mapa: GoogleMap

    // Lista de pontos turísticos a serem exibidos
    private var listaPontos: List<PontoTuristico> = listOf()

    // Relaciona marcadores a pontos turísticos para acesso rápido
    private val marcadorParaPonto = mutableMapOf<Marker, PontoTuristico>()

    /**
     * onCreate - Inicializa o mapa, carrega os pontos e configurações.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtém o fragmento do mapa e registra o callback
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapa) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Botão para abrir a tela de configurações do mapa
        binding.btnConfigMapa.setOnClickListener {
            startActivity(Intent(this, ConfiguracoesActivity::class.java))
        }

        // Carrega os pontos turísticos em background (thread IO)
        CoroutineScope(Dispatchers.IO).launch {
            listaPontos = AppDatabase.getDatabase(this@MapaActivity)
                .pontoTuristicoDao()
                .listarTodos()

            // Só atualiza o mapa na thread principal
            runOnUiThread {
                carregarPontosNoMapa()
            }
        }
    }

    /**
     * Callback chamado quando o mapa está pronto para uso.
     * Configura o tipo de mapa, zoom e InfoWindow personalizada.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mapa = googleMap

        // Aplica configurações (tipo de mapa, zoom)
        aplicarConfiguracoesMapa()

        // Adapta a InfoWindow de cada marcador para exibir detalhes customizados
        mapa.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? = null

            override fun getInfoContents(marker: Marker): View {
                val view = layoutInflater.inflate(R.layout.item_info_window, null)
                val ponto = marcadorParaPonto[marker]

                val imagemView = view.findViewById<ImageView>(R.id.imagemInfo)
                val textoNome = view.findViewById<TextView>(R.id.textoNome)
                val textoDescricao = view.findViewById<TextView>(R.id.textoDescricao)
                val textoCoordenadas = view.findViewById<TextView>(R.id.textoCoordenadas)

                textoNome.text = ponto?.nome
                textoDescricao.text = ponto?.descricao
                textoCoordenadas.text = "Endereço: ${ponto?.enderecoCoordenada ?: "não informado"}"

                try {
                    val bitmap = ponto?.imagem?.let { caminho ->
                        android.graphics.BitmapFactory.decodeFile(caminho)
                    }
                    imagemView.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    imagemView.setImageResource(android.R.drawable.ic_menu_report_image)
                }

                return view
            }
        })

        // Ao clicar na InfoWindow, mostra um BottomSheet com ações
        mapa.setOnInfoWindowClickListener { marker ->
            val ponto = marcadorParaPonto[marker]
            ponto?.let { mostrarBottomSheet(it) }
        }
    }

    /**
     * Sempre que a Activity volta para o topo, aplica as configurações de tipo/zoom.
     */
    override fun onResume() {
        super.onResume()
        if (::mapa.isInitialized) {
            aplicarConfiguracoesMapa()
        }
    }

    /**
     * Lê as configurações salvas pelo usuário e aplica ao mapa.
     * Inclui tipo de mapa e nível de zoom.
     */
    private fun aplicarConfiguracoesMapa() {
        val prefs = getSharedPreferences("configuracoes", Context.MODE_PRIVATE)
        val tipoMapa = prefs.getInt("tipoMapa", 1)
        val zoom = prefs.getInt("zoom", 12).toFloat()

        mapa.mapType = when (tipoMapa) {
            2 -> GoogleMap.MAP_TYPE_SATELLITE
            3 -> GoogleMap.MAP_TYPE_HYBRID
            4 -> GoogleMap.MAP_TYPE_TERRAIN
            else -> GoogleMap.MAP_TYPE_NORMAL
        }

        // Move a câmera para o primeiro ponto cadastrado
        if (listaPontos.isNotEmpty()) {
            val primeiro = LatLng(listaPontos[0].latitude, listaPontos[0].longitude)
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(primeiro, zoom))
        }
    }

    /**
     * Adiciona todos os pontos turísticos como marcadores no mapa.
     * Relaciona cada marcador ao respectivo ponto turístico.
     */
    private fun carregarPontosNoMapa() {
        if (!::mapa.isInitialized) return

        if (listaPontos.isNotEmpty()) {
            for (ponto in listaPontos) {
                val posicao = LatLng(ponto.latitude, ponto.longitude)
                val marcador = mapa.addMarker(
                    MarkerOptions()
                        .position(posicao)
                        .title(ponto.nome)
                )
                if (marcador != null) {
                    marcadorParaPonto[marcador] = ponto
                }
            }
        }
    }

    /**
     * Abre a tela de detalhes do ponto selecionado.
     */
    private fun abrirDetalhes(ponto: PontoTuristico) {
        val intent = Intent(this, DetalhesPontoActivity::class.java)
        intent.putExtra("ponto", ponto)
        startActivity(intent)
    }

    /**
     * Abre a tela de edição do ponto selecionado.
     */
    private fun abrirEditar(ponto: PontoTuristico) {
        val intent = Intent(this, EditarPontoActivity::class.java)
        intent.putExtra("ponto", ponto)
        startActivity(intent)
    }

    /**
     * Mostra um BottomSheet com as opções de Detalhar e Editar o ponto.
     * @param ponto PontoTuristico selecionado
     */
    private fun mostrarBottomSheet(ponto: PontoTuristico) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottomsheet_acoes_ponto, null)

        view.findViewById<TextView>(R.id.textoNomeSheet).text = ponto.nome

        view.findViewById<View>(R.id.botaoDetalhesSheet).setOnClickListener {
            abrirDetalhes(ponto)
            bottomSheetDialog.dismiss()
        }
        view.findViewById<View>(R.id.botaoEditarSheet).setOnClickListener {
            abrirEditar(ponto)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }
}