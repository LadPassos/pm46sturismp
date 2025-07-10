package utfpr.edu.br.pm46sturismo.ui.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import utfpr.edu.br.pm46sturismo.data.AppDatabase
import utfpr.edu.br.pm46sturismo.databinding.ActivityEditarPontoBinding
import utfpr.edu.br.pm46sturismo.model.PontoTuristico
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*

/**
 * Activity responsável por editar ou excluir um ponto turístico existente.
 *
 * Permite alterar nome, descrição, coordenadas, imagens e buscar novo mapa estático.
 * Também permite excluir o ponto do banco de dados Room.
 */
class EditarPontoActivity : AppCompatActivity() {

    // ViewBinding para acessar elementos da tela
    private lateinit var binding: ActivityEditarPontoBinding

    // Objeto ponto turístico a ser editado
    private lateinit var ponto: PontoTuristico

    // Caminhos das imagens (foto e mapa)
    private var caminhoImagem: String? = null
    private var caminhoMapa: String? = null

    // Chave da API do Google Maps (recuperada do Manifest)
    private lateinit var apiKey: String

    /**
     * onCreate - Inicializa a tela, recupera o ponto, preenche os campos e define listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPontoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Busca a chave da API do Google Static Maps
        apiKey = try {
            val ai = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            ai.metaData?.getString("com.google.android.geo.API_KEY") ?: ""
        } catch (e: Exception) {
            ""
        }

        // Recupera o ponto turístico recebido via intent de forma segura
        val pontoExtra = intent.getSerializableExtra("ponto") as? PontoTuristico
        if (pontoExtra == null) {
            Toast.makeText(this, "Não foi possível carregar o ponto.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        ponto = pontoExtra

        // Preenche campos do formulário com dados do ponto
        binding.editarNome.setText(ponto.nome)
        binding.editarDescricao.setText(ponto.descricao)
        binding.textoCoordenadas.text = "Coordenadas: ${ponto.latitude}, ${ponto.longitude}"

        caminhoImagem = ponto.imagem
        caminhoMapa = ponto.imagemMapa

        // Exibe imagens se existirem
        if (!caminhoImagem.isNullOrEmpty()) {
            val img = BitmapFactory.decodeFile(caminhoImagem)
            binding.imagemPonto.setImageBitmap(img)
        }
        if (!caminhoMapa.isNullOrEmpty()) {
            val mapa = BitmapFactory.decodeFile(caminhoMapa)
            binding.imagemMapa.setImageBitmap(mapa)
        }

        // Configura listeners dos botões
        binding.botaoFoto.setOnClickListener { abrirCamera() }
        binding.botaoGaleria.setOnClickListener { escolherDaGaleria() }
        binding.botaoBuscarMapa.setOnClickListener { buscarCoordenadasEMapa() }
        binding.botaoSalvar.setOnClickListener { salvarAlteracoes() }
        binding.botaoExcluir.setOnClickListener { excluirPonto() }
    }

    /**
     * Abre a câmera do dispositivo para capturar uma nova foto.
     * A imagem é salva temporariamente e exibida na tela.
     */
    private fun abrirCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val arquivo = File.createTempFile("foto_", ".jpg", externalCacheDir)
        caminhoImagem = arquivo.absolutePath

