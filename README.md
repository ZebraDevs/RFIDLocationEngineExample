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

Development Tool License Agreement
This Development Tool License Agreement (“Agreement”) includes information about the rights and obligations of the individual or entity entering into this Agreement (“Licensee”). These rights and obligations govern Licensee’s use of any software development kit (SDK), application programming interface (API), protocol, sample code, library, script, or other type of tool provided by Zebra Technologies Corporation or its affiliates (“Zebra”) that accompanies or references this Agreement (“Development Tool”).

The Development Tool or associated software or hardware may be protected by patent(s) in the U.S. and elsewhere. Information regarding Zebra’s patents is available at www.ip.zebra.com.

1            Acceptance

This Agreement is a legal contract made between Zebra and Licensee that defines the terms and conditions under which Zebra is willing to provide Licensee certain permissions associated with the Development Tool. By ordering, subscribing to, installing, executing, running, downloading, copying, modifying, distributing, or otherwise using the Development Tool, Licensee (i) accepts and agrees to be bound by this Agreement, (ii) represents that Licensee had read and understands this Agreement, and (iii) confirms that Licensee is lawfully able to enter into this Agreement.

 

2            Term of this Agreement

2.1       This Agreement becomes effective on the date Licensee accepts this Agreement and ends upon termination hereof in accordance with this Section 2 (“Term”).

2.2       This Agreement will terminate upon Licensee’s failure to cure a breach or violation hereof within thirty (30) days of Zebra providing Licensee notice of the breach or violation.

2.3       If Licensee’s use of the Development Tool is pursuant to an order, a subscription, or other type of commercial agreement, this Agreement will terminate upon expiration or termination of that other agreement.

2.4       Upon termination of this Agreement, Licensee shall immediately cease using the Development Tool in any way, including the acts permitted under Section 3.3, and any Zebra Content (as defined below) obtained by Licensee in connection with the Development Tool.

2.5       Sections 4, 5, 7, 10-13, and 17 will survive the termination of this Agreement, along with any other provision expected to survive termination by its nature or intent.

 

3            License and Ownership

3.1       The Development Tool is designed or configured to (i) facilitate or enable interoperability with or access to Zebra hardware, Zebra software, Zebra platforms, and/or Zebra computing services (“Zebra Products”) and/or (ii) support development or customization of software, libraries, or APIs that interact with, support, or function with Zebra Products.

3.2       “Licensee Application” means software, applications, code, programs, libraries, APIs, or any other type of machine-readable instructions Licensee creates, alters, or modifies using the Development Tool.

3.3       Subject to Licensee’s compliance with Section 4 this Agreement, Zebra hereby grants Licensee a limited, revocable, non-exclusive, non-sublicensable license to, during the Term:

3.3.1           reproduce the Development Tool internally in object code format for the purpose of integrating the Development Tool with or otherwise developing a Licensee Application that is configured to interface with, support, or function with a Zebra Product;

3.3.2           if Zebra provides Licensee source code with or as part of the Development Tool, modify the source code to create a derivative work thereof only for use in a Licensee Application that is configured to interface with, support, or function with a Zebra Product; and

3.3.3           externally distribute the Development Tool only in object code form and only as (i) integrated with a Licensee Application in accordance with Section 3.3.1 or (ii) modified for use in a Licensee Application in accordance with Section 3.3.2.

3.4       Section 3.3.3 does not permit Licensee to distribute (i) source code of the Development Tool or (ii) the Development Tool in its unmodified form (i.e., as a standalone package without being integrated into or modified for a Licensee Application).

3.5       Licensee will retain its right, title, and interest in any Licensee Application developed by Licensee using the Development Tool. Licensee shall defend and hold harmless Zebra from and against any claim, suit, or proceeding alleging that a Licensee Application or a distribution thereof violates a person’s privacy rights or infringes or misappropriates a third party’s intellectual property rights.

