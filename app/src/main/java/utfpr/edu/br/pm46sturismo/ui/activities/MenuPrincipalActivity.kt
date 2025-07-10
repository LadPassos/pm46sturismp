package utfpr.edu.br.pm46sturismo.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import utfpr.edu.br.pm46sturismo.databinding.ActivityMenuPrincipalBinding

/**
 * Activity principal do app PM46S Turismo.
 * Exibe o menu inicial com botões para cadastrar novos pontos, visualizar o mapa
 * ou acessar a lista de pontos turísticos.
 *
 * Opções:
 * - Cadastrar Ponto Turístico
 * - Visualizar Mapa
 * - Listar Pontos Turísticos
 */
class MenuPrincipalActivity : AppCompatActivity() {

    // ViewBinding para acesso aos componentes da interface.
    private lateinit var binding: ActivityMenuPrincipalBinding

    /**
     * onCreate - Inicializa os componentes da tela e define as ações dos botões do menu.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botão para abrir tela de cadastro de ponto turístico
        binding.botaoCadastrar.setOnClickListener {
            startActivity(Intent(this, CadastroPontoActivity::class.java))
        }

        // Botão para abrir o mapa com os pontos turísticos
        binding.botaoMapa.setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java))
        }

        // Botão para abrir a lista de pontos turísticos cadastrados
        binding.botaoLista.setOnClickListener {
            startActivity(Intent(this, ListaPontosActivity::class.java))
        }
    }
}