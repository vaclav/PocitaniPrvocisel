package com.example.myapplication

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    /*
    Servise na pozadi, ktery pocita prvocisla
     */
    var myService: MyService? = null

    /*
    Udava, zda jsme pripojeni k servisu
     */
    var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        konec.isEnabled = false

        // Tlacitkop start spusti servis na vypocet prvocisel
        start.setOnClickListener {
            startService()
        }

        // Tlacitko konec zastavi servis na vypocet prvocisel
        konec.setOnClickListener {
            stopService()
        }

        // Tlacitko Nacti ziska aktualni nejvetsi vypoctene prvocislo ze servisu a zobrazi ho
        nacistVysledky.setOnClickListener {
            if (isConnected) {
                val cas = myService?.dodejCasOdStartu()
                casVysledek.setText("Servis běží již " + cas + " sekund")
                val prvocislo = myService?.dodejNejvetsiPrvocislo()
                prvocisloVysledek.setText("Největší nalezené prvočíslo: " + prvocislo)
            } else {
                // Pokud servis pro vypocet prvocisel nebezi, zobraz uzivateli dialog
                AlertDialog.Builder(this)
                    .setTitle("Upozornění")
                    .setMessage("Služba neběží, musíte ji nastartovat!")
                    .setPositiveButton("Nastartovat!",
                        DialogInterface.OnClickListener { dialog, which ->
                            // Tlacitko Nastartovat v dialogu umi servis na vypocet prvocisel zpustit
                            startService()
                        })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        }
        // Zobrazi nastaveni zvuku
        sound.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SOUND_SETTINGS))
        }

        // Zobrazi nastaveni wifi
        wifi.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        // Vypne upozornovani na udalosti
        focus.setOnClickListener {
            // Otestuje, zda mame pravo omezovat zobrazovani udalosti
            if (!(this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                startActivity(intent)
            }
            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        }

        // Zapne upozornovani na udalosti
        ruch.setOnClickListener {
            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
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
