package ua.pp.teaching.android.obd.statics

import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import ua.pp.teaching.android.obd.commands.OBDCommand
import ua.pp.teaching.android.obd.enums.ObdModes
import ua.pp.teaching.android.obd.enums.ObdProtocols
import ua.pp.teaching.android.obd.models.PID
import ua.pp.teaching.android.obd.statics.PIDUtils.getPid
import ua.pp.teaching.android.obd.statics.PersistentStorage.clearAll

@Suppress("unused")
class ObdInitSequence {
    companion object {
        private const val TAG = "ObdInitSequence"
        private const val ECU_RESPONSE_TIMEOUT = 25
        private const val MODE_AT = "AT"

        /**
         * Will run the connection sequence to setup the connection with the ELM327 device given a
         * connected bluetooth socket with the device
         * @param socket Bluetooth socket that is already connected to the ELM327 device
         * @return True if connection sequence succeeded and the test PID was retrieved successfully
         * from the device, false otherwise.
         */
        fun run(socket: BluetoothSocket): Boolean {
            return run(socket.inputStream, socket.outputStream)
        }

        /**
         * Will run the connection sequence to setup the connection with the ELM327 device given
         * input and output streams (compatible with any socket implementation)
         * @param inputStream InputStream from the OBD adapter
         * @param outputStream OutputStream to the OBD adapter
         * @return True if connection sequence succeeded and the test PID was retrieved successfully
         * from the device, false otherwise.
         */
        fun run(inputStream: InputStream, outputStream: OutputStream): Boolean {
            var connected: Boolean
            try {
                Log.d(TAG, "Streams connected, starting initialization sequence")
                clearAll()
                var initPid: PID = getPid(ObdModes.MODE_01, "00") ?: return false

                // set defaults
                initPid.mode = MODE_AT
                initPid.PID = "D"
                var cmd = OBDCommand(initPid).setIgnoreResult(true).run(inputStream, outputStream)
                Log.d(
                        TAG,
                        "Set defaults sent (" +
                                initPid.mode +
                                " " +
                                initPid.PID +
                                ") Received: " +
                                cmd.rawResult
                )

                // resets the ELM327
                initPid.mode = MODE_AT
                initPid.PID = "Z"
                cmd = OBDCommand(initPid).setIgnoreResult(true).run(inputStream, outputStream)
                Log.d(
                        TAG,
                        "Reset command sent (" +
                                initPid.mode +
                                " " +
                                initPid.PID +
                                ") Received: " +
                                cmd.rawResult
                )

                // extended responses off
                initPid.mode = MODE_AT
                initPid.PID = "E0"
                cmd = OBDCommand(initPid).setIgnoreResult(true).run(inputStream, outputStream)
                Log.d(
                        TAG,
                        "Extended Responses Off (" +
                                initPid.mode +
                                " " +
                                initPid.PID +
                                ") Received: " +
                                cmd.rawResult
                )

                // line feeds off
                initPid.mode = MODE_AT
                initPid.PID = "L0"
                cmd = OBDCommand(initPid).setIgnoreResult(true).run(inputStream, outputStream)
                Log.d(
                        TAG,
                        "Turn Off Line Feeds (" +
                                initPid.mode +
                                " " +
                                initPid.PID +
                                ") Received: " +
                                cmd.rawResult
                )

                // printing of spaces off
                initPid.mode = MODE_AT
                initPid.PID = "S0"
                cmd = OBDCommand(initPid).setIgnoreResult(true).run(inputStream, outputStream)
                Log.d(
                        TAG,
                        "Printing Spaces Off (" +
                                initPid.mode +
                                " " +
                                initPid.PID +
                                ") Received: " +
                                cmd.rawResult
                )

                // headers off
                initPid.mode = MODE_AT
                initPid.PID = "H0"
                cmd = OBDCommand(initPid).setIgnoreResult(true).run(inputStream, outputStream)
                Log.d(
                        TAG,
                        "Headers Off (" +
                                initPid.mode +
                                " " +
                                initPid.PID +
                                ") Received: " +
                                cmd.rawResult
                )

                // set protocol
                initPid.mode = "$MODE_AT SP"
                initPid.PID = ObdProtocols.AUTO.value.toString()
                cmd = OBDCommand(initPid).setIgnoreResult(true).run(inputStream, outputStream)
                Log.d(
                        TAG,
                        "Select Protocol (" +
                                initPid.mode +
                                " " +
                                initPid.PID +
                                ") Received: " +
                                cmd.rawResult
                )

                // set timeout for response from the ECU
                initPid.mode = "$MODE_AT ST"
                initPid.PID = Integer.toHexString(0xFF and ECU_RESPONSE_TIMEOUT)
                cmd = OBDCommand(initPid).setIgnoreResult(true).run(inputStream, outputStream)
                Log.d(
                        TAG,
                        "Set timeout (" +
                                initPid.mode +
                                " " +
                                initPid.PID +
                                ") Received: " +
                                cmd.rawResult
                )

                // Test connection with PID 00 (supported PIDs)
                initPid = getPid(ObdModes.MODE_01, "00") ?: return false
                Log.d(
                        TAG,
                        "Mode 1 PID 00: " +
                                OBDCommand(initPid).run(inputStream, outputStream).formattedResult
                )
                connected = initPid.calculatedResultString != null
            } catch (e: IOException) {
                Log.e(TAG, "IOException on init commands: " + e.message)
                connected = false
            } catch (e: InterruptedException) {
                Log.e(TAG, "InterruptedException on init commands: " + e.message)
                connected = false
            }
            return connected
        }
    }
}