3.6       The Development Tool is licensed; not sold. Zebra reserves all right, title, and interest not expressly granted in this Agreement. Nothing in this Agreement provides or grants Licensee any ownership or license rights to Zebra Products, including software or hardware that the Development Tool is designed to support. Any such right must be obtained through a separate agreement or purchase of a Zebra Product.

3.7       No license is granted herein under any Zebra patents that are infringed by Licensee’s integrations made pursuant to Section 3.3.1, modifications made pursuant to Section 3.3.2, or distributions made pursuant to Section 3.3.3.

 

4            Restrictions

4.1       Except as expressly permitted under Section 3.3, Licensee shall not or permit another to (i) reproduce, modify, distribute, publicly display, publicly perform, or create derivative works of the Development Tool; (ii) disassemble, decompile, reverse-engineer, or attempt to discover or derive source code of the Development Tool, except and only to the extent that such activity is expressly permitted by applicable law notwithstanding this restriction; (iii) rent, sell, lease, lend, sublicense, provide commercial hosting services involving the Development Tool, or in any other way allow third parties to exploit the Development Tool; (iv) modify, circumvent, deactivate, degrade, or thwart any software-based or hardware-based protection mechanism Zebra has in place to safeguard the Development Tool; or (v) embed any virus, Trojan horse, worm, backdoor, shutdown mechanism, malicious code, sniffer, bot, drop dead mechanism, or spyware or other software, code, or program that is likely to or is intended to have an adverse impact on the performance of, disable, corrupt, cause damage to, cause or facilitate unauthorized access to, or deny authorized access to any software, hardware, network, services, systems, data, or Zebra Products.  

4.2       The rights granted to Licensee hereunder cannot be used or otherwise applied to anyone other than Licensee.

4.3       Licensee may not assign this Agreement or any rights or obligations hereunder, by operation of law or otherwise, without prior written consent from Zebra. Zebra may assign this Agreement and its rights and obligations without Licensee’s consent. Subject to the foregoing, this Agreement will be binding upon and inure to the benefit of the parties to it and their respective legal representatives, successors, and permitted assigns.

4.4       If Licensee’s use of the Development Tool includes collection of Sensitive Data, Personal Health Information, or Biometric Data (as those terms are defined by applicable law) associated with end users, Licensee shall limit use of the Development Tool (and any Licensee Application that utilizes the Development Tool) to individuals from whom Licensee has obtained all legally required consents and to whom Licensee has provided all legally required notifications with respect to such data collection (“Authorized Users”).

 

5            Permissions

5.1       “Content” means image data, images, graphics, text, templates, formats, forms, digital certificates or other types of user-identifying packages, plug-ins, widgets, audio, video, and audiovisual data.

5.2       “Input” means data provided to Zebra by Licensee or end users of a Licensee Application, including Content, measurement values, readings, sensor outputs, calculation results, and instructions.

5.3       “Feedback” means ideas, suggestions, comments, or reviews Licensee provides to Zebra in relation to the Development Tool.

5.4       To the extent permission is required, Licensee hereby grants Zebra permission to use Input and Content and access all software incorporated into Zebra hardware as necessary for the Development Tool to perform functions associated with the Input or Content. Licensee shall defend and hold harmless Zebra from and against any claim, suit, or proceeding alleging that Zebra’s use of or access to Licensee’s Content or Input violates a person’s privacy rights or infringes or misappropriates a third party’s intellectual property right.

5.5       Licensee agrees that Zebra is free to use Feedback to improve its products and services.

5.6       Where applicable, Licensee hereby agrees to waive and not enforce any “moral rights” or equivalent rights in Feedback, Input, or Content provided to Zebra in connection with the Development Tool or a Licensee Application.

 

6            Updates, Support, and Fixes

6.1       Nothing in this Agreement entitles Licensee to new releases, updates, maintenance, or technical support for the Development Tool. Such entitlements must be obtained from Zebra through a separate purchase order, support agreement, or service contract, as available.

