package com.jasmeet.cryptoapp.fragment.LoginRegister

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.jasmeet.cryptoapp.R
import com.jasmeet.cryptoapp.activities.MainActivity
import com.jasmeet.cryptoapp.databinding.FragmentLoginBinding
import com.jasmeet.cryptoapp.fragment.models.UserInfo

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding?=null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private  var db= FirebaseFirestore.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       _binding = FragmentLoginBinding.inflate(layoutInflater)

        auth = Firebase.auth
        firebaseAuth = FirebaseAuth.getInstance()
        _binding = FragmentLoginBinding.inflate(layoutInflater)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.tvForgotPassword.paintFlags = binding.tvForgotPassword.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        binding.tvDontHaveAccount.paintFlags = binding.tvDontHaveAccount.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.tvEmail.error = "Email Required"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tvEmail.error = "Enter a Email !"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 6) {
                binding.tvPassword.error = "6 char password required"
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }

            loginUserWithEmailAndPassword(email, password)
        }

        // text watcher for email
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvEmail.error = null
                binding.tvEmail.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.blue)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // text watcher for password
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvPassword.error = null
                binding.tvPassword.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.blue)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        binding.googleLogin.setOnClickListener {
            loginWithGoogle()
            binding.mainLayout.alpha = 0.5f
        }


        binding.tvDontHaveAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }


        return binding.root
    }


    private fun loginWithGoogle() {
        // start google sign in activity
        binding.mainLayout.alpha = 0.5f
        val signInIntent = googleSignInClient.signInIntent
        binding.progressBar2.visibility = View.VISIBLE
        resultLauncher.launch(signInIntent)

        //disable the back press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.mainLayout.alpha = 1f
                binding.progressBar2.visibility = View.INVISIBLE
                Toast.makeText(requireContext(), "Process canceled by user!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // result launcher for google sign in
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {

                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(Exception::class.java)
                firebaseAuthWithGoogle(account?.idToken!!)
            } catch (e: Exception) {

                // Google Sign In failed, update UI appropriately
                Toast.makeText(requireContext(), "Google Sign In Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // firebase auth with google
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    binding.mainLayout.alpha = 1f
                    binding.progressBar2.visibility = View.INVISIBLE

                    // Sign in success, update UI with the signed-in user's information
                    val email = firebaseAuth.currentUser?.email.toString()
                    val userName = firebaseAuth.currentUser?.displayName.toString()

                    val user = firebaseAuth.currentUser
                    val userInfo = UserInfo(
                        userName = userName,
                        userEmail = email,
                    )
                    // add user info to firestore
                    db.collection("user").document(user?.uid.toString()).set(userInfo)
                        .addOnSuccessListener {
                            Log.d("TAG", "DocumentSnapshot successfully written!")
                        }
                        .addOnFailureListener {
                            Log.d("TAG", "Error writing document", it)
                        }

                    Toast.makeText(requireContext(), "Google Sign In Success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                } else {
                    binding.mainLayout.alpha = 1f
                    binding.progressBar2.visibility = View.INVISIBLE
                    // If sign in fails, display a message to the user.
                    Toast.makeText(requireContext(), "Google Sign In Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loginUserWithEmailAndPassword(email: String, password: String) {
        binding.btnLogin.startAnimation()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.etEmail.text= null
                    binding.etPassword.text = null
                    binding.btnLogin.revertAnimation()
                    Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    binding.btnLogin.revertAnimation()
                    Toast.makeText(requireContext(), task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}