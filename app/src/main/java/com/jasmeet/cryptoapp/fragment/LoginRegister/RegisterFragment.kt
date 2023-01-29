package com.jasmeet.cryptoapp.fragment.LoginRegister

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jasmeet.cryptoapp.R
import com.jasmeet.cryptoapp.databinding.FragmentRegisterBinding
import com.jasmeet.cryptoapp.fragment.models.UserInfo

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentRegisterBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val name = binding.etName.text.toString().trim()

            if (name.isEmpty()) {
                binding.tvName.error = "Name is required"
                binding.etName.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                binding.tvEmail.error = "Email is required"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tvEmail.error = "Please enter a valid email"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.tvPassword.error = "Password is required"
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.tvPassword.error = "Password should be at least 6 characters long"
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }
            signUpWithEmailPassword(name, email, password)
        }

        //text watcher for email
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvEmail.error = null
                binding.tvEmail.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.blue)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        //text watcher for name
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvName.error = null
                binding.tvName.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.blue)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        //text watcher for password
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvPassword.error = null
                binding.tvPassword.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.blue)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return binding.root

    }

    private fun signUpWithEmailPassword(name: String, email: String, password: String) {
        binding.btnLogin.startAnimation()
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    binding.btnLogin.revertAnimation()
                    val user = firebaseAuth.currentUser
                    val userInfo = UserInfo(
                        userEmail = email,
                        userName = name
                    )
                    db.collection("user").document(user?.uid.toString())
                        .set(userInfo)
                        .addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "Login to continue",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Error: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    binding.btnLogin.revertAnimation()
                    Toast.makeText(
                        requireContext(),
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}