6.2       If Zebra, at its discretion, makes updates, fixes, or patches to the Development Tool available during the Term without providing superseding terms, this Agreement applies to such updates, fixes, and patches.

6.3       Provided the functionality and features of the Development Tool remain substantially similar thereafter, Zebra may automatically update the Development Tool without requiring Licensee’s acceptance. Zebra shall make reasonable efforts to provide Licensee notice of automatic updates made to the Development Tool, although such notice is not required under this Agreement.

 

7            Data

7.1       Zebra’s Privacy Statement (located at www.zebra.com/privacy), as amended from time to time, is hereby incorporated by reference into this Agreement. If Licensee or Authorized Users submit personal data to Zebra in connection with the Development Tool, the ways in which Zebra collects and uses that data are regulated by Zebra’s Privacy Statement in accordance with applicable law. All such data provided to Zebra shall also be processed in accordance with applicable Development Tool documentation. Licensee agrees not to provide Sensitive Data, Payment Card Information (PCI), or Personal Health Information (PHI) (as those terms are defined under applicable law) to Zebra via the Development Tool or a Licensee Application. If Licensee or Authorized Users provide email account data to Zebra in connection with the Development Tool or a Licensee Application, Licensee agrees that Zebra may retain the email account data according to Zebra’s Privacy Statement and contact such accounts for purposes of notification, support, or updates associated with the Development Tool or Zebra Products related thereto.

7.2       Licensee acknowledges and agrees that Zebra may, as permitted by law, (i) process personal data for purposes associated with use of the Development Tool, (ii) create aggregated and/or pseudonymized data records (i.e., data that cannot be used to identify a person without the use of additional information that is kept separately) using Licensee data or personal data, and (iii) use the aggregated or pseudonymized data records to improve the Development Tool, develop new software or services, understand industry trends, create and publish white papers, reports, or databases summarizing the foregoing, investigate and help address and/or prevent actual or potential unlawful activity, and generally for any legitimate purpose related to Zebra’s business.

7.3       “Machine Data” means usage data or status information collected by the Development Tool or hardware that interfaces with the Development Tool, such as information related to a computing device running the Development Tool. Example machine data includes remaining usage time, network information (e.g., name or identifier), wireless signal strength, device identifier, software version, hardware version, device type, metadata associated with the operation of the Development Tool, LED state, reboot cause, storage and memory availability or usage, power cycle count, and device up time. To the extent any Machine Data includes personal data, Zebra shall process such Machine Data in accordance with Zebra’s Privacy Statement. The Development Tool may provide Machine Data to Zebra. All title and ownership rights in and to Machine Data are held by Zebra. In the event and to the extent Licensee is deemed to have any ownership rights in Machine Data, Licensee hereby grants Zebra a perpetual, irrevocable, fully paid, worldwide license to use, reproduce, and make derivative works of Machine Data.

 

8            Modifications of this Agreement

Modification or amendment of this Agreement must be made through written agreement by an authorized representative of each party. Written agreement may be satisfied by Zebra’s offer of a superseding agreement for use of the Development Tool and Licensee’s acceptance thereof in accordance with Section 1.

 

9            Third-Party Content

9.1          Access to and use of third-party Content or services is subject to terms and conditions provided by the third party and may be protected by the third-party’s intellectual property rights.

9.2          Third-party resources linked or made available via the Development Tool are not considered part of the Development Tool, and Zebra reserves the right to, at its sole discretion, disable integrations of third-party Content or services or compatibility of the Development Tool therewith. Nothing in this Agreement is a license, permission, or assignment of any rights in or to such third-party Content or services.

9.3          Licensee acknowledges that if the Development Tool requires access to non-Zebra hardware, non-Zebra software, or non-Zebra Content to perform a function or provide a feature and Licensee denies such permission, the corresponding function or feature will not be available or execute properly.

 

10         DISCLAIMERS OF WARRANTY

