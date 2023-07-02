package com.example.map_practice

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.map_practice.databinding.ActivityMainBinding
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = binding.mapView

        if (checkLocationService()) {
            permissionCheck()
        } else {
            Toast.makeText(this, "GPS를 켜주세요", Toast.LENGTH_LONG).show()
        }
    }

    private fun permissionCheck() {
        val preference = getPreferences(Context.MODE_PRIVATE)
        val isFirstCheck = preference.getBoolean("isFirstPermissionCheck", true)
        if (ContextCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("현재 위치를 확인하시려면 위치 권한을 허용해주세요.")
                builder.setPositiveButton("확인") { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(ACCESS_FINE_LOCATION),
                        ACCESS_FINE_LOCATION_CODE
                    )
                }
                builder.setNegativeButton("취소") { dialog, which ->

                }
                builder.show()
            } else {
                if (isFirstCheck) {
                    preference.edit().putBoolean("isFirstPermissionCheck", false).apply()
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(ACCESS_FINE_LOCATION),
                        ACCESS_FINE_LOCATION_CODE
                    )
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("현재 위치를 확인하시려면 설정에서 위치 권한을 허용해주세요.")
                    builder.setPositiveButton("설정으로 이동") { dialog, which ->
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    }
                    builder.setNegativeButton("취소") { dialog, which -> }
                    builder.show()
                }
            }
        } else {
            startTracking()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_FINE_LOCATION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "위치 권한이 승인되었습니다.", Toast.LENGTH_LONG).show()
                startTracking()
            } else {
                Toast.makeText(this, "위치 권한이 거절되었습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkLocationService(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    // 현재 사용자 위치추적
    @SuppressLint("MissingPermission")
    private fun startTracking() {
        binding.mapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading

        val lm: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
    }

    // 위치 리스너
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val uNowPosition = MapPoint.mapPointWithGeoCoord(location.latitude, location.longitude)

            // 현 위치에 마커 찍기
            val marker = MapPOIItem()
            marker.itemName = "현 위치"
            marker.mapPoint = uNowPosition
            marker.markerType = MapPOIItem.MarkerType.BluePin
            marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin
            binding.mapView.addPOIItem(marker)

            mapView.setMapCenterPoint(uNowPosition, true) // 지도 화면 업데이트
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    // 위치추적 중지
    private fun stopTracking() {
        binding.mapView.currentLocationTrackingMode =
            MapView.CurrentLocationTrackingMode.TrackingModeOff
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
    }

    companion object {
        const val ACCESS_FINE_LOCATION_CODE = 1000
    }
}


