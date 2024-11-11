package com.example.mymaps

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mymaps.Models.Place
import com.example.mymaps.Models.UserMap
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.example.mymaps.databinding.ActivityCreateMapsBinding
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream




class CreateMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = "CreateMapsActivity"
    private val markers: MutableList<Marker> = mutableListOf()
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityCreateMapsBinding
    private lateinit var mapTitle: String
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Retrieve the title from the intent
        mapTitle = intent.getStringExtra(EXTRA_MAP_TITLE) ?: "New Map"

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = mapTitle

        // Initialize the map fragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Show Snackbar for instructions
        mapFragment.view?.let {
            Snackbar.make(it, "Long Press to add marker!", Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok", {})
                .setActionTextColor(ContextCompat.getColor(this, android.R.color.black))
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_create_map, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.miSave) {
            if (markers.isEmpty()) {
                Toast.makeText(this, "There must be at least one marker on the map", Toast.LENGTH_LONG).show()
                return true
            }
            saveMapAndFinish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Request location permission and move the camera to the user's location if granted
        enableMyLocation()

        mMap.setOnMapLongClickListener { latLng ->
            showAlertDialog(latLng)
        }

        mMap.setOnInfoWindowClickListener { marker ->
            showDeleteMarkerDialog(marker)
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
            } else {
                Toast.makeText(this, "Unable to access current location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                Toast.makeText(this, "Location permission is needed to show your location on the map", Toast.LENGTH_LONG).show()
            }
        }
    }

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

            val marker = mMap.addMarker(MarkerOptions().position(latLng).title(title).snippet(description))
            marker?.let { markers.add(it) }
            dialog.dismiss()
        }
    }

    private fun showDeleteMarkerDialog(marker: Marker) {
        AlertDialog.Builder(this)
            .setTitle("Delete Marker")
            .setMessage("Are you sure you want to delete this marker?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                // Remove the marker from the list and the map, but do NOT save yet
                markers.remove(marker)
                marker.remove()
                Toast.makeText(this, "Marker deleted", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun saveMapAndFinish() {
        // Convert markers to Place objects
        val places = markers.map { marker ->
            marker.title?.let { title ->
                marker.snippet?.let { description ->
                    Place(title, description, marker.position.latitude, marker.position.longitude)
                }
            }
        }.filterNotNull()

        // Create UserMap object with updated list of places
        val userMap = UserMap(mapTitle, places)
        val data = Intent()
        data.putExtra(EXTRA_USER_MAP, userMap)
        setResult(Activity.RESULT_OK, data)

        finish()  // Finish after setting the result
    }

    private fun SerializeUserMap(context: Context, userMaps: List<UserMap>) {
        try {
            ObjectOutputStream(FileOutputStream(getDataFile(context))).use { it.writeObject(userMaps) }
            Log.d(TAG, "User maps serialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error serializing user maps", e)
        }
    }

    private fun getDataFile(context: Context): File {
        return File(context.filesDir, "usermaps.dat")
    }
}
