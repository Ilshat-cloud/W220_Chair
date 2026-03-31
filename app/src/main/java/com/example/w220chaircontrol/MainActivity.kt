package com.example.w220chaircontrol


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import android.content.res.ColorStateList
import android.util.TypedValue

private var readerThread: Thread? = null
private lateinit var consoleView: TextView
private lateinit var scrollConsole: ScrollView
private lateinit var connectButton: Button
private  var chairSelect =false
private lateinit var buttonBottomFrontward:Button
private lateinit var buttonBottomBackward: Button
private lateinit var buttonHeadrestUp: Button
private lateinit var buttonHeadrestDown: Button
private lateinit var buttonChairFrontward: Button
private lateinit var buttonChairBackward: Button
private lateinit var buttonReserve: Button
private lateinit var buttonG: Button
private lateinit var buttonI: Button
private lateinit var buttonJ: Button
private lateinit var buttonK: Button
private lateinit var buttonL: Button
private lateinit var buttonM: Button
private lateinit var buttonR: Button

// SeekBar
private lateinit var PWM_heater_seek_Bar: SeekBar
@SuppressLint("MissingPermission")

class MainActivity : AppCompatActivity() {

    // --- TCP Client state ---
    private var socket: Socket? = null
    private var out: OutputStream? = null
    private var input: InputStream? = null
    private var isConnected = false

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var messageFromBtnToSend :String

        connectButton = findViewById(R.id.button2)
        val switch1 = findViewById<Switch>(R.id.switch1)
        val button2 = findViewById<Button>(R.id.button2)
        buttonBottomFrontward =findViewById<Button>(R.id.Bottom_frontwardA);
        buttonBottomBackward =findViewById<Button>(R.id.Bottom_backwardB);
        buttonHeadrestUp =findViewById<Button>(R.id.Headrest_UPE);
        buttonHeadrestDown =findViewById<Button>(R.id.Headrest_downF);
        buttonChairFrontward =findViewById<Button>(R.id.Backrest_frontwardC);
        buttonChairBackward =findViewById<Button>(R.id.Backrest_backwardD);
        buttonReserve =findViewById<Button>(R.id.Heater_off_H);
        buttonG =findViewById<Button>(R.id.HeaterG);
        buttonI =findViewById<Button>(R.id.reserve_I);
        buttonJ =findViewById<Button>(R.id.heater2_J);
        buttonK =findViewById<Button>(R.id.heater3_K);
        buttonL =findViewById<Button>(R.id.heater4_L);
        buttonM =findViewById<Button>(R.id.heater3_M);
        buttonR =findViewById<Button>(R.id.heater3_R);
        PWM_heater_seek_Bar=findViewById<SeekBar>(R.id.PWM_heater_seekBar);

        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as android.net.wifi.WifiManager
        consoleView = findViewById<TextView>(R.id.consoleView)
        scrollConsole = findViewById<ScrollView>(R.id.scroll_console)
        //wifiManager.isWifiEnabled = true
        switch1.setOnCheckedChangeListener { _, isChecked ->if (isChecked)
            {
                switch1.text="Chair right"
                chairSelect=true

            }else{
                switch1.text="Chair left"
                chairSelect=false
            }
        }

        button2.setOnClickListener {
            if (!isConnected){
                Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show()
                connectToESP()
            }else
            {
                // Disconnect
                disconnectESP()
            }

        }
        //-------------------------------------------button processing---------------------------//
        PWM_heater_seek_Bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // пользователь начал двигать
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // пользователь отпустил

