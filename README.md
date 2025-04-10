# RFID Tag Location Tracking Library (LocationEventEngine)

This library provides functionality to track the location of RFID tags based on read events received from various RFID readers. It uses a configuration (specified programmatically, not as a separate file) to map reader and antenna combinations to physical locations. The core class is the `LocationEventEngine`.

## Features

*   Location Tracking: Determines the location of RFID tags based on read events and pre-configured location definitions.
*   Event-Driven: Delivers location events via a listener interface when a tag's location is determined.
*   Configurable: Allows customization of window lengths and RSSI thresholds for location determination.
*   Close Range Detection: Supports detection of tags in very close proximity to antennas.
*   Single Read Sensitivity: Allows for immediate location events based on a single read.

## Installation

This library can be integrated into your Java/Kotlin project as follows:

1.  **Clone the Repository:**

    ```bash
    git clone <repository_url>
    ```

    Replace `<repository_url>` with the actual repository URL.

2.  **Include in your project:** Add the library to your project's dependencies. The method to do this depends on your build system.

## Usage

The following steps outline how to use the library:

1.  **Create an instance of the `LocationEventEngine` class:**

    **Java:**

    ```java
    import com.zebra.LocationEventEngine;
    import com.zebra.Location;
    import com.zebra.LocationEventListener;
    import com.zebra.LocationEvent;

    LocationEventEngine engine = new LocationEventEngine();
    ```

    **Kotlin:**

    ```kotlin
    import com.zebra.LocationEventEngine
    import com.zebra.Location
    import com.zebra.LocationEventListener
    import com.zebra.LocationEvent

    val engine = LocationEventEngine()
    ```

2.  **Define Locations:** Create `Location` objects representing the physical locations of your RFID readers and antennas.

    **Java:**

    ```java
    import com.zebra.Location; // Add this import!

    Location location1 = new Location("reader1", 1, "Entrance", false, false); // readerID, antennaID, friendlyName, isCloseRange, isSingleRead
    Location location2 = new Location("reader2", 2, "Exit", true, false); // readerID, antennaID, friendlyName, isCloseRange, isSingleRead
    Location location3 = new Location("reader3", 1, "Assembly Line", false, true); // Single read example

    engine.addLocation(location1);
    engine.addLocation(location2);
    engine.addLocation(location3);
    ```

    **Kotlin:**

    ```kotlin
    import com.zebra.Location // Add this import!

    val location1 = Location("reader1", 1, "Entrance", false, false)
    val location2 = Location("reader2", 2, "Exit", true, false)
    val location3 = Location("reader3", 1, "Assembly Line", false, true) // Single read example

    engine.addLocation(location1)
    engine.addLocation(location2)
    engine.addLocation(location3)
    ```

    | Property      | Description                                                                                                                                  |
    |---------------|----------------------------------------------------------------------------------------------------------------------------------------------|
    | `readerID`    | The unique identifier of the RFID reader.                                                                                                   |
    | `antennaID`   | The ID of the antenna connected to the reader.                                                                                               |
    | `friendlyName`| A human-readable name for the location.                                                                                                    |
    | `isCloseRange`| If `true`, a tag must be very close to the antenna for the location to trigger. The `eventType` in the `LocationEvent` will be set to "CloseRange". |
    | `isSingleRead`| If `true`, a single read from this location is sufficient to trigger a location event. If `false`, multiple reads are needed.              |

3.  **Register a Location Event Listener:** Implement the `LocationEventListener` interface to receive location events.

    **Java:**

    ```java
    import com.zebra.LocationEventListener;
    import com.zebra.LocationEvent;

    engine.registerLocationEventListener(new LocationEventListener() {
        @Override
        public void locationEvent(LocationEvent event) {
            System.out.println("Location Event: Tag " + event.getTagId() +
                                   " at " + event.getReaderName() + ":" + event.getAntennaId() +
                                   " Event Type: " + event.getEventType());
        }
    });
    ```

    **Kotlin:**

    ```kotlin
    import com.zebra.LocationEventListener
    import com.zebra.LocationEvent

    engine.registerLocationEventListener(object : LocationEventListener {
        override fun locationEvent(event: LocationEvent) {
            println("Location Event: Tag ${event.tagId} at ${event.readerName}:${event.antennaId} Event Type: ${event.eventType}")
        }
    })
    ```