EXCEPT AS OTHERWISE PROVIDED IN A SEPARATE AGREEMENT OR SERVICE CONTRACT LICENSEE ENTERS INTO WITH ZEBRA: (A) THE DEVELOPMENT TOOL AND ANY THIRD-PARTY CONTENT OR SERVICE ASSOCIATED WITH THE DEVELOPMENT TOOL ARE PROVIDED "AS IS" AND ON AN "AS AVAILABLE" BASIS, AND (B) TO THE FULLEST EXTENT POSSIBLE PURSUANT TO APPLICABLE LAW WITH RESPECT TO THE DEVELOPMENT TOOL AND ANY THIRD-PARTY CONTENT OR SERVICE ASSOCIATED WITH THE DEVELOPMENT TOOL, ZEBRA MAKES NO REPRESENTATIONS AND DISCLAIMS ALL WARRANTIES, WHETHER EXPRESS, IMPLIED, STATUTORY, OR OTHERWISE, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF MERCHANTABILITY, SATISFACTORY QUALITY OR WORKMANLIKE EFFORT, FITNESS FOR A PARTICULAR PURPOSE, RELIABILITY OR AVAILABILITY, AND NON-INFRINGEMENT. ZEBRA DOES NOT WARRANT THAT THE OPERATION OR AVAILABILITY OF THE DEVELOPMENT TOOL WILL BE UNINTERRUPTED OR ERROR FREE. NO ADVICE OR INFORMATION, WHETHER ORAL OR WRITTEN, OBTAINED FROM ZEBRA WILL BE DEEMED TO ALTER THIS DISCLAIMER OF WARRANTY.

 

11         LIMITATIONS OF LIABILITY

11.1      TO THE EXTENT ALLOWABLE BY APPLICABLE LAW, ZEBRA WILL NOT BE RESPONSIBLE OR LIABLE TO LICENSEE UNDER THIS AGREEMENT FOR:

11.1.1       ANY INDIRECT, SPECIAL, INCIDENTAL, EXEMPLARY, PUNITIVE, OR CONSEQUENTIAL DAMAGES, EVEN IF ZEBRA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES AND REGARDLESS OF THE FORM OF ACTION OR THEORY OF RECOVERY;

11.1.2       DAMAGES OF ANY KIND ARISING OUT OF OR RELATING TO AN INABILITY TO USE OR ACCESS THE DEVELOPMENT TOOL DUE TO CONNECTIVITY OR DATA TRANSMISSION ISSUES NOT CAUSED BY ZEBRA OR NOT UNDER ZEBRA’S CONTROL, INCLUDING NETWORK INTERRUPTIONS, TRANSMISSION LATENCIES, OR DEFECTS IN SYSTEMS LICENSEE USES TO CONNECT TO THE INTERNET OR OTHER COMMUNICATION NETWORKS; OR

11.1.3       DIRECTLY OR INDIRECTLY, ANY DAMAGE OR LOSS CAUSED BY OR IN CONNECTION WITH A LICENSEE APPLICATION OR LICENSEE’S USE OF THIRD-PARTY CONTENT OR SERVICES IN ASSOCIATION WITH THE DEVELOPMENT TOOL, INCLUDING BUT NOT LIMITED TO, ANY DAMAGE TO OR LOSS OF DATA, AS LICENSEE ACKNOWLEDGES AND AGREES THAT LICENSEE’S EXPLOITATION OF LICENSEE APPLICATIONS AND USE OF THIRD-PARTY CONTENT OR SERVICES IN ASSOCIATION WITH THE DEVELOPMENT TOOL ARE AT LICENSEE’S SOLE RISK.

