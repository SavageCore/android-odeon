/*
 * Copyright 2021 Thibault Seisel
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

package fr.nihilus.music.library.permission

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import fr.nihilus.music.R
import fr.nihilus.music.core.ui.base.BaseFragment
import fr.nihilus.music.databinding.FragmentMissingPermissionBinding

/**
 * Acts as a landing page when permission to access external storage has not been granted.
 */
internal class MissingPermissionFragment : BaseFragment(R.layout.fragment_missing_permission) {

    private val requestPermission: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // Proceed to the home screen, whether or not the permission has been granted.
            findNavController().popBackStack()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMissingPermissionBinding.bind(view)

        binding.requestPermissionButton.setOnClickListener {
            requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}