4.  **Send Tag Events:** Provide the `LocationEventEngine` with tag data.

    **Java:**

    ```java
    engine.tagEvent("tag123", System.currentTimeMillis(), -70, "reader1", 1, 5); // tagID, timestamp, rssi, readerName, antennaID, readCount
    engine.tagEvent("tag123", System.currentTimeMillis(), -60, "reader2", 2, 10); // Example of the tag moving to reader2
    engine.tagEvent("tag456", System.currentTimeMillis(), -50, "reader3", 1, 1);  // Tag triggered a single read location
    ```

    **Kotlin:**

    ```kotlin
    engine.tagEvent("tag123", System.currentTimeMillis(), -70, "reader1", 1, 5) // tagID, timestamp, rssi, readerName, antennaID, readCount
    engine.tagEvent("tag123", System.currentTimeMillis(), -60, "reader2", 2, 10) // Example of the tag moving to reader2
    engine.tagEvent("tag456", System.currentTimeMillis(), -50, "reader3", 1, 1)  // Tag triggered a single read location
    ```

    **`tagEvent` method: `tagEvent(String tagID, long timestamp, int rssi, String readerName, int antennaID, int readCount)`**

    | Parameter             | Description                                                       |
    |-----------------------|-------------------------------------------------------------------|
    | `tagID`               | The unique identifier of the RFID tag.                            |
    | `timestamp`           | The timestamp of the tag read event (in milliseconds).            |
    | `rssi`                | The Received Signal Strength Indicator (RSSI) value of the read.  |
    | `readerName`          | The ID of the reader that last read the tag (typically reader MAC)|
    | `antennaID`           | The ID of the antenna on the reader that last read the tag.       |
    | `readCount`           | The number of times the tag has been read.                        |

## Configuration

The `LocationEventEngine` provides the following configuration options:

*   `setMinimumWindowLength(int minimumWindowLength)`: Sets the minimum time (in milliseconds) required for a location to be considered valid. Defaults to a reasonable value if not set. A longer time will filter out short, transient reads.

*   `setWindowLength(int windowLength)`: Sets the time window (in milliseconds) used to analyze read events for location determination. A larger window considers a longer history of reads. Defaults to a reasonable value if not set.

*   `setCloseRangeReadRSSI(int closeRangeReadRSSI)`: Sets the RSSI threshold for determining if a tag is in close range. Tags with an RSSI greater than this value are considered to be in close range if `isCloseRange` is true for the antenna. Defaults to a reasonable value if not set.

*   `setLocationChangeResistance(int locationChangeResistance)`: Allows the algorithm to be adjusted to make it more or less sensitive to triggering a location change update.  Valid values are 1-99.  1 being the most sensitive and 99 being the least sensitive. Default: `67`.
  
*   `setLocationChangeResistanceRSSI(int locationChangeResistanceRSSI)`: Similar to the above but pertains specifically to how RSSI values are treated.  Valid values are 1-99 .  1 being the most sensitive and 99 being the least sensitive.  Default: `10`.

Example:

**Java:**

```java
engine.setMinimumWindowLength(500);  // 500 milliseconds
engine.setWindowLength(2000);       // 2 seconds
engine.setCloseRangeReadRSSI(-40);    // -40 dBm
engine.setLocationChangeResistance(67);
engine.setLocationChangeResistanceRSSI(10);
```

**Kotlin:**

```kotlin
engine.minimumWindowLength = 500  // 500 milliseconds
engine.windowLength = 2000       // 2 seconds
engine.closeRangeReadRSSI = -40    // -40 dBm
engine.locationChangeResistance = 67;
engine.locationChangeResistanceRSSI = 10;
```

## Location Event

The `LocationEvent` object is delivered to the registered listener when a location event is detected. It contains the following information:

