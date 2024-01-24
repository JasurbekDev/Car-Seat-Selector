package com.example.carseatselector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.carseatselector.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), CarSeatSelectorView.CarSeatSelectorListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewCarSeatSelector.setCarSeatSelectorListener(this)
//        binding.viewCarSeatSelector.setUnavailable(listOf(2, 4))

        binding.viewCarSeatSelector.viewTreeObserver.addOnGlobalLayoutListener {
            binding.btnMultiState.text = binding.viewCarSeatSelector.isMultiSelect.toString()
        }

        binding.btnMultiState.setOnClickListener {
            binding.viewCarSeatSelector.setIsMultiSelect(!binding.viewCarSeatSelector.isMultiSelect)
            binding.btnMultiState.text = binding.viewCarSeatSelector.isMultiSelect.toString()
        }
    }

    override fun onSeatClick(selectedSeatPositions: List<Int>) {
        binding.textSelectedSeats.text = "Selected seats: $selectedSeatPositions"
    }
}