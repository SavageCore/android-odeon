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

package fr.nihilus.music.dagger

import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import fr.nihilus.music.OdeonApplication
import fr.nihilus.music.media.di.MediaServiceModule
import javax.inject.Singleton

/**
 * The top-level component for this application.
 * Every injectable object annotated with `Singleton` is bound to it.
 */
@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        CommonModule::class,
        MediaServiceModule::class,
        ClientModule::class,
        FileViewerModule::class
    ]
)
interface AppComponent : AndroidInjector<OdeonApplication> {

    /**
     * Generate a builder for the application component.
     * This builder binds instance of the Android Application to the newly created component in
     * order to provide it as a dependency to any other object.
     */
    @Component.Factory
    interface Factory : AndroidInjector.Factory<OdeonApplication>
}