| Parameter     | Description                                                                            |
|---------------|----------------------------------------------------------------------------------------|
| `tagId`       | The ID of the tag.                                                                     |
| `timestamp`   | The timestamp of the event.                                                            |
| `readerName`  | The name of the reader.                                                                |
| `antennaId`   | The ID of the antenna.                                                                 |
| `friendlyName`| The friendly name associated with the location. (Populated from the `Location` object.) |
| `dwellTime`   | The amount of time the tag has been detected at the location.                         |
| `eventType`   | The type of event. Can be "Normal" (the default) or "CloseRange" if close range is detected.         |

## Example

Here's a complete example demonstrating the usage of the library:

**Java:**

```java
import com.zebra.LocationEventEngine;
import com.zebra.Location;
import com.zebra.LocationEventListener;
import com.zebra.LocationEvent;
import com.zebra.TagEvent;

public class Main {
    public static void main(String[] args) {
        LocationEventEngine engine = new LocationEventEngine();

        Location location1 = new Location("reader1", 1, "Entrance", false, false);
        Location location2 = new Location("reader2", 2, "Exit", true, false);

        engine.addLocation(location1);
        engine.addLocation(location2);

        engine.registerLocationEventListener(new LocationEventListener() {
            @Override
            public void locationEvent(LocationEvent event) {
                System.out.println("Location Event: Tag " + event.getTagId() +
                                       " at " + event.getReaderName() + ":" + event.getAntennaId() +
                                       " Event Type: " + event.getEventType());
            }
        });

        engine.setMinimumWindowLength(500);
        engine.setWindowLength(2000);
        engine.setCloseRangeReadRSSI(-40);

        engine.tagEvent("tag123", System.currentTimeMillis(), -70, "reader1", 1, 5);
        engine.tagEvent("tag123", System.currentTimeMillis(), -60, "reader2", 2, 10);
        engine.tagEvent("tag456", System.currentTimeMillis(), -30, "reader2", 2, 1); // Close range
        try {
            Thread.sleep(3000); // Allow time for processing
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
```

**Kotlin:**

```kotlin
import com.zebra.LocationEventEngine
import com.zebra.Location
import com.zebra.LocationEventListener
import com.zebra.LocationEvent
import com.zebra.TagEvent

fun main() {
    val engine = LocationEventEngine()

    val location1 = Location("reader1", 1, "Entrance", false, false)
    val location2 = Location("reader2", 2, "Exit", true, false)

    engine.addLocation(location1)
    engine.addLocation(location2)

    engine.registerLocationEventListener(object : LocationEventListener {
        override fun locationEvent(event: LocationEvent) {
            println("Location Event: Tag ${event.tagId} at ${event.readerName}:${event.antennaId} Event Type: ${event.eventType}")
        }
    })

    engine.minimumWindowLength = 500
    engine.windowLength = 2000
    engine.closeRangeReadRSSI = -40

    engine.tagEvent("tag123", System.currentTimeMillis(), -70, "reader1", 1, 5)
    engine.tagEvent("tag123", System.currentTimeMillis(), -60, "reader2", 2, 10)
    engine.tagEvent("tag456", System.currentTimeMillis(), -30, "reader2", 2, 1) // Close range

    Thread.sleep(3000)
}
```

## EULA

END USER LICENSE AGREEMENT (UNRESTRICTED SOFTWARE)

IMPORTANT PLEASE READ CAREFULLY: This End User License Agreement ("EULA") is a legal agreement between you (either an individual or a single entity) and ZIH Corp ("Zebra") for software, owned by Zebra and its affiliated companies and its third party suppliers and licensors, that accompanies this EULA. ("Software"). BY USING THE SOFTWARE, YOU ACKNOWLEDGE ACCEPTANCE OF THE TERMS OF THIS EULA. IF YOU DO NOT ACCEPT THESE TERMS, DO NOT USE THE SOFTWARE.

