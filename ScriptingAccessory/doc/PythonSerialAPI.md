ScriptingAccessory
==================
* When user connect an accessory to android device, device install script to android device.
* After installing, android device execute the device with utilizing accessory hardware.

# format
>extra = {
>    "command"   : "digitalWrite",
>    "port"      : "2",
>    "value"     : "high",
>}
>makeIntent('com.tapioka.android.usbserial.IO', None, None, extra).result

# intent extras
## digitalWrite
* command : "digitalWrite"
* port : 0~13 <TBD>
* value : "high" or "low"
* return : N/A
## digitalRead
* command : "digitalRead"
* port : 0~13 <TBD>
* value : N/A
* return : "high" or "low"
## analogWrite
* command : "analogWrite"
* port : 0~13 <TBD>
* value : 0~255
* return : N/A
## analogRead
* command : "analogRead"
* port : 0~13 <TBD>
* value : N/A
* return : 0~255


