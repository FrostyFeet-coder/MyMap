package com.example.mymaps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.mymaps.Models.UserMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mymaps.databinding.ActivityDisplayMapsBinding
import com.google.android.gms.maps.model.LatLngBounds
import com.example.mymaps.EXTRA_USER_MAP

private const val TAG = "DisplayMapsActivity"

class DisplayMapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var userMap: UserMap
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityDisplayMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDisplayMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Get the UserMap object from the Intent
        userMap = intent.getSerializableExtra(EXTRA_USER_MAP) as UserMap
        supportActionBar?.title = userMap.title
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Log.i(TAG, "User map to render: ${userMap.title}")
        val BoundsBuilder = LatLngBounds.Builder()//to focus where only the marker are we used this
        for (place in userMap.places){ //for loop for every place in the list and mark pos in map
            val latlng = LatLng(place.latitude, place.longitude)
            BoundsBuilder.include(latlng)//to make bound builder work
            mMap.addMarker(MarkerOptions().position(latlng).title(place.title).snippet(place.description))

        }
        // Add a marker and move the camera
//       mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(BoundsBuilder.build(),1000,1000,0))
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(BoundsBuilder.build(),1000,1000,0))

    }
}