GRANT OF LICENSE. Zebra grants you, End-User Customer, the following rights provided that you comply with all terms and conditions of this EULA: For Software associated with Zebra hardware, Zebra hereby grants you ("Licensee" or "you") a personal, nonexclusive, nontransferable, nonassignable, nonsublicenseable license to use the Software subject to the terms and conditions of this Agreement. Only your employees or subcontractors may use the Software. You shall take all necessary steps to insure that your employees and subcontractors abide by the terms of this Agreement. You shall use the Software only for your internal business purposes, exclusively to support the Zebra hardware, including the right to (i) use, modify, and incorporate all or portions of the sample source code (the "Sample Code"), runtime library files, and/or documentation files that may be included in the unmodified Software into Licensee’s own programs (the "User Programs") to support the Zebra hardware exclusively, provided that no license is granted herein under any patents that may be infringed by Licensee’s modifications, derivative works or by other works in which any portion of the Software may be incorporated; (ii) distribute the Sample Code in object code format only as substantially modified or only as part of the User Programs to support the Zebra hardware exclusively; and (iii) distribute the runtime library files in their original form to support the Zebra hardware exclusively. For a standalone Software application, you may install, use, modify, and incorporate all or portions of any object code, available source code ("Source Code"), runtime library files, and/or documentation files that may be included with the unmodified Software into your own programs (the "User Programs") and distribute the User Programs to third parties. Any use of the Software outside of the conditions set forth herein is strictly prohibited and will be deemed a breach of this Agreement resulting in immediate termination of your License. Zebra will be entitled to all available remedies at law or in equity (including immediate injunctive relief and repossession of all Software unless Licensee is a Federal agency of the United States Government). Certain items of the Software may be subject to open source licenses. The open source license provisions may override some of the terms of this EULA. Zebra makes the applicable open source licenses available to you on a Legal Notices readme file available on your device and/or in System Reference guides or in Command Line Interface (CLI) Reference guides associated with certain Zebra products.

RESERVATION OF RIGHTS AND OWNERSHIP. Zebra reserves all rights not expressly granted to you in this EULA. The Software is protected by copyright and other intellectual property laws and treaties. Zebra or its suppliers own the title, copyright and other intellectual property rights in the Software. The Software is licensed, not sold.

LIMITATIONS ON END USER RIGHTS. You shall not distribute, sublicense, rent, loan, lease, export, re-export, resell, ship or divert or cause to be exported, re-exported, resold, shipped or diverted, directly or indirectly, the unmodified Software under this Agreement. You shall not, and shall not permit others to: (i) combine the Software including any Sample or Source Code, in whole or in part, with any Open Source Software having license terms and obligations that include copyleft obligations and/or intellectual property encumbrances; (ii) remove any proprietary notices, marks, labels, or logos from the Software; (iii) rent or transfer all or some of the Software to any other party without Zebra’s prior written consent; or (iv) utilize any computer software or hardware which is designed to defeat any copy protection device, should the Software be equipped with such a protection device.

MACHINE DATA. “Machine Data” means anonymized usage data collected by devices sold (or licensed) under this Agreement such as battery management (time to empty, standby current, average current), device system time, CPU processing load, free RAM, number of running processes, network information (name, identifier), device identifier, firmware version, hardware version device type, audio volume, LED state, beeper volume, backlight level, key light, odometer count, reboot, reboot cause, total storage and physical memory availability, power cycle count, and device up time. Notwithstanding anything else in this Agreement, all title and ownership rights in and to Machine Data are held by Zebra. In the event, and to the extent you are deemed to have any ownership rights in Machine Data, you hereby grant Zebra a limited, revocable, non-exclusive right and license to use Machine Data.

LOCATION INFORMATION. The Software may enable you to collect location-based data from one or more client devices which may allow you to track the actual location of those client devices. Zebra specifically disclaims any liability for your use or misuse of the location-based data. You agree to pay all reasonable costs and expenses of Zebra arising from or related to third party claims resulting from your use of the location-based data.

SOFTWARE RELEASES. Zebra may periodically release new versions of the Software which will be made available to you.

EXPORT RESTRICTIONS. You acknowledge that the Software is subject to export restrictions of various countries. You agree to comply with all applicable international and national laws that apply to the Software, including all the applicable export restriction laws and regulations.

ASSIGNMENT. You may not assign this Agreement or any of your rights or obligations hereunder (by operation of law or otherwise) without the prior written consent of Zebra. Zebra may assign this Agreement and its rights and obligations without your consent. Subject to the foregoing, this Agreement shall be binding upon and inure to the benefit of the parties to it and their respective legal representatives, successors and permitted assigns.