11.2      THE DEVELOPMENT TOOL MAY ENABLE COLLECTION OF TRACKING OR BIOMETRIC DATA USABLE TO TRACK A DEVICE OR IDENTIFY A PERSON, RESPECTIVELY. LICENSEE HEREBY AGREES TO ASSUME ALL RISK AND LIABILITY ASSOCIATED WITH LICENSEE’S COLLECTION, USE, OR MISUSE OF TRACKING OR BIOMETRIC DATA VIA THE DEVELOPMENT TOOL OR A LICENSEE APPLICATION. THE RIGHTS GRANTED TO LICENSEE UNDER SECTION 3 OF THIS AGREEMENT ARE CONDITIONAL ON LICENSEE COMPLYING WITH ALL APPLICABLE LAWS REGARDING COLLECTION OR USE OF TRACKING OR BIOMETRIC DATA WITH RESPECT TO AUTHORIZED USERS, INCLUDING LAWS REQUIRING LICENSEE TO OBTAIN CONSENT OR PROVIDE NOTICE FOR THE COLLECTION OR USE OF SUCH DATA. LICENSEE AGREES TO DEFEND AND HOLD HARMLESS ZEBRA FROM AND AGAINST ANY CLAIM, SUIT, OR PROCEEDING ARISING FROM LICENSEE’S COLLECTION, USE, OR MISUSE OF TRACKING DATA OR BIOMETRIC DATA IMPLEMENTED VIA THE DEVELOPMENT TOOL OR A LICENSEE APPLICATION, INCLUDING CLAIMS BROUGHT BY LICENSEE’S EMPLOYEES OR END USERS.

11.3      NOTWITHSTANDING THE FOREGOING, ZEBRA’S TOTAL AGGREGATE LIABILITY TO LICENSEE FOR ALL LOSSES, DAMAGES, AND CAUSES OF ACTION, INCLUDING BUT NOT LIMITED TO THOSE BASED ON CONTRACT, TORT, OR OTHERWISE, ARISING OUT OF LICENSEE’S USE OF THE DEVELOPMENT TOOL, INCLUDING ANY LOSS OF DATA, WILL NOT EXCEED ONE THOUSAND DOLLARS ($1,000).

11.4      THE LIMITATIONS, EXCLUSIONS, AND DISCLAIMERS HEREIN WILL APPLY TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, EVEN IF ANY REMEDY FAILS ITS ESSENTIAL PURPOSE.

 

12         Governing Law

This Agreement is governed by the laws of the State of Illinois, without regard to its conflict of law provisions. This Agreement will not be governed by the UN Convention on Contracts for the International Sale of Goods, the application of which is expressly excluded. Licensee hereby submits itself and its property in any legal action or proceeding relating to this Agreement or for recognition and enforcement of any judgment in respect thereof to the exclusive general jurisdiction of the courts of the State of Illinois or to the United States North District Court of Illinois and to the respective appellate courts thereof in connection with any appeal therefrom.

 

13         Handling of Disputes

13.1      Licensee acknowledges that, in the event Licensee breaches any provision of this Agreement, Zebra may not have an adequate remedy in money or damages. Zebra will therefore be entitled to seek an injunction against such breach from any court of competent jurisdiction immediately upon request without posting bond. Zebra’s right to seek injunctive relief will not limit its right to seek further remedies.

13.2      If any term of this Agreement is to any extent illegal, otherwise invalid, or incapable of being enforced, such term will be excluded to the extent of such invalidity or unenforceability, all other terms hereof will remain in full force and effect, and, to the extent permitted and possible, the invalid or unenforceable term will be deemed replaced by a term that is valid and enforceable and that comes closest to expressing the intention of such invalid or unenforceable term. 

13.3      The parties agree that Licensee and Zebra are the sole parties to this Agreement, and Licensee hereby agrees to not seek remedies under this Agreement against Zebra’s authorized distributors or resellers with respect to the Development Tool.

 

14         Open-Source Software

The Development Tool may be subject to one or more open-source licenses. The open-source license provisions may override some terms of this Agreement. Zebra makes the applicable open-source licenses available on a legal notices website, readme file, system reference guides, or command line interface (CLI) reference guides associated with certain Zebra Products.

 

