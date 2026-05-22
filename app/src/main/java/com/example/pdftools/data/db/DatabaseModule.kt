package com.example.pdftools.data.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): PdfToolsDatabase {
        return Room.databaseBuilder(
            context,
            PdfToolsDatabase::class.java,
            "pdf_tools.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(database: PdfToolsDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    @Singleton
    fun provideRecentFileDao(database: PdfToolsDatabase): RecentFileDao {
        return database.recentFileDao()
    }
}
