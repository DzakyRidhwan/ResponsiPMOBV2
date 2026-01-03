package com.pmob.projectakhirpemrogramanmobile

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.pmob.projectakhirpemrogramanmobile.databinding.ActivitySetupProfileBinding
import com.pmob.projectakhirpemrogramanmobile.utils.ImageUtils
import java.util.Calendar

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupProfileBinding

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private var imageUri: Uri? = null

    private fun userPrefs(): SharedPreferences {
        val uid = auth.currentUser?.uid ?: error("User belum login")
        return getSharedPreferences("user_profile_$uid", MODE_PRIVATE)
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                binding.imgProfile.setImageURI(it)
                uploadPhoto(it)
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let {
                val uri = ImageUtils.saveBitmapToCache(this, it)
                imageUri = uri
                binding.imgProfile.setImageURI(uri)
                uploadPhoto(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDatePicker()
        loadProfile()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnChangePhoto.setOnClickListener {
            showPhotoOptionDialog()
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }

    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        val prefs = userPrefs()

        // ===== LOAD LOCAL =====
        binding.etFullName.setText(prefs.getString("fullName", ""))
        binding.etBirthPlace.setText(prefs.getString("birthPlace", ""))
        binding.etBirthDate.setText(prefs.getString("birthDate", ""))
        binding.etAddress.setText(prefs.getString("address", ""))
        binding.etHobby.setText(prefs.getString("hobby", ""))
        binding.etBio.setText(prefs.getString("bio", ""))

        when (prefs.getString("gender", "")) {
            "Laki-laki" -> binding.rbMale.isChecked = true
            "Perempuan" -> binding.rbFemale.isChecked = true
        }

        prefs.getString("photo", null)?.let {
            binding.imgProfile.setImageURI(Uri.parse(it))
        }

        // ===== SYNC FIREBASE =====
        database.child("users").child(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) return@addOnSuccessListener

                binding.etFullName.setText(snapshot.child("fullName").value?.toString())
                binding.etBirthPlace.setText(snapshot.child("birthPlace").value?.toString())
                binding.etBirthDate.setText(snapshot.child("birthDate").value?.toString())
                binding.etAddress.setText(snapshot.child("address").value?.toString())
                binding.etHobby.setText(snapshot.child("hobby").value?.toString())
                binding.etBio.setText(snapshot.child("bio").value?.toString())

                when (snapshot.child("gender").value?.toString()) {
                    "Laki-laki" -> binding.rbMale.isChecked = true
                    "Perempuan" -> binding.rbFemale.isChecked = true
                }

                snapshot.child("photo").value?.toString()?.let {
                    binding.imgProfile.setImageURI(Uri.parse(it))
                }
            }
    }

    private fun setupDatePicker() {
        binding.etBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val date = String.format("%02d/%02d/%04d", day, month + 1, year)
                    binding.etBirthDate.setText(date)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
                show()
            }
        }
    }

    private fun showPhotoOptionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Pilih Foto Profil")
            .setItems(arrayOf("Galeri", "Kamera")) { _, which ->
                when (which) {
                    0 -> galleryLauncher.launch("image/*")
                    1 -> cameraLauncher.launch(null)
                }
            }
            .show()
    }

    private fun uploadPhoto(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return

        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("profile_photos/$uid.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->

                    database.child("users")
                        .child(uid)
                        .child("photo")
                        .setValue(downloadUrl.toString())

                    getSharedPreferences(
                        "user_profile_$uid",
                        MODE_PRIVATE
                    ).edit()
                        .putString("photo", downloadUrl.toString())
                        .apply()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal upload foto", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: ""

        val fullName = binding.etFullName.text.toString().trim()
        val birthPlace = binding.etBirthPlace.text.toString().trim()
        val birthDate = binding.etBirthDate.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val hobby = binding.etHobby.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()

        val gender = when (binding.rgGender.checkedRadioButtonId) {
            R.id.rbMale -> "Laki-laki"
            R.id.rbFemale -> "Perempuan"
            else -> ""
        }

        if (fullName.isEmpty()) {
            binding.etFullName.error = "Nama wajib diisi"
            return
        }

        if (gender.isEmpty()) {
            Toast.makeText(this, "Pilih jenis kelamin", Toast.LENGTH_SHORT).show()
            return
        }

        if (birthPlace.isEmpty()) {
            binding.etBirthPlace.error = "Tempat lahir wajib diisi"
            return
        }

        if (birthDate.isEmpty()) {
            binding.etBirthDate.error = "Tanggal lahir wajib diisi"
            return
        }

        if (address.isEmpty()) {
            binding.etAddress.error = "Alamat wajib diisi"
            return
        }

        val userData = mapOf(
            "fullName" to fullName,
            "gender" to gender,
            "birthPlace" to birthPlace,
            "birthDate" to birthDate,
            "address" to address,
            "hobby" to hobby,
            "bio" to bio,
            "email" to email
        )

        database.child("users")
            .child(uid)
            .updateChildren(userData)
            .addOnSuccessListener {

                userPrefs()
                    .edit()
                    .apply {
                        putString("fullName", fullName)
                        putString("gender", gender)
                        putString("birthPlace", birthPlace)
                        putString("birthDate", birthDate)
                        putString("address", address)
                        putString("hobby", hobby)
                        putString("bio", bio)
                        putString("email", email)
                        apply()
                    }

                Toast.makeText(this, "Profil berhasil disimpan", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan profil", Toast.LENGTH_SHORT).show()
            }
    }
}
