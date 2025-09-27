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
package ua.pp.teaching.android.obd.statics

import ua.pp.teaching.android.obd.models.DTC
import ua.pp.teaching.android.obd.models.DTCS
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Class to hold all the static methods necessary for the OBD library
 * that pertain to DTCs
 *
 * @author Brad Barnhill
 */
@Suppress("unused")
object DTCUtils {
    val dtcList: List<DTC>
        @Throws(IOException::class)
        get() = Json.decodeFromString<DTCS>(FileUtils.readFromFile("dtc-codes.json")).dtcs
}
