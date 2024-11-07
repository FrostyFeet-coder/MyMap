package com.example.mymaps

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymaps.Models.Place
import com.example.mymaps.Models.UserMap

import com.example.mymaps.EXTRA_USER_MAP
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_USER_MAP = "EXTRA_USER_MAP"
        private const val EXTRA_MAP_TITLE = "EXTRA_MAP_TITLE"
        private const val REQUSET_CODE = 1234
    }

    private lateinit var rvMaps: RecyclerView
    private lateinit var userMaps: MutableList<UserMap>
    private lateinit var mapAdapter: MapsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Set the content view once

        // Initialize rvMaps before accessing it
        rvMaps = findViewById(R.id.rvMaps)

        enableEdgeToEdge() // Call enableEdgeToEdge without setting content view again

        // Apply window insets listener to adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize userMaps and generate sample data
        userMaps = generateSampleData().toMutableList()

        // Initialize the mapAdapter
        mapAdapter = MapsAdapter(this, userMaps, object : MapsAdapter.OnClickListener {
            override fun onItemClick(position: Int) {
                println("onItemClick $position")
                // When user taps on view in RV, navigate to new activity
                val intent = Intent(this@MainActivity, DisplayMapsActivity::class.java)
                intent.putExtra(EXTRA_USER_MAP, userMaps[position])
                startActivity(intent)
            }
        })

        // Set layout manager and adapter for rvMaps
        rvMaps.layoutManager = LinearLayoutManager(this)
        rvMaps.adapter = mapAdapter

        // Set up FloatingActionButton for creating a new map
        val fabCreateMap = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabCreateMap)
        fabCreateMap.setOnClickListener {
            println("Tap on FAB")
            showAlertDialog()
        }
    }

    private fun showAlertDialog() {
        val mapFormView = LayoutInflater.from(this).inflate(R.layout.dialog_create_map, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Map Title")
            .setView(mapFormView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null)
            .show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val title = mapFormView.findViewById<EditText>(R.id.etTitle).text.toString()
            if (title.trim().isEmpty()) {
                Toast.makeText(this, "Map must have non-empty title", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val intent = Intent(this@MainActivity, CreateMapsActivity::class.java)
            intent.putExtra(EXTRA_MAP_TITLE, title)
            startActivityForResult(intent, REQUSET_CODE)

            dialog.dismiss()
        }
    }

    // Handle activity result to add new map
// Handle the result after returning from CreateMapsActivity
// Handle the result after returning from CreateMapsActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUSET_CODE && resultCode == RESULT_OK) {
            val userMap = data?.getSerializableExtra(EXTRA_USER_MAP) as? UserMap
            userMap?.let {
                // Add the new map to the list
                userMaps.add(it)

                // Notify the adapter that a new map has been added
                mapAdapter.notifyItemInserted(userMaps.size - 1)

                // Scroll to the newly added map (optional)
                rvMaps.scrollToPosition(userMaps.size - 1)
            }
        }
    }
    private fun generateSampleData(): List<UserMap> {
        return listOf(
            UserMap(
                "Memories from University",
                listOf(
                    Place("Branner Hall", "Best dorm at Stanford", 37.426, -122.163),
                    Place("Gates CS building", "Many long nights in this basement", 37.430, -122.173),
                    Place("Pinkberry", "First date with my wife", 37.444, -122.170)
                )
            ),
            UserMap("January vacation planning!",
                listOf(
                    Place("Tokyo", "Overnight layover", 35.67, 139.65),
                    Place("Ranchi", "Family visit + wedding!", 23.34, 85.31),
                    Place("Singapore", "Inspired by \"Crazy Rich Asians\"", 1.35, 103.82)
                )),
            UserMap("Singapore travel itinerary",
                listOf(
                    Place("Gardens by the Bay", "Amazing urban nature park", 1.282, 103.864),
                    Place("Jurong Bird Park", "Family-friendly park with many varieties of birds", 1.319, 103.706),
                    Place("Sentosa", "Island resort with panoramic views", 1.249, 103.830),
                    Place("Botanic Gardens", "One of the world's greatest tropical gardens", 1.3138, 103.8159)
                )
            ),
            UserMap("My favorite places in the Midwest",
                listOf(
                    Place("Chicago", "Urban center of the midwest, the \"Windy City\"", 41.878, -87.630),
                    Place("Rochester, Michigan", "The best of Detroit suburbia", 42.681, -83.134),
                    Place("Mackinaw City", "The entrance into the Upper Peninsula", 45.777, -84.727),
                    Place("Michigan State University", "Home to the Spartans", 42.701, -84.482),
                    Place("University of Michigan", "Home to the Wolverines", 42.278, -83.738)
                )
            ),
            UserMap("Restaurants to try",
                listOf(
                    Place("Champ's Diner", "Retro diner in Brooklyn", 40.709, -73.941),
                    Place("Althea", "Chicago upscale dining with an amazing view", 41.895, -87.625),
                    Place("Shizen", "Elegant sushi in San Francisco", 37.768, -122.422),
                    Place("Citizen Eatery", "Bright cafe in Austin with a pink rabbit", 30.322, -97.739),
                    Place("Kati Thai", "Authentic Portland Thai food, served with love", 45.505, -122.635)
                )
            )
        )
    }
}