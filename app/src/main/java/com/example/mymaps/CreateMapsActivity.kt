package com.example.mymaps

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.mymaps.Models.Place
import com.example.mymaps.Models.UserMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.example.mymaps.databinding.ActivityCreateMapsBinding

class CreateMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = "CreateMapsActivity"
    private val markers: MutableList<Marker> = mutableListOf()
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityCreateMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the title from the intent
        val mapTitle = intent.getStringExtra(EXTRA_MAP_TITLE) ?: "New Map"

        // Set the toolbar title to the map title
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Optional: To enable back button
        supportActionBar?.title = mapTitle // Set the toolbar title to the passed map title

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Show Snackbar to inform user about adding markers
        mapFragment.view?.let {
            Snackbar.make(it, "Long Press to add marker!", Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok", {})
                .setActionTextColor(ContextCompat.getColor(this, android.R.color.black))
                .show()
        }
    }

    // Inflate the menu to show the "Save" button
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_create_map, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Handle action when the "Save" button is clicked
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.miSave) {
            println("Tapped on save")

            // Check if there are markers on the map
            if (markers.isEmpty()) {
                Toast.makeText(this, "There must be at least one marker on the map", Toast.LENGTH_LONG).show()
                return true
            }

            // Convert markers to Place objects
            val places = markers.map { marker ->
                marker.title?.let { title ->
                    marker.snippet?.let { description ->
                        Place(title, description, marker.position.latitude, marker.position.longitude)
                    }
                }
            }.filterNotNull() // Remove any null entries from the list

            // Create the UserMap object
            val userMap = intent.getStringExtra(EXTRA_MAP_TITLE)?.let { UserMap(it, places) }

            // Pass the UserMap back to MainActivity
            val data = Intent()
            data.putExtra(EXTRA_USER_MAP, userMap)
            setResult(Activity.RESULT_OK, data)
            finish()  // Immediately finish after setting the result
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // This callback is triggered when the map is ready to be used
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener { latLng ->
            showAlertDialog(latLng)  // Show dialog to add a marker when the user long presses
        }

        // Move the camera to a default location (Delhi in this case)
        val delhi = LatLng(28.65195, 77.23149)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(delhi, 10f))
    }

    // Show the dialog to add a marker
    private fun showAlertDialog(latLng: LatLng) {
        val placeFormView = LayoutInflater.from(this).inflate(R.layout.dialog_create_place, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Create a Marker")
            .setView(placeFormView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null)
            .show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val title = placeFormView.findViewById<EditText>(R.id.etTitle).text.toString()
            val description = placeFormView.findViewById<EditText>(R.id.etDescription).text.toString()
            if (title.trim().isEmpty() || description.trim().isEmpty()) {
                Toast.makeText(this, "Place must have non-empty title and description", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Add marker to the map
            val marker = mMap.addMarker(MarkerOptions().position(latLng).title(title).snippet(description))
            marker?.let { markers.add(it) }
            dialog.dismiss()
        }
    }
}
