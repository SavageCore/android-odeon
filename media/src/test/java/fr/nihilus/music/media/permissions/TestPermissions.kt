/*
 * Copyright 2019 Thibault Seisel
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

package fr.nihilus.music.media.permissions

import javax.inject.Inject

/**
 * All permissions are denied.
 */
internal object DeniedPermission : PermissionChecker {
    override val canReadExternalStorage: Boolean get() = false
    override val canWriteToExternalStorage: Boolean get() = false
}

/**
 * All permissions are granted.
 */
internal object GrantedPermission : PermissionChecker {
    override val canReadExternalStorage: Boolean get() = true
    override val canWriteToExternalStorage: Boolean get() = true
}

/**
 * A test [PermissionChecker] whose permissions can be granted or revoked manually.
 * By default all permissions are granted.
 */
internal class RevocablePermission @Inject constructor() : PermissionChecker {
    override var canReadExternalStorage: Boolean = true
    override var canWriteToExternalStorage: Boolean = true
}