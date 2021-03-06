package `in`.bitotsav.koin

import `in`.bitotsav.database.AppDatabase
import `in`.bitotsav.database.MIGRATION_1_2
import `in`.bitotsav.events.data.EventRepository
import `in`.bitotsav.events.ui.EventViewModel
import `in`.bitotsav.events.ui.NightViewModel
import `in`.bitotsav.events.ui.ScheduleViewModel
import `in`.bitotsav.feed.data.FeedRepository
import `in`.bitotsav.feed.ui.FeedViewModel
import `in`.bitotsav.info.ui.InfoViewModel
import `in`.bitotsav.profile.api.AuthenticationService
import `in`.bitotsav.profile.data.UserRepository
import `in`.bitotsav.profile.ui.LoginViewModel
import `in`.bitotsav.profile.ui.ProfileViewModel
import `in`.bitotsav.profile.ui.RegistrationViewModel
import `in`.bitotsav.shared.ui.UiUtilViewModel
import `in`.bitotsav.teams.championship.data.ChampionshipTeamRepository
import `in`.bitotsav.teams.nonchampionship.data.NonChampionshipTeamRepository
import `in`.bitotsav.teams.ui.LeaderboardViewModel
import android.content.Context.MODE_PRIVATE
import androidx.room.Room
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val baseUrl = "https://bitotsav.in/api/app/"

// TODO [Refactor]: Should modules conform to package division?
val repositoriesModule = module {

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "App.db")
            .addMigrations(MIGRATION_1_2)
            .build()
    }
    single { EventRepository(get<AppDatabase>().eventDao()) }
    single { FeedRepository(get<AppDatabase>().feedDao()) }
    single { ChampionshipTeamRepository(get<AppDatabase>().championshipTeamDao()) }
    single { NonChampionshipTeamRepository(get<AppDatabase>().nonChampionshipTeamDao()) }
    single { UserRepository(get<AppDatabase>().userDao()) }
}

val retrofitModule = module {

    single("custom_gson") {
        //        Use @Expose(serialize = false, deserialize = false) with this gson to ignore both serialization and
//        de-serialization of particular members or fields
        GsonBuilder().addDeserializationExclusionStrategy(
            object : ExclusionStrategy {
                override fun shouldSkipField(fieldAttributes: FieldAttributes): Boolean {
                    val expose = fieldAttributes.getAnnotation(Expose::class.java)
                    return expose != null && !expose.deserialize
                }

                override fun shouldSkipClass(aClass: Class<*>): Boolean {
                    return false
                }
            }
        ).create()
    }
    // TODO @ashank : Check if custom client is required
    single {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(get<Gson>("custom_gson")))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }

    single<AuthenticationService> { get<Retrofit>().create(AuthenticationService::class.java) }
}

val sharedPrefsModule = module {
    factory {
        androidContext().getSharedPreferences("profile", MODE_PRIVATE)
    }
}

val viewModelsModule = module {
    viewModel { ScheduleViewModel(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { RegistrationViewModel(get()) }
    viewModel { UiUtilViewModel() }
    viewModel { EventViewModel(get(), get()) }
    viewModel { FeedViewModel(get()) }
    viewModel { InfoViewModel() }
    viewModel { NightViewModel() }
    viewModel { LeaderboardViewModel(get()) }
}
