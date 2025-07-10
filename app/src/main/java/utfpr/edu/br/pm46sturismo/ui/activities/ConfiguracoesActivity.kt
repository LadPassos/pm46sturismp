package utfpr.edu.br.pm46sturismo.ui.activities

import android.content.Context
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import utfpr.edu.br.pm46sturismo.databinding.ActivityConfiguracoesBinding

/**
 * Activity responsável por configurar as preferências de exibição do mapa,
 * incluindo o tipo de visualização (Normal, Satélite, Híbrido, Terreno) e o nível de zoom.
 *
 * As configurações são salvas em SharedPreferences para serem persistidas e recuperadas pelo app.
 */
class ConfiguracoesActivity : AppCompatActivity() {

    // Binding para acessar os elementos da interface.
    private lateinit var binding: ActivityConfiguracoesBinding

    // SharedPreferences para salvar e carregar configurações do usuário.
    private lateinit var prefs: android.content.SharedPreferences

    /**
     * onCreate - Inicializa a tela de configurações e define os eventos dos componentes.
     * Carrega valores previamente salvos e define listeners para atualização das preferências.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa as preferências salvas no app
        prefs = getSharedPreferences("configuracoes", Context.MODE_PRIVATE)

        // Carrega o tipo de mapa salvo, padrão é 1 (Normal)
        val tipoMapa = prefs.getInt("tipoMapa", 1)
        // Carrega o nível de zoom salvo, padrão é 12
        val zoomSalvo = prefs.getInt("zoom", 12)

        // Define o zoom do SeekBar e exibe o valor atual
        binding.seekZoom.progress = zoomSalvo
        binding.textZoomValor.text = "Zoom: $zoomSalvo"

        // Marca a opção do tipo de mapa conforme salvo
        when (tipoMapa) {
            1 -> binding.opcaoNormal.isChecked = true
            2 -> binding.opcaoSatelite.isChecked = true
            3 -> binding.opcaoHibrido.isChecked = true
            4 -> binding.opcaoTerreno.isChecked = true
        }

        // Atualiza o texto inicial com o tipo de mapa atual
        atualizarTextoTipoMapa()

        // Listener para controlar o zoom, não permitindo valor menor que 5
        binding.seekZoom.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val zoom = if (progress < 5) 5 else progress
                binding.textZoomValor.text = "Zoom: $zoom"
                if (progress < 5) seekBar?.progress = 5 // Garante mínimo
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Listeners para atualização dinâmica do texto conforme o tipo de mapa é selecionado
        binding.opcaoNormal.setOnClickListener { atualizarTextoTipoMapa() }
        binding.opcaoSatelite.setOnClickListener { atualizarTextoTipoMapa() }
        binding.opcaoHibrido.setOnClickListener { atualizarTextoTipoMapa() }
        binding.opcaoTerreno.setOnClickListener { atualizarTextoTipoMapa() }

        // Salva as configurações quando o usuário clica em "Salvar"
        binding.botaoSalvarConfiguracoes.setOnClickListener {
            // Descobre qual tipo foi selecionado
            val tipoSelecionado = when (binding.grupoTipoMapa.checkedRadioButtonId) {
                binding.opcaoNormal.id -> 1
                binding.opcaoSatelite.id -> 2
                binding.opcaoHibrido.id -> 3
                binding.opcaoTerreno.id -> 4
                else -> 1
            }

            // Salva os valores em SharedPreferences
            prefs.edit()
                .putInt("zoom", binding.seekZoom.progress)
                .putInt("tipoMapa", tipoSelecionado)
                .apply()

            Toast.makeText(this, "Configurações salvas", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    /**
     * Atualiza o texto exibido na tela indicando o tipo de mapa selecionado.
     * É chamado sempre que o usuário muda a seleção dos tipos de mapa.
     */
    private fun atualizarTextoTipoMapa() {
        val tipoTexto = when {
            binding.opcaoSatelite.isChecked -> "Satélite"
            binding.opcaoHibrido.isChecked -> "Híbrido"
            binding.opcaoTerreno.isChecked -> "Terreno"
            else -> "Normal"
        }
        binding.textTipoSelecionado.text = "Tipo selecionado: $tipoTexto"
    }
}