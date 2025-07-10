package utfpr.edu.br.pm46sturismo.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import utfpr.edu.br.pm46sturismo.databinding.ActivityMainBinding

/**
 * Activity inicial do app PM46S Turismo.
 * Exibe a logo, o slogan e um botão para acessar o menu principal do aplicativo.
 *
 * - Exibe a tela de boas-vindas.
 * - Ao clicar em "Entrar", direciona para o MenuPrincipalActivity.
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding para acessar componentes da tela
    private lateinit var binding: ActivityMainBinding

    /**
     * onCreate - Inicializa a tela, configura o botão de entrada e o fluxo de navegação.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ao clicar no botão "Entrar", abre o menu principal e finaliza a tela atual
        binding.botaoEntrar.setOnClickListener {
            startActivity(Intent(this, MenuPrincipalActivity::class.java))
            finish()
        }
    }
}