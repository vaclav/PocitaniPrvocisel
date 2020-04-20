package com.example.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class MyIndependentService : Service() {

    /*
    Kanal pro komunikaci s volajicimi aktivitami
     */
    private val channel = MyChannelToService()

    /*
    Cas od startu vypoctu
     */
    private var pocetSekund = AtomicInteger(0)

    /*
    Nejvetsi nalezene prvocislo
     */
    private var prvocislo = AtomicLong(1)

    /*
    Nastaveno na true, pokud vypocet bezi, false pokud nebezi
     */
    private var pocitamePrvocisla = AtomicBoolean(false)

    /*
    Vlakno vypoctu prvocisel
     */
    private var t: Thread? = null

    /*
    Android planovac udalosti
     */
    val timerHandler = Handler()

    /*
    Moje hodinky, ktere posouvaji hodnotu v "pocetSekund" kazdou sekundu o 1
     */
    val casomira = TimeRunnable(timerHandler)

    /*
    Pri pripojeni aktivity spust hodinky a nastartuj vypocet prvocisel
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val r = super.onStartCommand(intent, flags, startId)
        timerHandler.postDelayed(casomira, 1000);
        t = pocitejPrvocisla()
        return r
    }

    override fun onBind(intent: Intent?): IBinder? {
        return channel
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    /*
         Pri odpojeni aktivity zastav hodinky a ukonci vypocet prvocisel
        */
    override fun onDestroy() {
        timerHandler.removeCallbacks(casomira);
        prestanPocitatPrvocisla()
    }

    private fun pocitejPrvocisla(): Thread? {

        //Pokud uz se pocita, nic noveho nedelej
        if (pocitamePrvocisla.get()) return t

        // Vytvor vlakno vypoctu pro pocitani prvocisel
        pocitamePrvocisla.set(true)
        val thread = Thread(Runnable {
            val current = prvocislo.get()
            var candidate = current
            while (pocitamePrvocisla.get()) {
                candidate += 1;
//                Log.d("Prvocisla", "Candidate: " + candidate)
                Thread.sleep(1000)
                var delitelNalezen = false
                for (i in (2..(Math.round(Math.sqrt(candidate.toDouble()))))) {
                    if (!pocitamePrvocisla.get()) {
                        Log.d("Prvocisla 2", "Zastavujeme vypocet prvocisel")
                        //Stop was required
                        return@Runnable
                    }
                    if (candidate % i == 0L) {
                        delitelNalezen = true
                        break
                    }
                }
                if (!delitelNalezen) {
                    Log.d("Prvocisla 2", "Found: " + candidate)
                    prvocislo.set(candidate)
                }
            }
        })
        //Nastartuj vlakno
        thread.start()
        return thread
    }

    private fun prestanPocitatPrvocisla() {
        // Nastav priznak ukonceni, aby vlakno vypoctu prvocisel vedelo, ze se ma zastavit
        pocitamePrvocisla.set(false)
    }

    fun dodejCasOdStartu(): Int {
        return pocetSekund.get()
    }

    fun dodejNejvetsiPrvocislo(): Long {
        return prvocislo.get()
    }

    /**
     * Kazdou sekundu se vzbudi, zvedne pocitadlo sekund o 1 a naplanuje se zase za 1 sekundu
     */
    inner class TimeRunnable(val handler: Handler) : Runnable {
        override fun run() {
            pocetSekund.incrementAndGet()
            handler.postDelayed(this, 1000);
        }
    }

    inner class MyChannelToService : Binder() {
        fun getService(): MyIndependentService {
            return this@MyIndependentService
        }
    }
}
