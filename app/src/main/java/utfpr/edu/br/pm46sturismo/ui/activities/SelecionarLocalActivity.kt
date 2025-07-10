package utfpr.edu.br.pm46sturismo.ui.activities

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import utfpr.edu.br.pm46sturismo.R
import java.util.*

/**
 * Activity que permite ao usuário selecionar um local no mapa.
 * Ao clicar em um ponto do mapa, um marcador é adicionado.
 * Ao confirmar, retorna as coordenadas e endereço via Intent.
 */
class SelecionarLocalActivity : AppCompatActivity(), OnMapReadyCallback {

    // Instância do GoogleMap
    private lateinit var map: GoogleMap

    // Referência ao marcador criado pelo usuário
    private var marcador: Marker? = null

    // Guarda o local (latitude/longitude) selecionado pelo usuário
    private var localSelecionado: LatLng? = null

    /**
     * onCreate - Inicializa o mapa e configura o botão de confirmação.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selecionar_local)

        // Obtém o fragmento do mapa do layout
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Botão para confirmar o local selecionado
        val botaoConfirmar: Button = findViewById(R.id.botaoConfirmarLocal)
        botaoConfirmar.setOnClickListener {
            localSelecionado?.let {
                val endereco = obterEndereco(it.latitude, it.longitude)
                // Retorna latitude, longitude e endereço para a Activity de origem
                val intent = Intent()
                intent.putExtra("latitude", it.latitude)
                intent.putExtra("longitude", it.longitude)
                intent.putExtra("endereco", endereco)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    /**
     * Callback chamado quando o mapa está pronto.
     * Inicializa o ponto inicial e define listener de clique no mapa.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Define ponto inicial do mapa (exemplo: Pato Branco)
        val pontoInicial = LatLng(-25.0892, -50.1619)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(pontoInicial, 14f))

        // Ao clicar no mapa, cria/atualiza marcador e salva a posição selecionada
        map.setOnMapClickListener { latLng ->
            marcador?.remove() // Remove marcador anterior se houver
            marcador = map.addMarker(MarkerOptions().position(latLng).title("Local escolhido"))
            localSelecionado = latLng
        }
    }

    /**
     * Obtém o endereço a partir das coordenadas fornecidas usando o Geocoder.
     *
     * @param lat Latitude do local
     * @param lon Longitude do local
     * @return Endereço encontrado ou mensagem padrão se não encontrar
     */
    private fun obterEndereco(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val resultado = geocoder.getFromLocation(lat, lon, 1)
            resultado?.get(0)?.getAddressLine(0) ?: "Endereço não encontrado"
        } catch (e: Exception) {
            "Endereço não encontrado"
        }
    }
}