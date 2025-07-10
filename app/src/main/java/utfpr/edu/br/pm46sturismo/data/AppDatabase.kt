package utfpr.edu.br.pm46sturismo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import utfpr.edu.br.pm46sturismo.model.PontoTuristico

/**
 * Classe abstrata que representa o banco de dados Room da aplicação.
 *
 * Esta classe define o banco de dados, suas entidades (tabelas) e fornece o DAO
 * para acesso aos dados dos pontos turísticos.
 *
 * - Entidades: PontoTuristico
 * - Versão: 4
 *
 * O banco utiliza o padrão Singleton para garantir uma única instância durante o ciclo de vida do app.
 */
@Database(entities = [PontoTuristico::class], version = 4)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Retorna o DAO (Data Access Object) para operações na tabela PontoTuristico.
     *
     * @return PontoTuristicoDao responsável pelos métodos de CRUD no banco.
     */
    abstract fun pontoTuristicoDao(): PontoTuristicoDao

    companion object {
        /**
         * Instância Singleton do banco de dados.
         *
         * A anotação @Volatile garante que a instância seja visível para todas as threads.
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtém a instância do banco de dados, criando-a se necessário.
         *
         * Usa Room.databaseBuilder para criar o banco chamado "pontos_turisticos.db".
         * O método fallbackToDestructiveMigration() apaga o banco caso haja incompatibilidade de versão,
         * útil para evitar crashes em fase de desenvolvimento.
         *
         * @param context Contexto da aplicação
         * @return Instância única de AppDatabase
         */
        fun getDatabase(context: Context): AppDatabase {
            // Retorna a instância existente ou cria uma nova em caso de null
            return INSTANCE ?: synchronized(this) {
                // Cria o banco de dados usando Room, contexto da aplicação, e nome do arquivo
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pontos_turisticos.db"
                )
                    // Destroi e recria o banco se houver mudança de versão sem migration
                    .fallbackToDestructiveMigration()
                    .build()
                // Guarda a instância criada para usos futuros
                INSTANCE = instance
                instance
            }
        }
    }
}