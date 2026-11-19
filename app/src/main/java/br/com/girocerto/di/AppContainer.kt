package br.com.girocerto.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.girocerto.core.sensors.LocationManagerMotionSource
import br.com.girocerto.core.util.ClockProvider
import br.com.girocerto.core.util.SystemClockProvider
import br.com.girocerto.core.voice.AndroidHudVoiceCopilot
import br.com.girocerto.core.voice.VoiceCopilot
import br.com.girocerto.data.local.GiroCertoDatabase
import br.com.girocerto.data.repository.RoomForumRepository
import br.com.girocerto.data.repository.RoomTransactionRepository
import br.com.girocerto.data.repository.RoomUserProfileRepository
import br.com.girocerto.domain.calculator.RideViabilityCalculator
import br.com.girocerto.domain.repository.ForumRepository
import br.com.girocerto.domain.repository.MotionSource
import br.com.girocerto.domain.repository.SampleWeatherRepository
import br.com.girocerto.domain.repository.TransactionRepository
import br.com.girocerto.domain.repository.UserProfileRepository
import br.com.girocerto.domain.repository.WeatherRepository
import br.com.girocerto.domain.validator.TransactionAmountValidator
import br.com.girocerto.feature.calculator.RideViabilityViewModel
import br.com.girocerto.feature.earnings.EarningsViewModel
import br.com.girocerto.feature.entry.QuickEntryViewModel
import br.com.girocerto.feature.forum.ForumViewModel
import br.com.girocerto.feature.history.HistoryViewModel
import br.com.girocerto.feature.home.HomeViewModel
import br.com.girocerto.feature.hud.HudViewModel
import br.com.girocerto.feature.profile.ProfileViewModel

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val clockProvider: ClockProvider = SystemClockProvider()
    val validator: TransactionAmountValidator = TransactionAmountValidator()
    val rideViabilityCalculator: RideViabilityCalculator = RideViabilityCalculator()

    val database: GiroCertoDatabase = Room.databaseBuilder(
        appContext,
        GiroCertoDatabase::class.java,
        "girocerto_local.db"
    )
        .addMigrations(GiroCertoDatabase.MIGRATION_1_2)
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                GiroCertoDatabase.seedInitialSocialAndProfile(db)
            }
        })
        .build()

    val transactionRepository: TransactionRepository = RoomTransactionRepository(
        transactionDao = database.transactionDao(),
        workSessionDao = database.workSessionDao(),
        clockProvider = clockProvider,
        validator = validator
    )

    val userProfileRepository: UserProfileRepository = RoomUserProfileRepository(
        userProfileDao = database.userProfileDao()
    )

    val forumRepository: ForumRepository = RoomForumRepository(
        forumPostDao = database.forumPostDao(),
        clockProvider = clockProvider
    )

    val motionSource: MotionSource = LocationManagerMotionSource(
        context = appContext,
        clockProvider = clockProvider
    )

    val weatherRepository: WeatherRepository = SampleWeatherRepository()

    fun createVoiceCopilot(): VoiceCopilot = AndroidHudVoiceCopilot(appContext)

    val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            HomeViewModel(transactionRepository, clockProvider, userProfileRepository)
        }
        initializer {
            EarningsViewModel(transactionRepository, clockProvider)
        }
        initializer {
            HistoryViewModel(transactionRepository, clockProvider)
        }
        initializer {
            QuickEntryViewModel(createSavedStateHandle(), transactionRepository, validator)
        }
        initializer {
            ForumViewModel(forumRepository, userProfileRepository)
        }
        initializer {
            ProfileViewModel(userProfileRepository)
        }
        initializer {
            RideViabilityViewModel(rideViabilityCalculator)
        }
        initializer {
            HudViewModel(
                motionSource = motionSource,
                weatherRepository = weatherRepository,
                voiceCopilot = createVoiceCopilot(),
                clockProvider = clockProvider
            )
        }
    }
}