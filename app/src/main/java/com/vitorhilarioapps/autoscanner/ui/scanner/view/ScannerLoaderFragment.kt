package com.vitorhilarioapps.autoscanner.ui.scanner.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vitorhilarioapps.autoscanner.databinding.FragmentScannerLoaderBinding

class ScannerLoaderFragment : Fragment() {

    private var _binding : FragmentScannerLoaderBinding? = null
    private val binding get () = _binding!!

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, saved: Bundle?): View? {
        _binding = FragmentScannerLoaderBinding.inflate(LayoutInflater.from(group?.context), group, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val res = arguments?.getBoolean(OPEN_PREVIEW)

        if (res != null) {
            val action = when (res) {
                true -> ScannerLoaderFragmentDirections.actionLoaderToPreview(true)
                false -> ScannerLoaderFragmentDirections.actionLoaderToCam()
            }

            findNavController().navigate(action)
        } else requireActivity().finish()
    }

    companion object {
        const val OPEN_PREVIEW = "OPEN_PREVIEW"
    }
}