        // Usa FileProvider para fornecer URI segura
        val uriImagem = FileProvider.getUriForFile(this, "$packageName.provider", arquivo)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImagem)
        startActivityForResult(intent, 1)
    }

    /**
     * Abre a galeria para o usuário selecionar uma imagem.
     * A imagem selecionada é salva temporariamente e exibida.
     */
    private fun escolherDaGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 2)
    }

    /**
     * Manipula o resultado das atividades de captura ou seleção de imagem.
     * Atualiza a imagem exibida conforme o resultado.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1 -> caminhoImagem?.let {
                    val bitmap = BitmapFactory.decodeFile(it)
                    binding.imagemPonto.setImageBitmap(bitmap)
                }
                2 -> {
                    val uri = data?.data
                    val inputStream = contentResolver.openInputStream(uri!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    val file = File.createTempFile("galeria_", ".jpg", externalCacheDir)
                    val out = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    out.close()
                    caminhoImagem = file.absolutePath
                    binding.imagemPonto.setImageBitmap(bitmap)
                }
            }
        }
    }

    /**
     * Busca as coordenadas a partir de um endereço digitado
     * e gera a imagem do mapa estático para o local.
     * Atualiza as coordenadas e imagem no formulário.
     */
    private fun buscarCoordenadasEMapa() {
        val endereco = binding.editarEndereco.text.toString().trim()
        if (endereco.isBlank()) {
            Toast.makeText(this, "Informe o endereço!", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(this@EditarPontoActivity, Locale.getDefault())
                val resultados = geocoder.getFromLocationName(endereco, 1)

                if (!resultados.isNullOrEmpty()) {
                    val latitude = resultados[0].latitude
                    val longitude = resultados[0].longitude

                    // Gera a imagem do mapa estático e atualiza ponto
                    caminhoMapa = gerarMapaEstatico(latitude, longitude)

                    runOnUiThread {
                        binding.textoCoordenadas.text = "Coordenadas: $latitude, $longitude"
                        val bitmap = BitmapFactory.decodeFile(caminhoMapa)
                        binding.imagemMapa.setImageBitmap(bitmap)
                        // Atualiza os dados do ponto temporariamente
                        ponto = ponto.copy(latitude = latitude, longitude = longitude, imagemMapa = caminhoMapa)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@EditarPontoActivity, "Endereço não encontrado.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@EditarPontoActivity, "Erro ao buscar o endereço.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Gera a imagem do mapa estático usando a API do Google Static Maps
     * e salva o arquivo temporariamente. Retorna o caminho do arquivo.
     *
     * @param latitude Latitude do local
     * @param longitude Longitude do local
     * @return Caminho absoluto do arquivo de imagem gerado, ou null em erro
     */
    private fun gerarMapaEstatico(latitude: Double, longitude: Double): String? {
        return try {
            val url = URL("https://maps.googleapis.com/maps/api/staticmap?center=$latitude,$longitude&zoom=15&size=600x300&maptype=roadmap&markers=color:red%7C$latitude,$longitude&key=$apiKey")
            val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            val arquivo = File.createTempFile("mapa_", ".png", externalCacheDir)
            val out = FileOutputStream(arquivo)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.close()
            arquivo.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Salva as alterações feitas no ponto turístico no banco de dados Room.
     * Realiza validação dos campos obrigatórios antes de salvar.
     */
    private fun salvarAlteracoes() {
        val nome = binding.editarNome.text.toString().trim()
        val descricao = binding.editarDescricao.text.toString().trim()
        val latitude = ponto.latitude
        val longitude = ponto.longitude
        val imagem = caminhoImagem
        val imagemMapa = caminhoMapa
        val favorito = ponto.favorito

        if (nome.isBlank() || descricao.isBlank()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show()
            return
        }

        // Cria novo objeto com os dados atualizados
        val pontoAtualizado = ponto.copy(
            nome = nome,
            descricao = descricao,
            latitude = latitude,
            longitude = longitude,
            imagem = imagem,
            imagemMapa = imagemMapa,
            favorito = favorito
        )

        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getDatabase(this@EditarPontoActivity)
                .pontoTuristicoDao()
                .atualizar(pontoAtualizado)

            runOnUiThread {
                Toast.makeText(this@EditarPontoActivity, "Alterações salvas!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * Exclui o ponto turístico após confirmação do usuário.
     * Remove o registro do banco de dados Room.
     */
    private fun excluirPonto() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar exclusão")
            .setMessage("Deseja realmente excluir este ponto?")
            .setPositiveButton("Sim") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    AppDatabase.getDatabase(this@EditarPontoActivity)
                        .pontoTuristicoDao()
                        .excluir(ponto)

                    runOnUiThread {
                        Toast.makeText(this@EditarPontoActivity, "Ponto excluído!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}