15         Government End User Restricted Rights

The U.S. Government has certain restricted rights in software under the Federal Acquisition Regulations and Defense Federal Acquisition Regulations Supplement, as applicable. If Licensee is a U.S. Government agency or contractor, Licensee should comply with the above regulations, including obtaining any necessary licenses or approval from relevant regulatory bodies before exporting or re-exporting the Development Tool to certain countries or individuals/entities on sanctioned lists. Consistent with the above regulations and other relevant sections of the Code of Federal Regulations, the Development Tool is distributed and licensed to U.S. Government end users (a) only as a “commercial item” consisting of “commercial computer software” and “computer software documentation” as such terms are defined in the above regulations, and (b) with only those rights as are granted to all other end users pursuant to the terms and conditions contained herein.

 

16         Export Control

By ordering, subscribing to, installing, executing, running, downloading, or otherwise using the Development Tool and any accompanying documentation, Licensee agrees that it is not located in a country that is subject to export embargoes or that has been designated as a "terrorist supporting" country. Licensee acknowledges that the Development Tool and any accompanying documentation may be subject to export control laws and regulations of the United States, European Economic Area, United Kingdom and any other local/country export control laws and regulations, which may be amended from time to time. Licensee agrees to comply with all applicable international and national laws that apply to the Development Tool and any accompanying documentation, including all applicable import, export and compliance laws and regulations, and to obtain any necessary licenses or approvals when applicable. Failure to comply with the above conditions may result in legal action by Zebra or relevant authorities. Licensee confirms to not download or otherwise obtain the Development Tool for “military end-use”, and/or “military intelligence end-use” as described on the U.S. Munitions List (22 C.F.R. §121) and in 15 C.F.R. §744 of the U.S. Export Administration Regulations.

 

17         Confidentiality

17.1   “Confidential Information” is defined as any non-public information, data, software, or object related to the Development Tool or this Agreement that is provided or conveyed to Licensee by Zebra in oral, written, graphic, machine recognizable, and/or physical form. Source code of the Development Tool and the structure and organization thereof are Confidential Information.

17.2   During the Term and a period of five (5) years thereafter – except with respect to trade secrets for which obligations of this paragraph apply during the Term and will continue beyond the Term until the information no longer qualifies as a trade secret under applicable law by means other than Licensee’s unauthorized disclosure thereof – Licensee shall (i) restrict disclosure of Confidential Information to only those who are bound by this Agreement and must be directly involved with the Confidential Information for the purposes contemplated by this Agreement; (ii) use the same degree of care as for your own information of like importance, but at least use reasonable care, in safeguarding against unauthorized use or disclosure of Confidential Information; (iii) promptly notify Zebra in writing upon discovery of any unauthorized use or disclosure of the Confidential Information and take reasonable steps to regain possession of the Confidential Information and prevent further unauthorized actions or other breach of this Agreement; and (iv) only use the Confidential Information for the purposes contemplated by this Agreement.

17.3   The obligations set forth in Section 17.2 will not apply to any portion of the Confidential Information that (i) is or becomes available to the public through means other than Licensee’s unauthorized disclosure; (ii) is explicitly approved for release by written authorization of Zebra; (iii) is lawfully obtained from a third party without breach of a confidentiality obligation; (iv) is known to Licensee prior to such disclosure; or (v) is independently developed by Licensee without the use of any of Zebra’s Confidential Information or any breach of this Agreement.

17.4   If Licensee is required to disclose Confidential Information pursuant to applicable law, statute, regulation, or court order, Licensee shall give Zebra prompt written notice of the requirement and a reasonable opportunity to object to such disclosure and seek a protective order or other appropriate remedy. If, in the absence of a protective order, Licensee determines upon the advice of counsel that Licensee is required to disclose such Confidential Information, Licensee may disclose only Confidential Information specifically required and only to the extent so compelled.