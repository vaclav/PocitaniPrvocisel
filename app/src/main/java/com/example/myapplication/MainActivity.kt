package com.example.myapplication

import android.app.AlertDialog
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var myService: MyService? = null
    var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        konec.isEnabled = false
        start.setOnClickListener {
            startService()
        }
        konec.setOnClickListener {
            stopService()
        }
        nacistVysledky.setOnClickListener {
            if (isConnected) {
                val cas = myService?.dodejCasOdStartu()
                casVysledek.setText("Servis běží již " + cas + " sekund")
                val prvocislo = myService?.dodejNejvetsiPrvocislo()
                prvocisloVysledek.setText("Největší nalezené prvočíslo: " + prvocislo)
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Upozornění")
                    .setMessage("Služba neběží, musíte ji nastartovat!")
                    .setPositiveButton("Nastartovat!",
                        DialogInterface.OnClickListener { dialog, which ->
                            startService()
                        })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        }
    }

    private fun startService() {
        bindService(Intent(this, MyService::class.java), myConnection, Context.BIND_AUTO_CREATE)
        start.isEnabled = false
        konec.isEnabled = true
        isConnected = true
    }

    private fun stopService() {
        unbindService(myConnection)
        start.isEnabled = true
        konec.isEnabled = false
        isConnected = false
    }

    private val myConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val channel = service as MyService.MyChannelToService
            myService = channel.getService()
            isConnected = true
        }

        override fun onServiceDisconnected(name: ComponentName) {

        }
    }
}
