Reverse Engineering Sylvac Calipers
===================================

This uses the Android Bluetooth Low Energy sample codebase to read Generic Attribute Profile (GATT)
to transmit arbitrary data between devices. It has customization for the Sylvac calipers.

Specifications
--------------

The S CAL EVO Calipers have three services/profiles. The first service offers 3 characteristics which can be read, the second service offers no characteristics and the third service offers 4 characteristics.

The UUID for these services and characteristics are listed in [SCalEvoBluetoothSpecifications.java](Application/src/main/java/ch/sylvac/calipers/SCalEvoBluetoothSpecifications.java). Currently we dont know what these services are for, nor what the characteristics mean but we have named them so that as we discover what they mean we annotate them.

Buttons
-------

There are two buttons, one for requesting manual data, one for connecting or disconnecting from the calipers.

Changing the mode is done by two steps:

1. Set the characteristic notificaiton to true
* mBluetoothLeService.setCharacteristicNotification(characteristic, true);
1. Set the value of the descriptor to enable notification
* descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);


References
-------

* [S CAL EVO User Guide](http://www.sylvac.ch/download-en/manuals/hand-tools/caliper-s-cal-evo)

* [Sylvac Android App](https://play.google.com/store/apps/details?id=com.sylvac.sylvacbt_smart_demo)

* [Sylvac's website](http://www.sylvac.ch)

The S_CAL_EVO calipers dont appear to follow any existing profiles for mesurement devices:

* [Bluetooth profiles](https://www.bluetooth.com/specifications/adopted-specifications)

* [Bluetooth characteristics](https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicsHome.aspx)

Introduction
------------

This sample can list all available Bluetooth LE devices and provides
an interface to connect, display data and display GATT services and
characteristics supported by any device.

It creates a [Service][1] for managing connection and data communication with a GATT server
hosted on a given Bluetooth LE device.

The Activities communicate with the Service, which in turn interacts with the [Bluetooth LE API][2].

[1]:http://developer.android.com/reference/android/app/Service.html
[2]:https://developer.android.com/reference/android/bluetooth/BluetoothGatt.html

Pre-requisites
--------------

- Android SDK v23
- Android Build Tools v23.0.2
- Android Support Repository

Screenshots
-------------

<img src="screenshots/1-main.png" height="400" alt="Screenshot"/> <img src="screenshots/2-detail.png" height="400" alt="Screenshot"/>

Getting Started
---------------

This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.


License
-------

Copyright 2014 The Android Open Source Project, Inc.

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
