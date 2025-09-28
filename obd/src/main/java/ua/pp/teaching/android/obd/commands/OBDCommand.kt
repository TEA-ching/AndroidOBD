/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
@file:Suppress("unused")

package ua.pp.teaching.android.obd.commands

import android.util.Log
import com.ezylang.evalex.Expression
import com.ezylang.evalex.config.ExpressionConfiguration
import ua.pp.teaching.android.obd.models.PID
import ua.pp.teaching.android.obd.statics.Translations


/**
 * Generic class to form an OBD command for communication, also includes the parsing of the result.
 *
 * @author Brad Barnhill
 */
class OBDCommand(pid: PID) : BaseObdCommand(pid.mode.trim { it <= ' ' } + if (pid.PID.trim { it <= ' ' }.isEmpty()) "" else " " + pid.PID.trim { it <= ' ' }, pid) {
    private var mMetricUnits = true

    override val formattedResult: String
        get() = mPid.calculatedResult.toString() + " " + if (mMetricUnits || mPid.imperialFormula == null) mPid.units else mPid.imperialUnits

    override val name: String
        get() = mPid.description

    val callDuration: Long
        get() = mPid.retrievalTime

    companion object {
        const val A = "A"
        const val B = "B"
        const val C = "C"
        const val D = "D"

        val expressionConfig: ExpressionConfiguration by lazy {
            ExpressionConfiguration.builder()
                .decimalPlacesRounding(5)
                .stripTrailingZeros(true)
                .build()
        }
    }

    init {
        pid.retrievalTime = 0
    }

    /**
     * Set if the result of the request will be ignored
     * @param ignoreResult [Boolean] which is whether the result should be ignored when its returned.
     * @return Current [OBDCommand]
     */
    fun setIgnoreResult(ignoreResult: Boolean): OBDCommand {
        mIgnoreResult = ignoreResult
        return this
    }

    /**
     * Set to metric or imperial units
     * @param metric True if to return metric units, false for imperial units.
     * @return Current [OBDCommand]
     */
    fun setUnitType(metric: Boolean): OBDCommand {
        mMetricUnits = metric
        return this
    }

    override fun performCalculations() {
        if (NODATA != rawResult) {
            val exprText = if (mMetricUnits || mPid.imperialFormula == null) mPid.formula else mPid.imperialFormula

            val numBytes: Byte = try {
                java.lang.Byte.parseByte(mPid.bytes)
            } catch (nfex: NumberFormatException) {
                0
            }

            mPid.data.clear()
            mPid.data.addAll(buffer)

            if (Translations.handleSpecialPidEnumerations(mPid, mPid.data)) {
                return
            }

            if (exprText == null || !exprText.contains(A) && !exprText.contains(B) && !exprText.contains(C) && !exprText.contains(D) || numBytes > 4 || numBytes <= 0 || mPid.data.size <= 2) {
                mPid.calculatedResultString = mPid.data.toString()
                return
            }

            //TODO: first two bytes show what command the data is for, verify this is the command returning that is expected

            val expression = Expression(exprText, expressionConfig)

            if (mPid.data.size > 2)
                expression.with(A, mPid.data[2])

            if (mPid.data.size > 3)
                expression.with(B, mPid.data[3])

            if (mPid.data.size > 4)
                expression.with(C, mPid.data[4])

            if (mPid.data.size > 5)
                expression.with(D, mPid.data[5])

            try {
                mPid.calculatedResult = expression.evaluate().numberValue.toFloat()
                mPid.calculatedResultString = mPid.calculatedResult.toString()
            } catch (nfex: NumberFormatException) {
                Log.e(OBDCommand::class.java.simpleName, "[Expression:" + expression + "] [mode:" + mPid.mode + "] [Pid:" + mPid.PID + "] [formula:" + mPid.formula
                        + "] [bytes:" + mPid.bytes + "] [BytesReturned:" + mPid.data.size + "]", nfex)
            } catch (genEx: Exception) {
                Log.e(OBDCommand::class.java.simpleName, "[Expression:" + expression + "] [mode:" + mPid.mode + "] [Pid:" + mPid.PID + "] [formula:" + mPid.formula + "] [bytes:" + mPid.bytes + "] [BytesReturned:" + mPid.data.size + "]", genEx)
            }
        }
    }
}
