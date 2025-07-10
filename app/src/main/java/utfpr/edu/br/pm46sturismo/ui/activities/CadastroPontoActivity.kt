package utfpr.edu.br.pm46sturismo.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import utfpr.edu.br.pm46sturismo.data.AppDatabase
import utfpr.edu.br.pm46sturismo.databinding.ActivityCadastroPontoBinding
import utfpr.edu.br.pm46sturismo.model.PontoTuristico
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Activity responsável pelo cadastro de um novo ponto turístico.
 * Permite capturar foto, buscar localização via endereço, salvar imagens e
 * registrar o ponto no banco de dados Room.
 *
 * Funcionalidades:
 * - Captura de foto pelo app ou seleção da galeria
 * - Busca coordenadas e imagem de mapa a partir de endereço digitado
 * - Salva os dados preenchidos no banco local
 */
class CadastroPontoActivity : AppCompatActivity() {

    // Binding para acesso aos componentes da tela
    private lateinit var binding: ActivityCadastroPontoBinding

    // Caminho absoluto da imagem capturada/selecionada
    private var caminhoImagem: String? = null

    // Caminho absoluto da imagem do mapa estático
    private var caminhoMapa: String? = null

    // Chave da API do Google Maps Static, recuperada do Manifest
    private lateinit var apiKey: String

