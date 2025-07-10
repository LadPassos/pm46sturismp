package utfpr.edu.br.pm46sturismo.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import utfpr.edu.br.pm46sturismo.databinding.ActivitySplashBinding

/**
 * Activity responsável pela tela de splash do aplicativo.
 * Exibe a logo e redireciona para a tela principal (MainActivity) após alguns segundos.
 *
 * - Exibe a logo do app.
 * - Permite breve tempo de carregamento para animações ou inicializações.
 */
class SplashActivity : AppCompatActivity() {

    // ViewBinding para acessar elementos da tela
    private lateinit var binding: ActivitySplashBinding

    /**
     * onCreate - Exibe a splash e redireciona automaticamente após delay.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Redireciona para a MainActivity após 2 segundos (2000 ms)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Remove a Splash da pilha
        }, 2000)
    }
}