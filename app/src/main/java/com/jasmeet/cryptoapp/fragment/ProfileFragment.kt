package com.jasmeet.cryptoapp.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jasmeet.cryptoapp.activities.LoginRegisterActivity
import com.jasmeet.cryptoapp.databinding.DialogLogoutBinding
import com.jasmeet.cryptoapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var bindingDialogLogoutBinding: DialogLogoutBinding
    private lateinit var db : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        bindingDialogLogoutBinding = DialogLogoutBinding.inflate(layoutInflater)

        _binding = FragmentProfileBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(bindingDialogLogoutBinding.root)
            .create()
        dialog.setCanceledOnTouchOutside(false)

        //get the current user info
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        val user = firebaseAuth.currentUser


        binding.email.text = user?.email
       binding.name.text = user?.displayName
        binding.uid.text = user?.uid

        //get the user data from firestore
        db.collection("user").document(user?.uid!!).get().addOnSuccessListener {
            binding.name.text = it.getString("userName")
            binding.email.text = it.getString("userEmail")
            binding.uid.text = it.getString("uid")
        }.addOnFailureListener {
            binding.name.text = user.displayName
            binding.email.text = user.email
            binding.uid.text = user.uid
        }

        binding.button6.setOnClickListener {

            dialog.show()

            bindingDialogLogoutBinding.btnNo.setOnClickListener {
                dialog.dismiss()
            }
            bindingDialogLogoutBinding.btnYes.setOnClickListener {
                firebaseAuth.signOut()
                val intent = Intent(requireActivity(), LoginRegisterActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            bindingDialogLogoutBinding.tvClose.setOnClickListener {
                dialog.dismiss()
            }
        }

        return binding.root
    }
}