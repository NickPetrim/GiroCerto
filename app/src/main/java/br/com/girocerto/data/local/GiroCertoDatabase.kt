package br.com.girocerto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.girocerto.data.local.converter.RoomConverters
import br.com.girocerto.data.local.dao.ForumPostDao
import br.com.girocerto.data.local.dao.TransactionDao
import br.com.girocerto.data.local.dao.UserProfileDao
import br.com.girocerto.data.local.dao.WorkSessionDao
import br.com.girocerto.data.local.entity.BlockedAuthorEntity
import br.com.girocerto.data.local.entity.ForumPostEntity
import br.com.girocerto.data.local.entity.TransactionEntity
import br.com.girocerto.data.local.entity.UserProfileEntity
import br.com.girocerto.data.local.entity.WorkSessionEntity

@Database(
    entities = [
        TransactionEntity::class,
        WorkSessionEntity::class,
        UserProfileEntity::class,
        ForumPostEntity::class,
        BlockedAuthorEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class GiroCertoDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun workSessionDao(): WorkSessionDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun forumPostDao(): ForumPostDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                createV2Tables(db)
                seedInitialSocialAndProfile(db)
            }
        }

        fun createV2Tables(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS user_profile (
                    id INTEGER NOT NULL PRIMARY KEY,
                    displayName TEXT NOT NULL,
                    bikeModel TEXT NOT NULL,
                    dailyGoalCents INTEGER NOT NULL,
                    isDarkMode INTEGER NOT NULL,
                    emergencyContactName TEXT NOT NULL,
                    emergencyContactPhone TEXT NOT NULL,
                    emergencyChannel TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS forum_posts (
                    id TEXT NOT NULL PRIMARY KEY,
                    authorName TEXT NOT NULL,
                    category TEXT NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL,
                    locationArea TEXT NOT NULL,
                    supportCount INTEGER NOT NULL,
                    isSupportedByMe INTEGER NOT NULL,
                    isReported INTEGER NOT NULL,
                    createdAtEpochMillis INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS blocked_authors (
                    authorName TEXT NOT NULL PRIMARY KEY,
                    blockedAtEpochMillis INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }

        fun seedInitialSocialAndProfile(db: SupportSQLiteDatabase) {
            val now = System.currentTimeMillis()
            db.execSQL(
                """
                INSERT OR IGNORE INTO user_profile (
                    id, displayName, bikeModel, dailyGoalCents, isDarkMode,
                    emergencyContactName, emergencyContactPhone, emergencyChannel
                ) VALUES (1, 'Motoca SP', 'CG 160 Fan', 25000, 0, '', '', 'WHATSAPP')
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT OR IGNORE INTO forum_posts (
                    id, authorName, category, title, description, locationArea,
                    supportCount, isSupportedByMe, isReported, createdAtEpochMillis
                ) VALUES
                ('seed-1', 'Carlão ZL', 'BLITZ', 'Blitz moto na Radial Leste',
                 'Operação na faixa da direita logo após o viaduto Bresser sentido centro. Documentação em dia.',
                 'Radial Leste · Bresser', 18, 0, 0, ${now - 900_000L}),
                ('seed-2', 'Rafa Pinheiros', 'ACCIDENT', 'Pista com óleo na Marginal',
                 'Acidente leve na faixa local perto da Ponte Eusébio Matoso. Trânsito travado no corredor.',
                 'Marginal Pinheiros · Butantã', 12, 0, 0, ${now - 2_400_000L}),
                ('seed-3', 'Dudu Centro', 'SUPPORT_POINT', 'Ponto de apoio com banheiro e tomada',
                 'Posto na Av. Angélica libera carregador, água gelada e calibrador para entregadores.',
                 'Higienópolis · Av. Angélica', 25, 0, 0, ${now - 5_400_000L}),
                ('seed-4', 'Marcão Oficina', 'MECHANICS', 'Troca de óleo com chuva em SP',
                 'Com muita chuva e corredor travado, revise folga da corrente e pastilha a cada 1.000 km.',
                 'Vila Mariana · Manutenção', 9, 0, 0, ${now - 10_800_000L})
                """.trimIndent()
            )
        }
    }
}