package utfpr.edu.br.pm46sturismo.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissaoUtil {

    /**
     * Verifica se a permissão foi concedida.
     */
    fun verificarPermissao(activity: Activity, permissao: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permissao) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Solicita permissão ao usuário.
     */
    fun solicitarPermissao(activity: Activity, permissao: String, codigo: Int) {
        ActivityCompat.requestPermissions(activity, arrayOf(permissao), codigo)
    }

    /**
     * Verifica se deve exibir uma explicação antes de solicitar.
     */
    fun deveMostrarExplicacao(activity: Activity, permissao: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permissao)
    }
}