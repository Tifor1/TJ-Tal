package translator.tifor

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import translator.tifor.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var condition = DownloadConditions.Builder().build()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var spSourceLanguage: Spinner
    private lateinit var spTargetLanguage: Spinner

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.navigationBarColor = resources.getColor(R.color.nav)
        setContentView(binding.root)

        spSourceLanguage = findViewById(R.id.spSourceLanguage)
        spTargetLanguage = findViewById(R.id.spTargetLanguage)


        val sourceLanguages = listOf(
            "English", "Bangla", "Hindi", "Urdu", "Japanese", "Chinese", "Arabic", "Slovak "
        ) // Replace with actual language options
        val targetLanguages = listOf(
            "Bangla", "English", "Hindi", "Urdu", "Japanese", "Chinese", "Arabic", "Slovak "
        ) // Replace with actual language options


        val sourceAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, sourceLanguages)
        sourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spSourceLanguage.adapter = sourceAdapter

        val targetAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, targetLanguages)
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTargetLanguage.adapter = targetAdapter


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        binding.textImage.setOnClickListener {

            try {

                val intent = Intent(this, ImTeActivity::class.java)
                startActivity(intent)


            } catch (e: Exception) {

                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()

            }

        }


        textToSpeech = TextToSpeech(this) {

            textToSpeech.setLanguage(Locale("bn_IN"))
        }
        binding.etMic.setOnClickListener {

            textToSpeech.speak(binding.editText.text.toString(), TextToSpeech.QUEUE_FLUSH, null)
        }
        binding.textMic.setOnClickListener {

            textToSpeech.speak(binding.tView.text.toString(), TextToSpeech.QUEUE_FLUSH, null)
        }

        /*
                binding.textCopy.setOnClickListener { textcopy(binding.tView.text.toString()) }

                binding.etCopy.setOnClickListener { textcopy(binding.editText.text.toString()) }*/

        binding.clear.setOnClickListener {
            try {
                binding.etCopy.visibility = View.GONE
                binding.clear.visibility = View.GONE
                binding.textCopy.visibility = View.GONE
                binding.etMic.visibility = View.GONE
                binding.textMic.visibility = View.GONE
                binding.editText.text = null
                binding.tView.text = null

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_SHORT).show()
            }
        }


        binding.btn.setOnClickListener {

            if (!binding.editText.text.toString().isEmpty()) {

                binding.tView.setTextColor(getColor(R.color.black))

                val options = TranslatorOptions.Builder().setSourceLanguage(seleceTo())
                    .setTargetLanguage(selectFrom()).build()

                val translator = Translation.getClient(options)

                translator.downloadModelIfNeeded(condition).addOnSuccessListener {

                    translator.translate(binding.editText.text.toString()).addOnSuccessListener {

                        binding.tView.text = it

                    }.addOnFailureListener {

                        Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_LONG).show()

                    }

                }.addOnFailureListener {

                    Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_LONG).show()

                }

                val lanTo: String = binding.spSourceLanguage.toString()
                val lanFrom: String = binding.spTargetLanguage.toString()

                sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("lanToKey", lanTo)
                editor.putString("lanFromKey", lanFrom)
                editor.apply()

                binding.clear.visibility = View.VISIBLE
                binding.textCopy.visibility = View.VISIBLE
                binding.etCopy.visibility = View.VISIBLE
                binding.etMic.visibility = View.VISIBLE
                binding.textMic.visibility = View.VISIBLE
            } else {
                binding.tView.setTextColor(getColor(R.color.red))
                binding.etCopy.visibility = View.GONE
                binding.textCopy.visibility = View.GONE
                binding.tView.text = "Enter Your Text !"
            }

        }


    }

    private fun seleceTo(): String {


        val sourceLanguage = spSourceLanguage.selectedItem.toString()

        return when (sourceLanguage) {


            "" -> TranslateLanguage.BENGALI
            "English" -> TranslateLanguage.ENGLISH

            "Bangla" -> TranslateLanguage.BENGALI
            "Hindi" -> TranslateLanguage.HINDI
            "Urdu" -> TranslateLanguage.URDU
            "Japanese" -> TranslateLanguage.JAPANESE
            "Chinese" -> TranslateLanguage.CHINESE
            "Arabic" -> TranslateLanguage.ARABIC
            "Slovak " -> TranslateLanguage.SLOVAK


            else -> TranslateLanguage.BENGALI


        }


    }

    private fun selectFrom(): String {

        val targetLanguage = spTargetLanguage.selectedItem.toString()

        return when (targetLanguage) {

            "" -> TranslateLanguage.ENGLISH
            "Bangla" -> TranslateLanguage.BENGALI

            "English" -> TranslateLanguage.ENGLISH
            "Hindi" -> TranslateLanguage.HINDI
            "Urdu" -> TranslateLanguage.URDU
            "Japanese" -> TranslateLanguage.JAPANESE
            "Chinese" -> TranslateLanguage.CHINESE
            "Arabic" -> TranslateLanguage.ARABIC
            "Slovak " -> TranslateLanguage.SLOVAK


            else -> TranslateLanguage.ENGLISH

        }

    }

}