    /**
     * onCreate - Inicializa a tela e define listeners dos botões.
     * Recupera a chave da API do Google, configura a captura de foto,
     * seleção de galeria e busca por endereço.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroPontoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recupera a chave da API do Google Maps do AndroidManifest.xml
        apiKey = try {
            val ai = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            ai.metaData?.getString("com.google.android.geo.API_KEY") ?: ""
        } catch (e: Exception) {
            ""
        }

        // Listener do botão para capturar foto pela câmera
        binding.botaoFoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }

        // Listener do botão para selecionar imagem da galeria
        binding.botaoGaleria.setOnClickListener {
            galeriaLauncher.launch("image/*")
        }

        // Listener do botão salvar
        binding.botaoSalvar.setOnClickListener {
            salvarPonto()
        }

        // Listener do botão buscar mapa a partir do endereço
        binding.botaoBuscarMapa.setOnClickListener {
            val endereco = binding.editarEndereco.text.toString().trim()

            if (endereco.isBlank()) {
                Toast.makeText(this, "Digite um endereço para buscar o mapa.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Busca coordenadas geográficas em background (thread IO)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val geocoder = Geocoder(this@CadastroPontoActivity, Locale.getDefault())
                    val resultados = geocoder.getFromLocationName(endereco, 1)

                    if (!resultados.isNullOrEmpty()) {
                        val latitude = resultados[0].latitude
                        val longitude = resultados[0].longitude

                        // Gera e salva imagem de mapa estático
                        caminhoMapa = gerarMapaEstatico(latitude, longitude)

                        runOnUiThread {
                            binding.textoCoordenadas.text = "Coordenadas: $latitude, $longitude"
                            val bitmap = BitmapFactory.decodeFile(caminhoMapa)
                            binding.imagemMapa.setImageBitmap(bitmap)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@CadastroPontoActivity, "Endereço não encontrado.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@CadastroPontoActivity, "Erro ao buscar coordenadas.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Launcher para capturar foto da câmera.
     * Salva e exibe a imagem capturada.
     */
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val foto = it.data?.extras?.get("data") as? Bitmap
            foto?.let { bmp ->
                caminhoImagem = salvarImagem(bmp)
                binding.imagemPonto.setImageBitmap(bmp)
            }
        }
    }

    /**
     * Launcher para selecionar imagem da galeria.
     * Salva e exibe a imagem selecionada.
     */
    private val galeriaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val inputStream = contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            caminhoImagem = salvarImagem(bitmap)
            binding.imagemPonto.setImageBitmap(bitmap)
        }
    }

    /**
     * Método para salvar os dados do ponto turístico no banco de dados.
     * Realiza validação dos campos obrigatórios, busca coordenadas e salva no Room.
     */
    private fun salvarPonto() {
        val nome = binding.editarNome.text.toString().trim()
        val descricao = binding.editarDescricao.text.toString().trim()
        val endereco = binding.editarEndereco.text.toString().trim()

        if (nome.isBlank() || descricao.isBlank() || endereco.isBlank()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
            return
        }

        // Operação em background (coroutine) para não travar a UI
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(this@CadastroPontoActivity, Locale.getDefault())
                val resultados = geocoder.getFromLocationName(endereco, 1)

                if (!resultados.isNullOrEmpty()) {
                    val latitude = resultados[0].latitude
                    val longitude = resultados[0].longitude

                    // Busca endereço textual pelas coordenadas
                    var enderecoCoordenada: String? = null
                    try {
                        val listaEndereco = geocoder.getFromLocation(latitude, longitude, 1)
                        if (!listaEndereco.isNullOrEmpty()) {
                            val addr = listaEndereco[0]
                            enderecoCoordenada = buildString {
                                append(addr.thoroughfare ?: "")
                                if (addr.subThoroughfare != null) append(", ${addr.subThoroughfare}")
                                if (addr.locality != null) append(" - ${addr.locality}")
                                if (addr.adminArea != null) append(", ${addr.adminArea}")
                            }
                        }
                    } catch (e: Exception) {
                        enderecoCoordenada = null // Em caso de erro, deixa null
                    }

                    // Gera a imagem do mapa estático e salva caminho
                    caminhoMapa = gerarMapaEstatico(latitude, longitude)

                    // Cria objeto do ponto turístico
                    val ponto = PontoTuristico(
                        nome = nome,
                        descricao = descricao,
                        latitude = latitude,
                        longitude = longitude,
                        imagem = caminhoImagem,
                        imagemMapa = caminhoMapa,
                        enderecoCoordenada = enderecoCoordenada
                    )

                    // Insere no banco de dados Room
                    AppDatabase.getDatabase(this@CadastroPontoActivity)
                        .pontoTuristicoDao()
                        .inserir(ponto)

                    // Mostra mensagem de sucesso e fecha activity
                    runOnUiThread {
                        Toast.makeText(
                            this@CadastroPontoActivity,
                            "Ponto cadastrado com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@CadastroPontoActivity,
                            "Endereço inválido. Tente novamente.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@CadastroPontoActivity,
                        "Erro ao salvar ponto.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    /**
     * Salva uma imagem Bitmap no armazenamento interno da aplicação.
     *
     * @param bitmap Imagem capturada ou selecionada.
     * @return Caminho absoluto do arquivo salvo.
     */
    private fun salvarImagem(bitmap: Bitmap): String {
        val nome = "imagem_${System.currentTimeMillis()}.jpg"
        val arquivo = File(filesDir, nome)
        val out = FileOutputStream(arquivo)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()
        return arquivo.absolutePath
    }

    /**
     * Gera e salva a imagem do mapa estático a partir das coordenadas fornecidas,
     * utilizando a API do Google Static Maps.
     *
     * @param latitude Latitude do local.
     * @param longitude Longitude do local.
     * @return Caminho absoluto da imagem do mapa, ou null em caso de erro.
     */
    private fun gerarMapaEstatico(latitude: Double, longitude: Double): String? {
        return try {
            val url = java.net.URL(
                "https://maps.googleapis.com/maps/api/staticmap?center=$latitude,$longitude&zoom=15&size=600x300&maptype=roadmap&markers=color:red%7C$latitude,$longitude&key=$apiKey"
            )
            val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            val nome = "mapa_${System.currentTimeMillis()}.png"
            val arquivo = File(filesDir, nome)
            val out = FileOutputStream(arquivo)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.close()
            return arquivo.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}