                when(seekBar.progress){
                    0->{
                        messageFromBtnToSend = if (chairSelect) "Ch232N" else "Ch232n"
                        sendToESP(messageFromBtnToSend)
                        val typedValue = TypedValue()
                        theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
                        val defaultColor = typedValue.data
                        seekBar.progressTintList = ColorStateList.valueOf(defaultColor)
                        seekBar.thumbTintList = ColorStateList.valueOf(defaultColor)
                    }
                    1->{
                        messageFromBtnToSend = if (chairSelect) "Ch232O" else "Ch232o"
                        sendToESP(messageFromBtnToSend)
                        seekBar.progressTintList = ColorStateList.valueOf(Color.GREEN)
                        seekBar.thumbTintList = ColorStateList.valueOf(Color.GREEN)
                    }
                    2->{
                        messageFromBtnToSend = if (chairSelect) "Ch232P" else "Ch232p"
                        sendToESP(messageFromBtnToSend)
                        seekBar.progressTintList = ColorStateList.valueOf(Color.YELLOW)
                        seekBar.thumbTintList = ColorStateList.valueOf(Color.YELLOW)
                    }
                    3->{
                        messageFromBtnToSend = if (chairSelect) "Ch232Q" else "Ch232q"
                        sendToESP(messageFromBtnToSend)
                        seekBar.progressTintList = ColorStateList.valueOf(Color.RED)
                        seekBar.thumbTintList = ColorStateList.valueOf(Color.RED)
                    }
                    else -> {
                        Toast.makeText(this@MainActivity, "SeekBar выбрано:"+seekBar.progress, Toast.LENGTH_SHORT).show()
                    }

                }
            }
        })

        buttonBottomFrontward.setOnClickListener {
            messageFromBtnToSend = if (chairSelect) "Ch232A" else "Ch232a"
            sendToESP(messageFromBtnToSend)
        }
        buttonHeadrestUp.setOnClickListener {
            messageFromBtnToSend = if (chairSelect) "Ch232E" else "Ch232e"
            sendToESP(messageFromBtnToSend)
        }
        buttonHeadrestDown.setOnClickListener {
            messageFromBtnToSend =  if (chairSelect) "Ch232F" else "Ch232f"
            sendToESP(messageFromBtnToSend)
        }
        buttonBottomBackward.setOnClickListener {
            messageFromBtnToSend = if (chairSelect) "Ch232B" else "Ch232b"
            sendToESP(messageFromBtnToSend)
        }
        buttonChairFrontward.setOnClickListener {
            messageFromBtnToSend = if (chairSelect) "Ch232C" else "Ch232c"
            sendToESP(messageFromBtnToSend)
        }
        buttonChairBackward.setOnClickListener {
            messageFromBtnToSend = if (chairSelect) "Ch232D" else "Ch232d"
            sendToESP(messageFromBtnToSend)
        }
        buttonReserve.setOnClickListener {
            messageFromBtnToSend = if (chairSelect) "Ch232H" else "Ch232h"
            sendToESP(messageFromBtnToSend)
        }
        buttonG.setOnClickListener {
            messageFromBtnToSend = if (chairSelect) "Ch232G" else "Ch232g"
            sendToESP(messageFromBtnToSend)
        }
        buttonI.setOnClickListener {
            messageFromBtnToSend = if (chairSelect) "Ch232I" else "Ch232i"
            sendToESP(messageFromBtnToSend)
        }
        buttonJ.setOnClickListener {
            messageFromBtnToSend = if (chairSelect) "Ch232J" else "Ch232j"
            sendToESP(messageFromBtnToSend)
        }
        buttonK.setOnClickListener {
            messageFromBtnToSend = if (chairSelect) "Ch232K" else "Ch232k"
            sendToESP(messageFromBtnToSend)
        }
        buttonL.setOnClickListener {
            messageFromBtnToSend = if (chairSelect) "Ch232L" else "Ch232l"
            sendToESP(messageFromBtnToSend)
        }
        buttonM.setOnClickListener {
            messageFromBtnToSend = if (chairSelect) "Ch232M" else "Ch232m"
            sendToESP(messageFromBtnToSend)
        }
        buttonR.setOnClickListener {
            messageFromBtnToSend = if (chairSelect) "Ch232R" else "Ch232r"
            sendToESP(messageFromBtnToSend)
        }


    }
    // ------------------------------------------------------------
    // TCP Client: Connect
    // ------------------------------------------------------------
    private fun connectToESP() {
        Thread {
            try {
                socket = Socket()
                socket!!.connect(InetSocketAddress("192.168.4.1", 4444), 20000)

                out = socket!!.getOutputStream()
                input = socket!!.getInputStream()

                isConnected = true

                runOnUiThread {
                    startReaderThread()
                    connectButton.text = "Disconnect"
                    connectButton.setBackgroundColor(Color.GREEN)
                    Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("TCP", "Connection failed: $e")
                runOnUiThread {
                    Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show()
                }
                isConnected = false
            }
        }.start()
    }

    // ------------------------------------------------------------
    // TCP Client: Disconnect
    // ------------------------------------------------------------
    private fun disconnectESP() {
        Thread {
            try {
                socket?.close()
            } catch (_: Exception) {}

            socket = null
            out = null
            input = null
            isConnected = false
            readerThread?.interrupt()
            readerThread = null
            runOnUiThread {
                connectButton.text = "Connect"
                connectButton.setBackgroundColor(Color.BLUE)
                Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    // ------------------------------------------------------------
    // TCP Client: Send data
    // ------------------------------------------------------------
    private fun sendToESP(msg: String) {
        if (!isConnected || out == null) {
            Toast.makeText(this, "Not connected!", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            try {
                out!!.write(msg.toByteArray())
                out!!.flush()
                Log.d("TCP", "Sent: $msg")
            } catch (e: Exception) {
                Log.e("TCP", "Send failed: $e")
            }
        }.start()
    }
    private fun printToConsole(text: String) {
        runOnUiThread {
            consoleView.append(text + "\n")

            // автоскролл вниз
            scrollConsole.post {
                scrollConsole.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
    }
    private fun startReaderThread() {
        readerThread = Thread {
            val buffer = ByteArray(1024)

            while (isConnected && socket != null) {
                try {
                    val bytes = input?.read(buffer) ?: break
                    if (bytes > 0) {
                        // Проверка нашего пакета (8 байт)
                        if (bytes >= 8 && buffer[0] == 0xAA.toByte()) {

                            val b1 = buffer[1].toInt() and 0xFF
                            val b2 = buffer[2].toInt() and 0xFF
                            val pwm = buffer[3].toInt() and 0xFF
                            val b1_slave = buffer[4].toInt() and 0xFF
                            val b2_slave = buffer[5].toInt() and 0xFF
                            val pwm_slave = buffer[6].toInt() and 0xFF
                            val checksum = buffer[7].toInt() and 0xFF

                            val calcChecksum = b1 xor b2 xor pwm xor b1_slave xor b2_slave xor pwm_slave

                            if (checksum == calcChecksum) {
                                // Протокол валиден — выводим только payload
                                val payloadHex = listOf(b1, b2, pwm,b1_slave,b2_slave,pwm_slave)
                                    .joinToString(" ") { "%02X".format(it) }

                                printToConsole("ESP состояние: $payloadHex")
                                if(chairSelect){
                                    if((b1 and 0x18) == 0x18 && (b1 and 0x80) == 0) { // A: E+D H=0
                                        //A
                                        buttonBottomFrontward.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonBottomFrontward.setBackgroundColor(Color.GRAY)
                                    }
                                    if((b1 and 0x90) == 0x90 && (b1 and 0x08) == 0) { // B: E+H D=0
                                        //B
                                        buttonBottomBackward.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonBottomBackward.setBackgroundColor(Color.GRAY)
                                    }
                                    if((b1 and 0x0C) == 0x0C && (b1 and 0x80) == 0) { // C: C+D H=0
                                        //C
                                        buttonChairFrontward.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonChairFrontward.setBackgroundColor(Color.GRAY)
                                    }
                                    if((b1 and 0x84) == 0x84 && (b1 and 0x08) == 0) { // D: C+H D=0
                                        //D
                                        buttonChairBackward.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonChairBackward.setBackgroundColor(Color.GRAY)
                                    }
                                    if((b1 and 0x48) == 0x48 && (b1 and 0x80) == 0) { // E: G+D H=0
                                        //E
                                        buttonHeadrestUp.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonHeadrestUp.setBackgroundColor(Color.GRAY)
                                    }
                                    if((b1 and 0xC0) == 0xC0 && (b1 and 0x08) == 0) { // F: G+H D=0
                                        //F
                                        buttonHeadrestDown.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonHeadrestDown.setBackgroundColor(Color.GRAY)
                                    }
                                    if((b1 and 0x02) == 0x02 && (b2 and 0x02) == 0x02 && (b1 and 0x01) == 0) { // G: B+J A=0
                                        //G
                                        buttonG.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonG.setBackgroundColor(Color.GRAY)
                                    }
                                    if ((b1 and 0x03) == 0x03 && (b2 and 0x02) == 0) { // H: B+A J=0
                                        //H
                                        buttonReserve.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonReserve.setBackgroundColor(Color.GRAY)
                                    }
                                    if ((b1 and 0x20) == 0x20 && (b2 and 0x02) == 0x02 && (b1 and 0x01) == 0) { // I: F+J A=0
                                        //I
                                        buttonI.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonI.setBackgroundColor(Color.GRAY)
                                    }
                                    if((b1 and 0x21) == 0x21 && (b2 and 0x02) == 0) { // J: F+A J=0
                                        //J
                                        buttonJ.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonJ.setBackgroundColor(Color.GRAY)
                                    }
                                    if ((b1 and 0x01) == 0 && (b2 and 0x03) == 0x03) { // K: I+J A=0
                                        // K
                                        buttonK.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonK.setBackgroundColor(Color.GRAY)
                                    }
                                    if ((b1 and 0x01) == 0x01 && (b2 and 0x01) == 0x01 && (b2 and 0x02) == 0) { // L: I+A J=0
                                        // L
                                        buttonL.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonL.setBackgroundColor(Color.GRAY)
                                    }
                                    if (b1 == 0 && b2 == 0) { // M: all off
                                        // M
                                        buttonM.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonM.setBackgroundColor(Color.RED)
                                    }
                                    if ((b2 and 0x08) != 0) { // R: GPIO12 toggle
                                        // R
                                        buttonR.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonR.setBackgroundColor(Color.GRAY)
                                    }
                                }else{
                                    if((b1_slave and 0x18) == 0x18 && (b1_slave and 0x80) == 0) { // A: E+D H=0
                                        //A
                                        buttonBottomFrontward.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonBottomFrontward.setBackgroundColor(Color.GRAY)
                                    }
                                    if((b1_slave and 0x90) == 0x90 && (b1_slave and 0x08) == 0) { // B: E+H D=0
                                        //B
                                        buttonBottomBackward.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonBottomBackward.setBackgroundColor(Color.GRAY)
                                    }
                                    if((b1_slave and 0x0C) == 0x0C && (b1_slave and 0x80) == 0) { // C: C+D H=0
                                        //C
                                        buttonChairFrontward.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonChairFrontward.setBackgroundColor(Color.GRAY)
                                    }
                                    if((b1_slave and 0x84) == 0x84 && (b1_slave and 0x08) == 0) { // D: C+H D=0
                                        //D
                                        buttonChairBackward.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonChairBackward.setBackgroundColor(Color.GRAY)
                                    }
                                    if((b1_slave and 0x48) == 0x48 && (b1_slave and 0x80) == 0) { // E: G+D H=0
                                        //E
                                        buttonHeadrestUp.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonHeadrestUp.setBackgroundColor(Color.GRAY)
                                    }
                                    if((b1_slave and 0xC0) == 0xC0 && (b1_slave and 0x08) == 0) { // F: G+H D=0
                                        //F
                                        buttonHeadrestDown.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonHeadrestDown.setBackgroundColor(Color.GRAY)
                                    }
                                    if((b1_slave and 0x02) == 0x02 && (b2_slave and 0x02) == 0x02 && (b1_slave and 0x01) == 0) { // G: B+J A=0
                                        //G
                                        buttonG.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonG.setBackgroundColor(Color.GRAY)
                                    }
                                    if ((b1_slave and 0x03) == 0x03 && (b2_slave and 0x02) == 0) { // H: B+A J=0
                                        //H
                                        buttonReserve.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonReserve.setBackgroundColor(Color.GRAY)
                                    }
                                    if ((b1_slave and 0x20) == 0x20 && (b2_slave and 0x02) == 0x02 && (b1_slave and 0x01) == 0) { // I: F+J A=0
                                        //I
                                        buttonI.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonI.setBackgroundColor(Color.GRAY)
                                    }
                                    if((b1_slave and 0x21) == 0x21 && (b2_slave and 0x02) == 0) { // J: F+A J=0
                                        //J
                                        buttonJ.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonJ.setBackgroundColor(Color.GRAY)
                                    }
                                    if ((b1_slave and 0x01) == 0 && (b2_slave and 0x03) == 0x03) { // K: I+J A=0
                                        // K
                                        buttonK.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonK.setBackgroundColor(Color.GRAY)
                                    }
                                    if ((b1_slave and 0x01) == 0x01 && (b2_slave and 0x01) == 0x01 && (b2_slave and 0x02) == 0) { // L: I+A J=0
                                        // L
                                        buttonL.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonL.setBackgroundColor(Color.GRAY)
                                    }
                                    if (b1_slave == 0 && b2_slave == 0) { // M: all off
                                        // M
                                        buttonM.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonM.setBackgroundColor(Color.RED)
                                    }
                                    if ((b2_slave and 0x08) != 0) { // R: GPIO12 toggle
                                        // R
                                        buttonR.setBackgroundColor(Color.GREEN)
                                    }else{
                                        buttonR.setBackgroundColor(Color.GRAY)
                                    }
                                }
                                continue
                            }
                        }

                        // Если пакет невалидный — выводим raw
                        val hexString = buffer
                            .take(bytes)
                            .joinToString(" ") { "%02X".format(it) }

                        printToConsole("ESP не валидный пакет: $hexString")
                    }
                } catch (e: Exception) {
                    printToConsole("Read error: $e")
                    break
                }
            }

            printToConsole("Reader stopped")

            runOnUiThread {
                disconnectESP()
            }
        }

        readerThread!!.start()
    }

}

