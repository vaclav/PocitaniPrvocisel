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

class MyService : Service() {

    private val channel = MyChannelToService()

    private var hodnota = AtomicInteger(0)
    private var prvocislo = AtomicLong(1)

    private var pocitamePrvocisla = AtomicBoolean(false)
    private var t : Thread? = null

    val timerHandler = Handler()
    val casomira = TimeRunnable(timerHandler)

    override fun onBind(intent: Intent): IBinder {
        timerHandler.postDelayed(casomira, 1000);
        t = pocitejPrvocisla()
        return channel
    }

    override fun onRebind(intent: Intent?) {
        timerHandler.postDelayed(casomira, 1000);
        t = pocitejPrvocisla()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        timerHandler.removeCallbacks(casomira);
        prestanPocitatPrvocisla()
        return true
    }

    private fun pocitejPrvocisla() : Thread? {

        if(pocitamePrvocisla.get()) return t

        pocitamePrvocisla.set(true)
        val thread = Thread(Runnable {
            val current = prvocislo.get()
            var candidate = current
            while (pocitamePrvocisla.get()) {
                candidate += 1;
//                Log.d("Prvocisla", "Candidate: " + candidate)
//                Thread.sleep(100)
                var delitelNalezen = false
                for (i in (2..(Math.round(Math.sqrt(candidate.toDouble()))))) {
                    if (!pocitamePrvocisla.get()) {
                        Log.d("Prvocisla", "Zastavujeme vypocet prvocisel")
                        //Stop was required
                        return@Runnable
                    }
                    if (candidate % i == 0L) {
                        delitelNalezen = true
                        break
                    }
                }
                if (!delitelNalezen) {
                    Log.d("Prvocisla", "Found: " + candidate)
                    prvocislo.set(candidate)
                }
            }
        })
        thread.start()
        return thread
    }

    private fun prestanPocitatPrvocisla() {
        pocitamePrvocisla.set(false)
    }

    fun dodejCasOdStartu() : Int {
        return hodnota.get()
    }

    fun dodejNejvetsiPrvocislo() : Long {
        return prvocislo.get()
    }

    inner class TimeRunnable(val handler: Handler) : Runnable {
        override fun run() {
            hodnota.incrementAndGet()
            handler.postDelayed(this, 1000);
        }
    }

    inner class MyChannelToService : Binder() {
        fun getService() : MyService {
            return this@MyService
        }
    }
}
