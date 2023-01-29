package com.jasmeet.cryptoapp.fragment.LoginRegister

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.jasmeet.cryptoapp.R
import com.jasmeet.cryptoapp.databinding.FragmentForgotPasswordBinding

class ForgotPasswordFragment : BottomSheetDialogFragment() {

    private var _binding : FragmentForgotPasswordBinding?= null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentForgotPasswordBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                binding.tvEmail.error = "Email Required"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tvEmail.error = "Valid Email Required"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }
            binding.btnResetPassword.startAnimation()
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        binding.btnResetPassword.revertAnimation()
                        binding.successAnimation.visibility = View.VISIBLE
                        binding.tvSuccess.visibility = View.VISIBLE
                        binding.textView7.visibility = View.GONE
                        binding.btnResetPassword.visibility = View.GONE
                        binding.tvEmail.visibility = View.GONE
                    }
                    else {
                        binding.btnResetPassword.revertAnimation()
                        Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
        }

        //textWatcher for email
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvEmail.error = null
                binding.tvEmail.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.blue)

            }
        })
        return binding.root
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }


}