TERMINATION. This EULA is effective until terminated. Your rights under this License will terminate automatically without notice from Zebra if you fail to comply with any of the terms and conditions of this EULA. Zebra may terminate this Agreement by offering you a superseding Agreement for the Software or for any new release of the Software and conditioning your continued use of the Software or such new release on your acceptance of such superseding Agreement. Upon termination of this EULA, you must cease all use of the Software and destroy all copies, full or partial, of the Software.

DISCLAIMER OF WARRANTY. UNLESS SEPARATELY STATED IN A WRITTEN EXPRESS LIMITED WARRANTY, ALL SOFTWARE PROVIDED BY ZEBRA IS PROVIDED "AS IS" AND ON AN "AS AVAILABLE" BASIS, WITHOUT WARRANTIES OF ANY KIND FROM ZEBRA, EITHER EXPRESS OR IMPLIED. TO THE FULLEST EXTENT POSSIBLE PURSUANT TO APPLICABLE LAW, ZEBRA DISCLAIMS ALL WARRANTIES EXPRESS, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, IMPLIED WARRANTIES OF MERCHANTABILITY, SATISFACTORY QUALITY OR WORKMANLIKE EFFORT, FITNESS FOR A PARTICULAR PURPOSE, RELIABILITY OR AVAILABILITY, ACCURACY, LACK OF VIRUSES, NON INFRINGEMENT OF THIRD PARTY RIGHTS OR OTHER VIOLATION OF RIGHTS. ZEBRA DOES NOT WARRANT THAT THE OPERATION OF THE SOFTWARE WILL BE UNINTERRUPTED OR ERROR FREE. TO THE EXTENT THAT THE SOFTWARE COVERED BY THIS EULA INCLUDES EMULATION LIBRARIES, SUCH EMULATION LIBRARIES DO NOT WORK 100% CORRECTLY OR COVER 100% OF THE FUNCTIONALITY BEING EMULATED, ARE OFFERED "AS IS" AND WITH ALL FAULTS, AND ALL THE DISCLAIMERS AND LIMITATIONS CONTAINED IN THIS PARAGRAPH AND THIS AGREEMENT APPLY TO SUCH EMULATION LIBRARIES. SOME JURISDICTIONS DO NOT ALLOW EXCLUSIONS OR LIMITATIONS OF IMPLIED WARRANTIES, SO THE ABOVE EXCLUSIONS OR LIMITATIONS MAY NOT APPLY TO YOU. NO ADVICE OR INFORMATION, WHETHER ORAL OR WRITTEN, OBTAINED BY YOU FROM ZEBRA OR ITS AFFILIATES SHALL BE DEEMED TO ALTER THIS DISCLAIMER BY ZEBRA OF WARRANTY REGARDING THE SOFTWARE, OR TO CREATE ANY WARRANTY OF ANY SORT FROM ZEBRA.

THIRD-PARTY APPLICATIONS. Certain third party applications may be included with, or downloaded with this Software. Zebra makes no representations whatsoever about any of these applications. Since Zebra has no control over such applications, you acknowledge and agree that Zebra is not responsible for such applications. You expressly acknowledge and agree that use of third party applications is at your sole risk and that the entire risk of unsatisfactory quality, performance, accuracy and effort is with you. You agree that Zebra shall not be responsible or liable, directly or indirectly, for any damage or loss, including but not limited to any damage to or loss of data, caused or alleged to be caused by, or in connection with, use of or reliance on any such third party content, products, or services available on or through any such application. You acknowledge and agree that the use of any third-party application is governed by such third party application provider's Terms of Use, License Agreement, Privacy Policy, or other such agreement and that any information or personal data you provide, whether knowingly or unknowingly, to such third-party application provider, will be subject to such third party application provider's privacy policy, if such a policy exists. ZEBRA DISCLAIMS ANY RESPONSIBILITY FOR ANY DISCLOSURE OF INFORMATION OR ANY OTHER PRACTICES OF ANY THIRD PARTY APPLICATION PROVIDER. ZEBRA EXPRESSLY DISCLAIMS ANY WARRANTY REGARDING WHETHER YOUR PERSONAL INFORMATION IS CAPTURED BY ANY THIRD PARTY APPLICATION PROVIDER OR THE USE TO WHICH SUCH PERSONAL INFORMATION MAY BE PUT BY SUCH THIRD PARTY APPLICATION PROVIDER.

