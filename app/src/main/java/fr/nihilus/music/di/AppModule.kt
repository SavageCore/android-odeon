/*
 * Copyright 2018 Thibault Seisel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.nihilus.music.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import fr.nihilus.music.OdeonApplication
import javax.inject.Singleton

/**
 * The main module for this application.
 * It defines dependencies that cannot be instantiated with a constructor,
 * such as implementations for abstract types or calls to factory methods.
 */
@Module(includes = [AppModuleBinds::class])
class AppModule {

    @Provides
    fun provideContext(application: OdeonApplication): Context = application.applicationContext

    @Provides @Singleton
    fun provideAppPreferences(application: OdeonApplication): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(application)
}