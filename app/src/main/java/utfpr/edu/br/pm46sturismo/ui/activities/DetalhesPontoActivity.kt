package utfpr.edu.br.pm46sturismo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import utfpr.edu.br.pm46sturismo.model.PontoTuristico
import java.io.File

@Suppress("DEPRECATION")
class DetalhesPontoActivity : AppCompatActivity() {

    // Componentes da interface (ImageView, TextView, Buttons)
    private lateinit var imagemPonto: ImageView
    private lateinit var imagemMapa: ImageView
    private lateinit var textoNome: TextView
    private lateinit var textoDescricao: TextView
    private lateinit var textoEnderecoCoordenada: TextView
    private lateinit var textoDistancia: TextView
    private lateinit var btnFavoritar: Button
    private lateinit var btnCompartilhar: Button
    private lateinit var btnVerMapa: Button
    private lateinit var btnIr: Button

    // Instância do ponto turístico selecionado
    private lateinit var ponto: PontoTuristico

    // Cliente para obter localização real do usuário
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Localização atual do usuário (inicialmente nula)
    private var usuarioLat: Double? = null
    private var usuarioLon: Double? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_ponto)

        // Faz o bind dos componentes do layout
        imagemPonto = findViewById(R.id.imagemPonto)
        imagemMapa = findViewById(R.id.imagemMapa)
        textoNome = findViewById(R.id.textoNome)
        textoDescricao = findViewById(R.id.textoDescricao)
        textoEnderecoCoordenada = findViewById(R.id.textoEnderecoCoordenada)
        textoDistancia = findViewById(R.id.textoDistancia)
        btnFavoritar = findViewById(R.id.btnFavoritar)
        btnCompartilhar = findViewById(R.id.btnCompartilhar)
        btnVerMapa = findViewById(R.id.btnVerMapa)
        btnIr = findViewById(R.id.btnIr)

        // Recupera o objeto ponto turístico passado por intent
        val pontoExtra = intent.getSerializableExtra("ponto") as? PontoTuristico
        if (pontoExtra == null) {
            Toast.makeText(this, "Não foi possível carregar os dados do ponto.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        ponto = pontoExtra

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        preencherCampos()

        btnFavoritar.setOnClickListener {
            ponto = ponto.copy(favorito = !ponto.favorito)
            atualizarBotaoFavorito()
            Toast.makeText(
                this,
                if (ponto.favorito) "Adicionado aos favoritos" else "Removido dos favoritos",
                Toast.LENGTH_SHORT
            ).show()
            // Aqui você pode atualizar o banco se quiser persistir essa alteração!
        }

        btnCompartilhar.setOnClickListener {
            val textoCompartilhar = buildString {
                append("${ponto.nome}\n\n${ponto.descricao}")
                if (!ponto.enderecoCoordenada.isNullOrBlank()) {
                    append("\nEndereço: ${ponto.enderecoCoordenada}")
                }
                append("\n\nVeja no mapa: https://maps.google.com/maps?q=${ponto.latitude},${ponto.longitude}")
            }
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, textoCompartilhar)
            startActivity(Intent.createChooser(intent, "Compartilhar ponto turístico"))
        }

        btnVerMapa.setOnClickListener {
            val url = "https://maps.google.com/maps?q=${ponto.latitude},${ponto.longitude}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        btnIr.setOnClickListener {
            val gmmIntentUri = Uri.parse("google.navigation:q=${ponto.latitude},${ponto.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        // Solicita permissão e obtém localização real
        checkLocationPermissionAndFetch()
    }

    private fun preencherCampos() {
        textoNome.text = ponto.nome
        textoDescricao.text = ponto.descricao
        textoEnderecoCoordenada.text = "Endereço: ${ponto.enderecoCoordenada ?: "Não encontrado"}"

        if (!ponto.imagem.isNullOrEmpty() && File(ponto.imagem!!).exists()) {
            val bitmap = BitmapFactory.decodeFile(ponto.imagem)
            imagemPonto.setImageBitmap(bitmap)
        } else {
            imagemPonto.setImageResource(R.drawable.sem_imagem)
        }

        if (!ponto.imagemMapa.isNullOrEmpty() && File(ponto.imagemMapa!!).exists()) {
            val bitmapMapa = BitmapFactory.decodeFile(ponto.imagemMapa)
            imagemMapa.setImageBitmap(bitmapMapa)
        } else {
            imagemMapa.setImageResource(R.drawable.sem_mapa)
        }

        atualizarBotaoFavorito()

        // Inicialmente exibe mensagem genérica, depois atualiza com distância real
        textoDistancia.text = "Calculando distância..."
    }

    private fun atualizarBotaoFavorito() {
        btnFavoritar.text = if (ponto.favorito) "Desfavoritar" else "Favoritar"
    }

    // Verifica permissão e pede se necessário
    private fun checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            obterLocalizacaoUsuario()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obterLocalizacaoUsuario()
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
                textoDistancia.text = "Permissão de localização negada"
            }
        }
    }

    // Obtém a última localização conhecida do usuário e calcula distância
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun obterLocalizacaoUsuario() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    usuarioLat = location.latitude
                    usuarioLon = location.longitude
                    calcularDistanciaUsuario(ponto.latitude, ponto.longitude)
                } else {
                    Toast.makeText(this, "Não foi possível obter a localização atual", Toast.LENGTH_SHORT).show()
                    textoDistancia.text = "Localização não disponível"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao obter localização: ${it.message}", Toast.LENGTH_SHORT).show()
                textoDistancia.text = "Erro ao obter localização"
            }
    }

    // Calcula a distancia do Usuario
    private fun calcularDistanciaUsuario(destLat: Double, destLon: Double) {
        if (usuarioLat == null || usuarioLon == null) {
            textoDistancia.text = "Localização do usuário não disponível"
            return
        }

        val results = FloatArray(1)
        Location.distanceBetween(usuarioLat!!, usuarioLon!!, destLat, destLon, results)
        val distanciaKm = results[0] / 1000

        textoDistancia.text = "Distância: %.2f km".format(distanciaKm)
    }
}