LIMITATION OF LIABILITY. ZEBRA WILL NOT BE LIABLE FOR ANY DAMAGES OF ANY KIND ARISING OUT OF OR RELATING TO THE USE OR THE INABILITY TO USE THE SOFTWARE OR ANY THIRD PARTY APPLICATION, ITS CONTENT OR FUNCTIONALITY, INCLUDING BUT NOT LIMITED TO DAMAGES CAUSED BY OR RELATED TO ERRORS, OMISSIONS, INTERRUPTIONS, DEFECTS, DELAY IN OPERATION OR TRANSMISSION, COMPUTER VIRUS, FAILURE TO CONNECT, NETWORK CHARGES, IN-APP PURCHASES, AND ALL OTHER DIRECT, INDIRECT, SPECIAL, INCIDENTAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ZEBRA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. SOME JURISDICTIONS DO NOT ALLOW THE EXCLUSION OR LIMITATION OF INCIDENTAL OR CONSEQUENTIAL DAMAGES, SO THE ABOVE EXCLUSIONS OR LIMITATIONS MAY NOT APPLY TO YOU. NOTWITHSTANDING THE FOREGOING, ZEBRA’S TOTAL LIABILITY TO YOU FOR ALL LOSSES, DAMAGES, CAUSES OF ACTION, INCLUDING BUT NOT LIMITED TO THOSE BASED ON CONTRACT, TORT, OR OTHERWISE, ARISING OUT OF YOUR USE OF THE SOFTWARE OR THIRD PARTY APPLICATIONS, OR ANY OTHER PROVISION OF THIS EULA, SHALL NOT EXCEED THE FAIR MARKET VALUE OF THE SOFTWARE OR AMOUNT PURCHASER PAID SPECIFICALLY FOR THE SOFTWARE. THE FOREGOING LIMITATIONS, EXCLUSIONS, AND DISCLAIMERS (INCLUDING SECTIONS 10, 11, 12, AND 15) SHALL APPLY TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, EVEN IF ANY REMEDY FAILS ITS ESSENTIAL PURPOSE.

INJUNCTIVE RELIEF. You acknowledge that, in the event you breach any provision of this Agreement, Zebra will not have an adequate remedy in money or damages. Zebra shall therefore be entitled to obtain an injunction against such breach from any court of competent jurisdiction immediately upon request without posting bond. Zebra's right to obtain injunctive relief shall not limit its right to seek further remedies.

MODIFICATION. No modification of this Agreement shall be binding unless it is in writing and is signed by an authorized representative of the party against whom enforcement of the modification is sought.

U.S. GOVERNMENT END USERS RESTRICTED RIGHTS. This provision only applies to U.S. Government end users. The Software is a “commercial item” as that term is defined at 48 C.F.R. Part 2.101, consisting of “commercial computer software” and “computer software documentation” as such terms are defined in 48 C.F.R. Part 252.227-7014(a)(1) and 48 C.F.R. Part 252.227-7014(a)(5), and used in 48 C.F.R. Part 12.212 and 48 C.F.R. Part 227.7202, as applicable. Consistent with 48 C.F.R. Part 12.212, 48 C.F.R. Part 252.227-7015, 48 C.F.R. Part 227.7202-1 through 227.7202-4, 48 C.F.R. Part 52.227-19, and other relevant sections of the Code of Federal Regulations, as applicable, the Software is distributed and licensed to U.S. Government end users (a) only as a commercial item, and (b) with only those rights as are granted to all other end users pursuant to the terms and conditions contained herein.

APPLICABLE LAW. This EULA is governed by the laws of the state of Illinois, without regard to its conflict of law provisions. This EULA shall not be governed by the UN Convention on Contracts for the International Sale of Goods, the application of which is expressly excluded.