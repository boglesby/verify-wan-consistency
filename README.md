# Verify Region Data Consistency Between WAN Sites
## Description

This project provides a service that verifies the consistency of Region data between two WAN sites.

The **WanVerificationService**:

- is instantiated with a Region name, a Pool connected to site 1 and a Pool connected site 2
- creates a proxy Region connected to site 1 and gets the keySet for the Region on that site
- creates a proxy Region connected to site 2 and gets the keySet for the Region on that site
- compares the keySets for equality
- iterates site 1 keySet and for each key compares the value in site 1 to the value in site 2
- if the keySets aren't equal, iterates site 2 keySet and for each key compares the value in site 1 to the value in site 2 (if the keySets are equal, this step is not necessary)
- logs the result

The keySet comparison is done using equals.

The value comparison is done using a **ValueComparator** which defines one method called *compare*. The default implementation of *compare* uses equals, so if the value class implements equals, the default implementation will work fine. But, if the value class does not implement equals, then a non-default **ValueComparator** will be required that knows how to properly compare the two values.

This project also provides a Spring Boot Client that executes the following scenarios to show the behavior of the **WanVerificationService**:

- **Scenario 1**

	- Load entries into the site 1 Region which are replicated to site 2

	In this scenario, the keys and values are equal.
- **Scenario 2**
	
	- Load entries into the site 1 Region which are replicated to site 2
	- Load entries into the site 2 Region which are not replicated to site 1

	In this scenario, all the keys are equal, but only some of the values are.
- **Scenario 3**

	- Load entries into the site 2 Region which are not replicated to site 1

	In this scenario, neither the keys nor the values are equal.
- **Scenario 4**

	- Pause GatewaySender in site 1
	- Load entries into the site 1 Region which are not replicated to site 2

	In this scenario, neither the keys nor the values are equal.
- **Scenario 5**

	- Pause GatewaySender in site 1
	- Load entries into the site 1 Region which are not replicated to site 2
	- Load other entries into the site 2 Region which are not replicated to site 1
	
	In this scenario, neither the keys nor the values are equal.
	
## Configuration
This project configures 2 WAN sites.

Site 1 starts a locator and 2 servers, each with:

- a GatewaySender
- a Region named Trade connected to the GatewaySender

Site 2 starts a locator and 2 servers, each with:

- a GatewayReceiver
- a Region named Trade

Site 2 does not define the GatewaySender so that different scenarios can be tested.

## Caveats
The **WanVerificationService** should be run when the GatewaySender queues are empty. Any events in the queues are ignored, so if the queues contain events, the results might incorrectly show differences which may not exist.

The **WanVerificationService** calls Region get on each key in each site. If the Region defines a CacheLoader, it will be invoked if the value is null for that key.

## Initialization
Modify the **GEODE** environment variable in the *setenv.sh* script to point to a Geode installation directory.

## Build
Build the Spring Boot Client Application and **WanVerificationService** using gradle like:

```
./gradlew clean jar bootJar
```
## Start and Configure the Locator and Servers in Each WAN Site
Start and configure the locator and servers in each WAN site using the *startsites.sh* script like:

```
./startsites.sh 
1. Executing - set variable --name=APP_RESULT_VIEWER --value=any

Value for variable APP_RESULT_VIEWER is now: any.

2. Executing - start locator --name=locator-ln --port=10331 --locators=localhost[10331] --mcast-port=0 --J=-Dgemfire.remote-locators=localhost[10332] --J=-Dgemfire.distributed-system-id=1 --J=-Dgemfire.jmx-manager-start=true --J=-Dgemfire.jmx-manager-http-port=8081 --J=-Dgemfire.jmx-manager-port=1091

...........
Locator in <working-directory>/locator-ln on xxx.xxx.x.x[10331] as locator-ln is currently online.
Process ID: 96566
Uptime: 13 seconds
Geode Version: 1.14.0-build.0
Java Version: 1.8.0_121
Log File: <working-directory>/locator-ln/locator-ln.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

Successfully connected to: JMX Manager [host=xxx.xxx.x.x, port=1091]

Cluster configuration service is up and running.

3. Executing - start server --name=server-ln-1 --locators=localhost[10331] --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

......
Server in <working-directory>/server-ln-1 on xxx.xxx.x.x[58598] as server-ln-1 is currently online.
Process ID: 96598
Uptime: 6 seconds
Geode Version: 1.14.0-build.0
Java Version: 1.8.0_121
Log File: <working-directory>/server-ln-1/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

4. Executing - start server --name=server-ln-2 --locators=localhost[10331] --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

.....
Server in <working-directory>/server-ln-2 on xxx.xxx.x.x[58621] as server-ln-2 is currently online.
Process ID: 96613
Uptime: 5 seconds
Geode Version: 1.14.0-build.0
Java Version: 1.8.0_121
Log File: <working-directory>/server-ln-2/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

5. Executing - create gateway-sender --id="ny" --parallel="true" --remote-distributed-system-id="2"

  Member    | Status | Message
----------- | ------ | -------------------------------------------
server-ln-1 | OK     | GatewaySender "ny" created on "server-ln-1"
server-ln-2 | OK     | GatewaySender "ny" created on "server-ln-2"

Cluster configuration for group 'cluster' is updated.

6. Executing - sleep --time=5


7. Executing - create region --name=Trade --type=PARTITION_REDUNDANT --gateway-sender-id="ny"

  Member    | Status | Message
----------- | ------ | ----------------------------------------
server-ln-1 | OK     | Region "/Trade" created on "server-ln-1"
server-ln-2 | OK     | Region "/Trade" created on "server-ln-2"

Cluster configuration for group 'cluster' is updated.

8. Executing - disconnect

Disconnecting from: xxx.xxx.x.x[1091]
Disconnected from : xxx.xxx.x.x[1091]

9. Executing - start locator --name=locator-ny --port=10332 --locators=localhost[10332] --mcast-port=0 --J=-Dgemfire.remote-locators=localhost[10331] --J=-Dgemfire.distributed-system-id=2 --J=-Dgemfire.jmx-manager-start=true --J=-Dgemfire.jmx-manager-http-port=8082 --J=-Dgemfire.jmx-manager-port=1092

............
Locator in <working-directory>/locator-ny on xxx.xxx.x.x[10332] as locator-ny is currently online.
Process ID: 96624
Uptime: 14 seconds
Geode Version: 1.14.0-build.0
Java Version: 1.8.0_121
Log File: <working-directory>/locator-ny/locator-ny.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

Successfully connected to: JMX Manager [host=xxx.xxx.x.x, port=1092]

Cluster configuration service is up and running.

10. Executing - start server --name=server-ny-1 --locators=localhost[10332] --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

......
Server in <working-directory>/server-ny-1 on xxx.xxx.x.x[58695] as server-ny-1 is currently online.
Process ID: 96649
Uptime: 5 seconds
Geode Version: 1.14.0-build.0
Java Version: 1.8.0_121
Log File: <working-directory>/server-ny-1/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

11. Executing - start server --name=server-ny-2 --locators=localhost[10332] --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false

......
Server in <working-directory>/server-ny-2 on xxx.xxx.x.x[58718] as server-ny-2 is currently online.
Process ID: 96668
Uptime: 5 seconds
Geode Version: 1.14.0-build.0
Java Version: 1.8.0_121
Log File: <working-directory>/server-ny-2/cacheserver.log
JVM Arguments: <jvm-arguments>
Class-Path: <classpath>

12. Executing - create gateway-receiver

  Member    | Status | Message
----------- | ------ | ----------------------------------------------------------------------------------
server-ny-1 | OK     | GatewayReceiver created on member "server-ny-1" and will listen on the port "5321"
server-ny-2 | OK     | GatewayReceiver created on member "server-ny-2" and will listen on the port "5086"

Cluster configuration for group 'cluster' is updated.

13. Executing - create region --name=Trade --type=PARTITION_REDUNDANT

  Member    | Status | Message
----------- | ------ | ----------------------------------------
server-ny-1 | OK     | Region "/Trade" created on "server-ny-1"
server-ny-2 | OK     | Region "/Trade" created on "server-ny-2"

Cluster configuration for group 'cluster' is updated.

14. Executing - disconnect

Disconnecting from: xxx.xxx.x.x[1092]
Disconnected from : xxx.xxx.x.x[1092]

************************* Execution Summary ***********************
Script file: startsites.gfsh

Command-1 : set variable --name=APP_RESULT_VIEWER --value=any
Status    : PASSED

Command-2 : start locator --name=locator-ln --port=10331 --locators=localhost[10331] --mcast-port=0 --J=-Dgemfire.remote-locators=localhost[10332] --J=-Dgemfire.distributed-system-id=1 --J=-Dgemfire.jmx-manager-start=true --J=-Dgemfire.jmx-manager-http-port=8081 --J=-Dgemfire.jmx-manager-port=1091
Status    : PASSED

Command-3 : start server --name=server-ln-1 --locators=localhost[10331] --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false
Status    : PASSED

Command-4 : start server --name=server-ln-2 --locators=localhost[10331] --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false
Status    : PASSED

Command-5 : create gateway-sender --id="ny" --parallel="true" --remote-distributed-system-id="2"
Status    : PASSED

Command-6 : sleep --time=5
Status    : PASSED

Command-7 : create region --name=Trade --type=PARTITION_REDUNDANT --gateway-sender-id="ny"
Status    : PASSED

Command-8 : disconnect
Status    : PASSED

Command-9 : start locator --name=locator-ny --port=10332 --locators=localhost[10332] --mcast-port=0 --J=-Dgemfire.remote-locators=localhost[10331] --J=-Dgemfire.distributed-system-id=2 --J=-Dgemfire.jmx-manager-start=true --J=-Dgemfire.jmx-manager-http-port=8082 --J=-Dgemfire.jmx-manager-port=1092
Status    : PASSED

Command-10 : start server --name=server-ny-1 --locators=localhost[10332] --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false
Status     : PASSED

Command-11 : start server --name=server-ny-2 --locators=localhost[10332] --server-port=0 --statistic-archive-file=cacheserver.gfs --J=-Dgemfire.log-file=cacheserver.log --J=-Dgemfire.conserve-sockets=false
Status     : PASSED

Command-12 : create gateway-receiver
Status     : PASSED

Command-13 : create region --name=Trade --type=PARTITION_REDUNDANT
Status     : PASSED

Command-14 : disconnect
Status     : PASSED
```
## Scenario 1
This scenario:

- loads entries into the site 1 Region which are replicated to site 2.

In this scenario, the keys and values are equal.

### Load Entries
Load entries using the *runclient.sh* script like below.

The parameters are:

- operation (run-scenario-1)
- region name (Trade)

```
./runclient.sh run-scenario-1 Trade

> Task :bootRun

2020-06-09 12:12:13.414  INFO 89828 --- [           main] example.client.Client           : Starting Client on ...
...
2020-06-09 12:12:15.167  INFO 89828 --- [           main] example.client.Client           : Started Client in 2.061 seconds (JVM running for 2.39)
2020-06-09 12:12:15.168  INFO 89828 --- [           main] example.client.Client           : Putting into region=Trade, site=site1; numEntries=20
2020-06-09 12:12:15.254  INFO 89828 --- [           main] example.client.Client           :    key=0, value=Trade(id=0, cusip=TM, shares=19, price=783.36)
2020-06-09 12:12:15.255  INFO 89828 --- [           main] example.client.Client           :    key=1, value=Trade(id=1, cusip=MCD, shares=45, price=717.35)
2020-06-09 12:12:15.256  INFO 89828 --- [           main] example.client.Client           :    key=2, value=Trade(id=2, cusip=HON, shares=93, price=55.51)
2020-06-09 12:12:15.257  INFO 89828 --- [           main] example.client.Client           :    key=3, value=Trade(id=3, cusip=TXN, shares=11, price=878.72)
2020-06-09 12:12:15.258  INFO 89828 --- [           main] example.client.Client           :    key=4, value=Trade(id=4, cusip=NKE, shares=85, price=965.89)
2020-06-09 12:12:15.259  INFO 89828 --- [           main] example.client.Client           :    key=5, value=Trade(id=5, cusip=VZ, shares=30, price=389.39)
2020-06-09 12:12:15.260  INFO 89828 --- [           main] example.client.Client           :    key=6, value=Trade(id=6, cusip=V, shares=0, price=409.84)
2020-06-09 12:12:15.261  INFO 89828 --- [           main] example.client.Client           :    key=7, value=Trade(id=7, cusip=PEP, shares=39, price=488.13)
2020-06-09 12:12:15.262  INFO 89828 --- [           main] example.client.Client           :    key=8, value=Trade(id=8, cusip=VZ, shares=20, price=785.77)
2020-06-09 12:12:15.263  INFO 89828 --- [           main] example.client.Client           :    key=9, value=Trade(id=9, cusip=GE, shares=40, price=509.12)
2020-06-09 12:12:15.264  INFO 89828 --- [           main] example.client.Client           :    key=10, value=Trade(id=10, cusip=PFE, shares=79, price=928.04)
2020-06-09 12:12:15.265  INFO 89828 --- [           main] example.client.Client           :    key=11, value=Trade(id=11, cusip=COST, shares=63, price=713.75)
2020-06-09 12:12:15.266  INFO 89828 --- [           main] example.client.Client           :    key=12, value=Trade(id=12, cusip=BMY, shares=78, price=353.65)
2020-06-09 12:12:15.267  INFO 89828 --- [           main] example.client.Client           :    key=13, value=Trade(id=13, cusip=C, shares=38, price=448.33)
2020-06-09 12:12:15.268  INFO 89828 --- [           main] example.client.Client           :    key=14, value=Trade(id=14, cusip=COST, shares=25, price=317.02)
2020-06-09 12:12:15.269  INFO 89828 --- [           main] example.client.Client           :    key=15, value=Trade(id=15, cusip=TM, shares=30, price=607.22)
2020-06-09 12:12:15.270  INFO 89828 --- [           main] example.client.Client           :    key=16, value=Trade(id=16, cusip=INTC, shares=97, price=47.41)
2020-06-09 12:12:15.271  INFO 89828 --- [           main] example.client.Client           :    key=17, value=Trade(id=17, cusip=ADBE, shares=61, price=801.09)
2020-06-09 12:12:15.272  INFO 89828 --- [           main] example.client.Client           :    key=18, value=Trade(id=18, cusip=UPS, shares=85, price=8.82)
2020-06-09 12:12:15.273  INFO 89828 --- [           main] example.client.Client           :    key=19, value=Trade(id=19, cusip=HD, shares=42, price=553.18)
```
### Verify Entries
Verify the entries in both sites using the *runclient.sh* script like below.

The parameters are:

- operation (verify-region)
- region name (Trade)

```
./runclient.sh verify-region Trade

> Task :bootRun

2020-06-09 12:20:23.411  INFO 90472 --- [           main] example.client.Client                    : Starting Client on ...
...
2020-06-09 12:20:25.119  INFO 90472 --- [           main] example.client.Client                    : Started Client in 2.03 seconds (JVM running for 2.385)
2020-06-09 12:20:25.251  INFO 90472 --- [           main] e.c.verifier.WanVerificationService      : 
Verifying entries for region=Trade

==============
Comparing keys
==============
All 20 keys are equal

============================================
Comparing values in site 1 to those in site 2
=============================================
All values in site 1 are equal to those in site 2
```
### Clear Region
Clear the region in both sites using the `runclient.sh` script like below.

```
./runclient.sh clear-region Trade

> Task :bootRun

2020-06-09 12:11:07.148  INFO 89745 --- [           main] example.client.Client           : Starting Client on ...
...
2020-06-09 12:11:09.087  INFO 89745 --- [           main] example.client.Client           : Started Client in 2.276 seconds (JVM running for 2.646)
2020-06-09 12:11:09.088  INFO 89745 --- [           main] example.client.Client           : Destroying from region=Trade, site=site1; numEntries=20
2020-06-09 12:11:09.120  INFO 89745 --- [           main] example.client.Client           :    key=0
2020-06-09 12:11:09.121  INFO 89745 --- [           main] example.client.Client           :    key=1
2020-06-09 12:11:09.122  INFO 89745 --- [           main] example.client.Client           :    key=2
2020-06-09 12:11:09.123  INFO 89745 --- [           main] example.client.Client           :    key=3
2020-06-09 12:11:09.124  INFO 89745 --- [           main] example.client.Client           :    key=4
2020-06-09 12:11:09.124  INFO 89745 --- [           main] example.client.Client           :    key=5
2020-06-09 12:11:09.125  INFO 89745 --- [           main] example.client.Client           :    key=6
2020-06-09 12:11:09.126  INFO 89745 --- [           main] example.client.Client           :    key=7
2020-06-09 12:11:09.127  INFO 89745 --- [           main] example.client.Client           :    key=8
2020-06-09 12:11:09.127  INFO 89745 --- [           main] example.client.Client           :    key=9
2020-06-09 12:11:09.128  INFO 89745 --- [           main] example.client.Client           :    key=10
2020-06-09 12:11:09.129  INFO 89745 --- [           main] example.client.Client           :    key=11
2020-06-09 12:11:09.129  INFO 89745 --- [           main] example.client.Client           :    key=12
2020-06-09 12:11:09.130  INFO 89745 --- [           main] example.client.Client           :    key=13
2020-06-09 12:11:09.131  INFO 89745 --- [           main] example.client.Client           :    key=14
2020-06-09 12:11:09.132  INFO 89745 --- [           main] example.client.Client           :    key=15
2020-06-09 12:11:09.133  INFO 89745 --- [           main] example.client.Client           :    key=16
2020-06-09 12:11:09.133  INFO 89745 --- [           main] example.client.Client           :    key=17
2020-06-09 12:11:09.134  INFO 89745 --- [           main] example.client.Client           :    key=18
2020-06-09 12:11:09.135  INFO 89745 --- [           main] example.client.Client           :    key=19
```
## Scenario 2
This scenario:

- loads entries into the site 1 Region which are replicated to site 2
- loads entries into the site 2 Region which are not replicated to site 1

In this scenario, all the keys are equal, but only some of the values are.

### Load Entries
Load entries using the *runclient.sh* script like below.

The parameters are:

- operation (run-scenario-2)
- region name (Trade)

```
./runclient.sh run-scenario-2 Trade

> Task :bootRun

2020-06-09 11:48:09.103  INFO 88266 --- [           main] example.client.Client           : Starting Client on ...
...
2020-06-09 11:48:10.814  INFO 88266 --- [           main] example.client.Client           : Started Client in 2.024 seconds (JVM running for 2.361)
2020-06-09 11:48:10.815  INFO 88266 --- [           main] example.client.Client           : Putting into region=Trade, site=site1; numEntries=20
2020-06-09 11:48:10.890  INFO 88266 --- [           main] example.client.Client           :    key=0, value=Trade(id=0, cusip=MRK, shares=12, price=202.65)
2020-06-09 11:48:10.892  INFO 88266 --- [           main] example.client.Client           :    key=1, value=Trade(id=1, cusip=UNH, shares=51, price=995.72)
2020-06-09 11:48:10.893  INFO 88266 --- [           main] example.client.Client           :    key=2, value=Trade(id=2, cusip=CMCSA, shares=83, price=684.68)
2020-06-09 11:48:10.894  INFO 88266 --- [           main] example.client.Client           :    key=3, value=Trade(id=3, cusip=V, shares=42, price=346.58)
2020-06-09 11:48:10.895  INFO 88266 --- [           main] example.client.Client           :    key=4, value=Trade(id=4, cusip=AXP, shares=53, price=244.85)
2020-06-09 11:48:10.896  INFO 88266 --- [           main] example.client.Client           :    key=5, value=Trade(id=5, cusip=NFLX, shares=20, price=537.40)
2020-06-09 11:48:10.897  INFO 88266 --- [           main] example.client.Client           :    key=6, value=Trade(id=6, cusip=C, shares=25, price=490.07)
2020-06-09 11:48:10.898  INFO 88266 --- [           main] example.client.Client           :    key=7, value=Trade(id=7, cusip=NFLX, shares=5, price=866.30)
2020-06-09 11:48:10.899  INFO 88266 --- [           main] example.client.Client           :    key=8, value=Trade(id=8, cusip=HON, shares=12, price=154.95)
2020-06-09 11:48:10.900  INFO 88266 --- [           main] example.client.Client           :    key=9, value=Trade(id=9, cusip=MSFT, shares=83, price=481.65)
2020-06-09 11:48:10.901  INFO 88266 --- [           main] example.client.Client           :    key=10, value=Trade(id=10, cusip=ORCL, shares=51, price=75.53)
2020-06-09 11:48:10.903  INFO 88266 --- [           main] example.client.Client           :    key=11, value=Trade(id=11, cusip=V, shares=58, price=763.22)
2020-06-09 11:48:10.904  INFO 88266 --- [           main] example.client.Client           :    key=12, value=Trade(id=12, cusip=BAC, shares=87, price=588.86)
2020-06-09 11:48:10.905  INFO 88266 --- [           main] example.client.Client           :    key=13, value=Trade(id=13, cusip=MA, shares=42, price=555.58)
2020-06-09 11:48:10.906  INFO 88266 --- [           main] example.client.Client           :    key=14, value=Trade(id=14, cusip=JPM, shares=81, price=19.72))
2020-06-09 11:48:10.907  INFO 88266 --- [           main] example.client.Client           :    key=15, value=Trade(id=15, cusip=UNP, shares=83, price=510.32)
2020-06-09 11:48:10.908  INFO 88266 --- [           main] example.client.Client           :    key=16, value=Trade(id=16, cusip=FB, shares=29, price=513.78)
2020-06-09 11:48:10.909  INFO 88266 --- [           main] example.client.Client           :    key=17, value=Trade(id=17, cusip=COST, shares=36, price=65.92)
2020-06-09 11:48:10.910  INFO 88266 --- [           main] example.client.Client           :    key=18, value=Trade(id=18, cusip=HD, shares=6, price=275.66)
2020-06-09 11:48:10.911  INFO 88266 --- [           main] example.client.Client           :    key=19, value=Trade(id=19, cusip=WFC, shares=54, price=872.55)
2020-06-09 11:48:10.912  INFO 88266 --- [           main] example.client.Client           : Putting into region=Trade, site=site2; numEntries=10
2020-06-09 11:48:10.913  INFO 88266 --- [           main] example.client.Client           :    key=0, value=Trade(id=0, cusip=PFE, shares=11, price=682.45)
2020-06-09 11:48:10.914  INFO 88266 --- [           main] example.client.Client           :    key=1, value=Trade(id=1, cusip=BAC, shares=69, price=882.05)
2020-06-09 11:48:10.915  INFO 88266 --- [           main] example.client.Client           :    key=2, value=Trade(id=2, cusip=TM, shares=98, price=143.51)
2020-06-09 11:48:10.916  INFO 88266 --- [           main] example.client.Client           :    key=3, value=Trade(id=3, cusip=GOOGL, shares=93, price=467.43)
2020-06-09 11:48:10.917  INFO 88266 --- [           main] example.client.Client           :    key=4, value=Trade(id=4, cusip=PG, shares=66, price=270.92)
2020-06-09 11:48:10.917  INFO 88266 --- [           main] example.client.Client           :    key=5, value=Trade(id=5, cusip=AVGO, shares=7, price=239.55)
2020-06-09 11:48:10.918  INFO 88266 --- [           main] example.client.Client           :    key=6, value=Trade(id=6, cusip=PG, shares=52, price=566.23)
2020-06-09 11:48:10.919  INFO 88266 --- [           main] example.client.Client           :    key=7, value=Trade(id=7, cusip=MMM, shares=42, price=484.09)
2020-06-09 11:48:10.920  INFO 88266 --- [           main] example.client.Client           :    key=8, value=Trade(id=8, cusip=NFLX, shares=94, price=439.81)
2020-06-09 11:48:10.920  INFO 88266 --- [           main] example.client.Client           :    key=9, value=Trade(id=9, cusip=KO, shares=74, price=568.00)
```
### Verify Entries
Verify the entries in both sites using the *runclient.sh* script like below.

The parameters are:

- operation (verify-region)
- region name (Trade)

```
./runclient.sh verify-region Trade

> Task :bootRun

2020-06-09 11:51:42.879  INFO 88520 --- [           main] example.client.Client                    : Starting Client on ...
...
2020-06-09 11:51:44.608  INFO 88520 --- [           main] example.client.Client                    : Started Client in 2.042 seconds (JVM running for 2.382)
2020-06-09 11:51:44.744  INFO 88520 --- [           main] e.c.verifier.WanVerificationService      : 
Verifying entries for region=Trade

==============
Comparing keys
==============
All 20 keys are equal

============================================
Comparing values in site 1 to those in site 2
=============================================
Values are not equal for key=0; site1Value=Trade(id=0, cusip=MRK, shares=12, price=202.65); site2Value=Trade(id=0, cusip=PFE, shares=11, price=682.45)
Values are not equal for key=1; site1Value=Trade(id=1, cusip=UNH, shares=51, price=995.72); site2Value=Trade(id=1, cusip=BAC, shares=69, price=882.05)
Values are not equal for key=2; site1Value=Trade(id=2, cusip=CMCSA, shares=83, price=684.68); site2Value=Trade(id=2, cusip=TM, shares=98, price=143.51)
Values are not equal for key=3; site1Value=Trade(id=3, cusip=V, shares=42, price=346.58); site2Value=Trade(id=3, cusip=GOOGL, shares=93, price=467.43)
Values are not equal for key=4; site1Value=Trade(id=4, cusip=AXP, shares=53, price=244.85); site2Value=Trade(id=4, cusip=PG, shares=66, price=270.92)
Values are not equal for key=5; site1Value=Trade(id=5, cusip=NFLX, shares=20, price=537.40); site2Value=Trade(id=5, cusip=AVGO, shares=7, price=239.55)
Values are not equal for key=6; site1Value=Trade(id=6, cusip=C, shares=25, price=490.07); site2Value=Trade(id=6, cusip=PG, shares=52, price=566.23)
Values are not equal for key=7; site1Value=Trade(id=7, cusip=NFLX, shares=5, price=866.30); site2Value=Trade(id=7, cusip=MMM, shares=42, price=484.09)
Values are not equal for key=8; site1Value=Trade(id=8, cusip=HON, shares=12, price=154.95); site2Value=Trade(id=8, cusip=NFLX, shares=94, price=439.81)
Values are not equal for key=9; site1Value=Trade(id=9, cusip=MSFT, shares=83, price=481.65); site2Value=Trade(id=9, cusip=KO, shares=74, price=568.00)
```
### Clear Region
Clear the region in both sites using the `runclient.sh` script like in Scenario 1.
## Scenario 3
This scenario:

- loads entries into the site 2 Region which are not replicated to site 1

In this scenario, neither the keys nor the values are equal.
### Load Entries
Load entries using the *runclient.sh* script like below.

The parameters are:

- operation (run-scenario-3)
- region name (Trade)

```
./runclient.sh run-scenario-3 Trade

> Task :bootRun

2020-06-09 12:26:51.151  INFO 91029 --- [           main] example.client.Client           : Starting Client on ...
...
2020-06-09 12:26:52.826  INFO 91029 --- [           main] example.client.Client           : Started Client in 1.988 seconds (JVM running for 2.333)
2020-06-09 12:26:52.827  INFO 91029 --- [           main] example.client.Client           : Putting into region=Trade, site=site2; numEntries=20
2020-06-09 12:26:52.900  INFO 91029 --- [           main] example.client.Client           :    key=0, value=Trade(id=0, cusip=KO, shares=20, price=897.78)
2020-06-09 12:26:52.901  INFO 91029 --- [           main] example.client.Client           :    key=1, value=Trade(id=1, cusip=KO, shares=42, price=150.23)
2020-06-09 12:26:52.902  INFO 91029 --- [           main] example.client.Client           :    key=2, value=Trade(id=2, cusip=HON, shares=83, price=227.96)
2020-06-09 12:26:52.903  INFO 91029 --- [           main] example.client.Client           :    key=3, value=Trade(id=3, cusip=NKE, shares=39, price=341.00)
2020-06-09 12:26:52.904  INFO 91029 --- [           main] example.client.Client           :    key=4, value=Trade(id=4, cusip=BUD, shares=29, price=942.74)
2020-06-09 12:26:52.904  INFO 91029 --- [           main] example.client.Client           :    key=5, value=Trade(id=5, cusip=TXN, shares=55, price=382.56)
2020-06-09 12:26:52.905  INFO 91029 --- [           main] example.client.Client           :    key=6, value=Trade(id=6, cusip=UNH, shares=9, price=388.62)
2020-06-09 12:26:52.906  INFO 91029 --- [           main] example.client.Client           :    key=7, value=Trade(id=7, cusip=PG, shares=43, price=490.53)
2020-06-09 12:26:52.907  INFO 91029 --- [           main] example.client.Client           :    key=8, value=Trade(id=8, cusip=NFLX, shares=55, price=914.49)
2020-06-09 12:26:52.907  INFO 91029 --- [           main] example.client.Client           :    key=9, value=Trade(id=9, cusip=TXN, shares=42, price=289.76)
2020-06-09 12:26:52.909  INFO 91029 --- [           main] example.client.Client           :    key=10, value=Trade(id=10, cusip=BUD, shares=58, price=176.86)
2020-06-09 12:26:52.910  INFO 91029 --- [           main] example.client.Client           :    key=11, value=Trade(id=11, cusip=HD, shares=54, price=140.87)
2020-06-09 12:26:52.911  INFO 91029 --- [           main] example.client.Client           :    key=12, value=Trade(id=12, cusip=PEP, shares=44, price=529.08)
2020-06-09 12:26:52.912  INFO 91029 --- [           main] example.client.Client           :    key=13, value=Trade(id=13, cusip=HON, shares=74, price=449.58)
2020-06-09 12:26:52.913  INFO 91029 --- [           main] example.client.Client           :    key=14, value=Trade(id=14, cusip=PEP, shares=51, price=967.85)
2020-06-09 12:26:52.914  INFO 91029 --- [           main] example.client.Client           :    key=15, value=Trade(id=15, cusip=KO, shares=67, price=47.36)
2020-06-09 12:26:52.915  INFO 91029 --- [           main] example.client.Client           :    key=16, value=Trade(id=16, cusip=HD, shares=51, price=443.05)
2020-06-09 12:26:52.916  INFO 91029 --- [           main] example.client.Client           :    key=17, value=Trade(id=17, cusip=JNJ, shares=12, price=95.95)
2020-06-09 12:26:52.917  INFO 91029 --- [           main] example.client.Client           :    key=18, value=Trade(id=18, cusip=LLY, shares=5, price=471.82)
2020-06-09 12:26:52.918  INFO 91029 --- [           main] example.client.Client           :    key=19, value=Trade(id=19, cusip=C, shares=67, price=432.76)
```
### Verify Entries
Verify the entries in both sites using the *runclient.sh* script like below.

The parameters are:

- operation (verify-region)
- region name (Trade)

```
./runclient.sh verify-region Trade

> Task :bootRun

2020-06-09 12:28:24.625  INFO 91145 --- [           main] example.client.Client                    : Starting Client on boglesbymac.local with PID 91145 (/Users/boglesby/Dev/Tests/spring-boot/wan-verification-client/build/classes/java/main started by boglesby in /Users/boglesby/Dev/Tests/spring-boot/wan-verification-client)
...
2020-06-09 12:28:26.348  INFO 91145 --- [           main] example.client.Client                    : Started Client in 2.041 seconds (JVM running for 2.377)
2020-06-09 12:28:26.475  INFO 91145 --- [           main] e.c.verifier.WanVerificationService      : 
Verifying entries for region=Trade

==============
Comparing keys
==============
All keys are not equal. Site 1 contains 0 keys. Site 2 contains 20 keys.
Site 1 contains these 0 keys not found in site 2: []
Site 2 contains these 20 keys not found in site 1: [11, 12, 13, 14, 15, 16, 17, 18, 19, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

============================================
Comparing values in site 1 to those in site 2
=============================================
All values in site 1 are equal to those in site 2

============================================
Comparing values in site 2 to those in site 1
=============================================
Values are not equal for key=11; site1Value=null; site2Value=Trade(id=11, cusip=HD, shares=54, price=140.87)
Values are not equal for key=12; site1Value=null; site2Value=Trade(id=12, cusip=PEP, shares=44, price=529.08)
Values are not equal for key=13; site1Value=null; site2Value=Trade(id=13, cusip=HON, shares=74, price=449.58)
Values are not equal for key=14; site1Value=null; site2Value=Trade(id=14, cusip=PEP, shares=51, price=967.85)
Values are not equal for key=15; site1Value=null; site2Value=Trade(id=15, cusip=KO, shares=67, price=47.36)
Values are not equal for key=16; site1Value=null; site2Value=Trade(id=16, cusip=HD, shares=51, price=443.05)
Values are not equal for key=17; site1Value=null; site2Value=Trade(id=17, cusip=JNJ, shares=12, price=95.95)
Values are not equal for key=18; site1Value=null; site2Value=Trade(id=18, cusip=LLY, shares=5, price=471.82)
Values are not equal for key=19; site1Value=null; site2Value=Trade(id=19, cusip=C, shares=67, price=432.76)
Values are not equal for key=0; site1Value=null; site2Value=Trade(id=0, cusip=KO, shares=20, price=897.78)
Values are not equal for key=1; site1Value=null; site2Value=Trade(id=1, cusip=KO, shares=42, price=150.23)
Values are not equal for key=2; site1Value=null; site2Value=Trade(id=2, cusip=HON, shares=83, price=227.96)
Values are not equal for key=3; site1Value=null; site2Value=Trade(id=3, cusip=NKE, shares=39, price=341.00)
Values are not equal for key=4; site1Value=null; site2Value=Trade(id=4, cusip=BUD, shares=29, price=942.74)
Values are not equal for key=5; site1Value=null; site2Value=Trade(id=5, cusip=TXN, shares=55, price=382.56)
Values are not equal for key=6; site1Value=null; site2Value=Trade(id=6, cusip=UNH, shares=9, price=388.62)
Values are not equal for key=7; site1Value=null; site2Value=Trade(id=7, cusip=PG, shares=43, price=490.53)
Values are not equal for key=8; site1Value=null; site2Value=Trade(id=8, cusip=NFLX, shares=55, price=914.49)
Values are not equal for key=9; site1Value=null; site2Value=Trade(id=9, cusip=TXN, shares=42, price=289.76)
Values are not equal for key=10; site1Value=null; site2Value=Trade(id=10, cusip=BUD, shares=58, price=176.86)
```
### Clear Region
Clear the region in both sites using the `runclient.sh` script like in Scenario 1.
## Scenario 4
This scenario:

- pauses GatewaySender in site 1
- loads entries into the site 1 Region which are not replicated to site 2

In this scenario, neither the keys nor the values are equal.
### Pause GatewaySender
Pause the GatewaySender in site 1 using the *pausegatewaysender.sh* script like:

```
./pausegatewaysender.sh 

(1) Executing - connect --locator=localhost[10331]

Connecting to Locator at [host=localhost, port=10331] ..
Connecting to Manager at [host=xxx.xxx.x.x, port=1091] ..
Successfully connected to: [host=xxx.xxx.x.x, port=1091]


(2) Executing - pause gateway-sender --id=ny

                 Member                  | Result | Message
---------------------------------------- | ------ | -----------------------------------------------------------------------------
xxx.xxx.x.x(server-ln-1:96598)<v1>:41001 | OK     | GatewaySender ny is paused on member xxx.xxx.x.x(server-ln-1:96598)<v1>:41001
xxx.xxx.x.x(server-ln-2:96613)<v2>:41002 | OK     | GatewaySender ny is paused on member xxx.xxx.x.x(server-ln-2:96613)<v2>:41002
```
### Load Entries
Load entries using the *runclient.sh* script like below.

The parameters are:

- operation (run-scenario-4)
- region name (Trade)

```
./runclient.sh run-scenario-4 Trade

> Task :bootRun

2020-06-09 14:30:10.601  INFO 99767 --- [           main] example.client.Client           : Starting Client on ...
...
2020-06-09 14:30:12.366  INFO 99767 --- [           main] example.client.Client           : Started Client in 2.075 seconds (JVM running for 2.409)
2020-06-09 14:30:12.367  INFO 99767 --- [           main] example.client.Client           : Putting into region=Trade, site=site1; numEntries=20
2020-06-09 14:30:12.456  INFO 99767 --- [           main] example.client.Client           :    key=0, value=Trade(id=0, cusip=NFLX, shares=38, price=858.77)
2020-06-09 14:30:12.459  INFO 99767 --- [           main] example.client.Client           :    key=1, value=Trade(id=1, cusip=NKE, shares=82, price=182.93)
2020-06-09 14:30:12.462  INFO 99767 --- [           main] example.client.Client           :    key=2, value=Trade(id=2, cusip=PFE, shares=62, price=673.33)
2020-06-09 14:30:12.465  INFO 99767 --- [           main] example.client.Client           :    key=3, value=Trade(id=3, cusip=BUD, shares=30, price=80.78)
2020-06-09 14:30:12.467  INFO 99767 --- [           main] example.client.Client           :    key=4, value=Trade(id=4, cusip=WMT, shares=61, price=43.73)
2020-06-09 14:30:12.469  INFO 99767 --- [           main] example.client.Client           :    key=5, value=Trade(id=5, cusip=SAP, shares=31, price=275.28)
2020-06-09 14:30:12.471  INFO 99767 --- [           main] example.client.Client           :    key=6, value=Trade(id=6, cusip=MRK, shares=74, price=708.56)
2020-06-09 14:30:12.473  INFO 99767 --- [           main] example.client.Client           :    key=7, value=Trade(id=7, cusip=CMCSA, shares=72, price=608.54)
2020-06-09 14:30:12.475  INFO 99767 --- [           main] example.client.Client           :    key=8, value=Trade(id=8, cusip=LMT, shares=60, price=928.63)
2020-06-09 14:30:12.477  INFO 99767 --- [           main] example.client.Client           :    key=9, value=Trade(id=9, cusip=AVGO, shares=17, price=966.19)
2020-06-09 14:30:12.479  INFO 99767 --- [           main] example.client.Client           :    key=10, value=Trade(id=10, cusip=UNH, shares=63, price=191.01)
2020-06-09 14:30:12.481  INFO 99767 --- [           main] example.client.Client           :    key=11, value=Trade(id=11, cusip=NKE, shares=2, price=57.57)
2020-06-09 14:30:12.483  INFO 99767 --- [           main] example.client.Client           :    key=12, value=Trade(id=12, cusip=MMM, shares=64, price=101.69)
2020-06-09 14:30:12.486  INFO 99767 --- [           main] example.client.Client           :    key=13, value=Trade(id=13, cusip=AXP, shares=38, price=265.81)
2020-06-09 14:30:12.487  INFO 99767 --- [           main] example.client.Client           :    key=14, value=Trade(id=14, cusip=PEP, shares=45, price=312.95)
2020-06-09 14:30:12.490  INFO 99767 --- [           main] example.client.Client           :    key=15, value=Trade(id=15, cusip=CRM, shares=97, price=768.55)
2020-06-09 14:30:12.492  INFO 99767 --- [           main] example.client.Client           :    key=16, value=Trade(id=16, cusip=HSBC, shares=25, price=929.00)
2020-06-09 14:30:12.494  INFO 99767 --- [           main] example.client.Client           :    key=17, value=Trade(id=17, cusip=BAC, shares=90, price=531.28)
2020-06-09 14:30:12.496  INFO 99767 --- [           main] example.client.Client           :    key=18, value=Trade(id=18, cusip=XOM, shares=60, price=90.27)
2020-06-09 14:30:12.498  INFO 99767 --- [           main] example.client.Client           :    key=19, value=Trade(id=19, cusip=HSBC, shares=15, price=892.48)
```
### Verify Entries
Verify the entries in both sites using the *runclient.sh* script like below.

The parameters are:

- operation (verify-region)
- region name (Trade)

```
./runclient.sh verify-region Trade

> Task :bootRun

2020-06-09 14:31:49.653  INFO 99873 --- [           main] example.client.Client                    : Starting Client on ...
...
2020-06-09 14:31:51.333  INFO 99873 --- [           main] example.client.Client                    : Started Client in 1.998 seconds (JVM running for 2.326)
2020-06-09 14:31:51.465  INFO 99873 --- [           main] e.c.verifier.WanVerificationService      : 
Verifying entries for region=Trade

==============
Comparing keys
==============
All keys are not equal. Site 1 contains 20 keys. Site 2 contains 0 keys.
Site 1 contains these 20 keys not found in site 2: [11, 12, 13, 14, 15, 16, 17, 18, 19, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
Site 2 contains these 0 keys not found in site 1: []

=============================================
Comparing values in site 1 to those in site 2
=============================================
Values are not equal for key=11; site1Value=Trade(id=11, cusip=NKE, shares=2, price=57.57); site2Value=null
Values are not equal for key=12; site1Value=Trade(id=12, cusip=MMM, shares=64, price=101.69); site2Value=null
Values are not equal for key=13; site1Value=Trade(id=13, cusip=AXP, shares=38, price=265.81); site2Value=null
Values are not equal for key=14; site1Value=Trade(id=14, cusip=PEP, shares=45, price=312.95); site2Value=null
Values are not equal for key=15; site1Value=Trade(id=15, cusip=CRM, shares=97, price=768.55); site2Value=null
Values are not equal for key=16; site1Value=Trade(id=16, cusip=HSBC, shares=25, price=929.00); site2Value=null
Values are not equal for key=17; site1Value=Trade(id=17, cusip=BAC, shares=90, price=531.28); site2Value=null
Values are not equal for key=18; site1Value=Trade(id=18, cusip=XOM, shares=60, price=90.27); site2Value=null
Values are not equal for key=19; site1Value=Trade(id=19, cusip=HSBC, shares=15, price=892.48); site2Value=null
Values are not equal for key=0; site1Value=Trade(id=0, cusip=NFLX, shares=38, price=858.77); site2Value=null
Values are not equal for key=1; site1Value=Trade(id=1, cusip=NKE, shares=82, price=182.93); site2Value=null
Values are not equal for key=2; site1Value=Trade(id=2, cusip=PFE, shares=62, price=673.33); site2Value=null
Values are not equal for key=3; site1Value=Trade(id=3, cusip=BUD, shares=30, price=80.78); site2Value=null
Values are not equal for key=4; site1Value=Trade(id=4, cusip=WMT, shares=61, price=43.73); site2Value=null
Values are not equal for key=5; site1Value=Trade(id=5, cusip=SAP, shares=31, price=275.28); site2Value=null
Values are not equal for key=6; site1Value=Trade(id=6, cusip=MRK, shares=74, price=708.56); site2Value=null
Values are not equal for key=7; site1Value=Trade(id=7, cusip=CMCSA, shares=72, price=608.54); site2Value=null
Values are not equal for key=8; site1Value=Trade(id=8, cusip=LMT, shares=60, price=928.63); site2Value=null
Values are not equal for key=9; site1Value=Trade(id=9, cusip=AVGO, shares=17, price=966.19); site2Value=null
Values are not equal for key=10; site1Value=Trade(id=10, cusip=UNH, shares=63, price=191.01); site2Value=null

=============================================
Comparing values in site 2 to those in site 1
=============================================
All values in site 2 are equal to those in site 1
```
### Resume GatewaySender
Resume the GatewaySender in site 1 using the *resumegatewaysender.sh* like:

```
./resumegatewaysender.sh 

(1) Executing - connect --locator=localhost[10331]

Connecting to Locator at [host=localhost, port=10331] ..
Connecting to Manager at [host=xxx.xxx.x.x, port=1091] ..
Successfully connected to: [host=xxx.xxx.x.x, port=1091]


(2) Executing - resume gateway-sender --id=ny

                 Member                  | Result | Message
---------------------------------------- | ------ | ------------------------------------------------------------------------------
xxx.xxx.x.x(server-ln-1:96598)<v1>:41001 | OK     | GatewaySender ny is resumed on member xxx.xxx.x.x(server-ln-1:96598)<v1>:41001
xxx.xxx.x.x(server-ln-2:96613)<v2>:41002 | OK     | GatewaySender ny is resumed on member xxx.xxx.x.x(server-ln-2:96613)<v2>:41002
```

### Clear Region
Clear the region in both sites using the `runclient.sh` script like in Scenario 1.
## Scenario 5
This scenario:

- pauses GatewaySender in site 1
- loads entries into the site 1 Region which are not replicated to site 2
- loads other entries into the site 2 Region which are not replicated to site 1

In this scenario, neither the keys nor the values are equal.
### Pause GatewaySender
Pause the GatewaySender using the `pausegatewaysender.sh` script like in Scenario 4.

### Load Entries
Load entries using the *runclient.sh* script like below.

The parameters are:

- operation (run-scenario-5)
- region name (Trade)

```
./runclient.sh run-scenario-5 Trade

> Task :bootRun

2020-06-09 14:43:23.040  INFO 932 --- [           main] example.client.Client           : Starting Client on ...
...
2020-06-09 14:43:24.793  INFO 932 --- [           main] example.client.Client           : Started Client in 2.065 seconds (JVM running for 2.395)
2020-06-09 14:43:24.794  INFO 932 --- [           main] example.client.Client           : Putting into region=Trade, site=site1; numEntries=20
2020-06-09 14:43:24.868  INFO 932 --- [           main] example.client.Client           :      key=0, value=Trade(id=0, cusip=CRM, shares=44, price=921.92)
2020-06-09 14:43:24.870  INFO 932 --- [           main] example.client.Client           :      key=2, value=Trade(id=2, cusip=BAC, shares=78, price=939.22)
2020-06-09 14:43:24.872  INFO 932 --- [           main] example.client.Client           :      key=4, value=Trade(id=4, cusip=V, shares=96, price=242.98)
2020-06-09 14:43:24.877  INFO 932 --- [           main] example.client.Client           :      key=6, value=Trade(id=6, cusip=WMT, shares=0, price=651.09)
2020-06-09 14:43:24.879  INFO 932 --- [           main] example.client.Client           :      key=8, value=Trade(id=8, cusip=VZ, shares=32, price=271.64)
2020-06-09 14:43:24.882  INFO 932 --- [           main] example.client.Client           :      key=10, value=Trade(id=10, cusip=CVS, shares=63, price=168.13)
2020-06-09 14:43:24.884  INFO 932 --- [           main] example.client.Client           :      key=12, value=Trade(id=12, cusip=UNH, shares=56, price=846.28)
2020-06-09 14:43:24.889  INFO 932 --- [           main] example.client.Client           :      key=14, value=Trade(id=14, cusip=TM, shares=34, price=708.74)
2020-06-09 14:43:24.891  INFO 932 --- [           main] example.client.Client           :      key=16, value=Trade(id=16, cusip=WMT, shares=77, price=576.58)
2020-06-09 14:43:24.894  INFO 932 --- [           main] example.client.Client           :      key=18, value=Trade(id=18, cusip=AAPL, shares=47, price=590.66)
2020-06-09 14:43:24.895  INFO 932 --- [           main] example.client.Client           : Putting into region=Trade, site=site2; numEntries=20
2020-06-09 14:43:24.900  INFO 932 --- [           main] example.client.Client           :      key=1, value=Trade(id=1, cusip=CRM, shares=81, price=796.38)
2020-06-09 14:43:24.901  INFO 932 --- [           main] example.client.Client           :      key=3, value=Trade(id=3, cusip=FB, shares=33, price=64.32)
2020-06-09 14:43:24.903  INFO 932 --- [           main] example.client.Client           :      key=5, value=Trade(id=5, cusip=SBUX, shares=20, price=567.32)
2020-06-09 14:43:24.904  INFO 932 --- [           main] example.client.Client           :      key=7, value=Trade(id=7, cusip=SBUX, shares=77, price=67.04)
2020-06-09 14:43:24.906  INFO 932 --- [           main] example.client.Client           :      key=9, value=Trade(id=9, cusip=PFE, shares=58, price=135.06)
2020-06-09 14:43:24.909  INFO 932 --- [           main] example.client.Client           :      key=11, value=Trade(id=11, cusip=PFE, shares=57, price=85.83)
2020-06-09 14:43:24.911  INFO 932 --- [           main] example.client.Client           :      key=13, value=Trade(id=13, cusip=INTC, shares=75, price=412.98)
2020-06-09 14:43:24.914  INFO 932 --- [           main] example.client.Client           :      key=15, value=Trade(id=15, cusip=CVS, shares=18, price=893.04)
2020-06-09 14:43:24.916  INFO 932 --- [           main] example.client.Client           :      key=17, value=Trade(id=17, cusip=C, shares=64, price=84.74)
2020-06-09 14:43:24.917  INFO 932 --- [           main] example.client.Client           :      key=19, value=Trade(id=19, cusip=JNJ, shares=84, price=377.13)
```
### Verify Entries
Verify the entries in both sites using the *runclient.sh* script like below.

The parameters are:

- operation (verify-region)
- region name (Trade)

```
./runclient.sh verify-region Trade

> Task :bootRun

2020-06-09 14:45:25.203  INFO 1055 --- [           main] example.client.Client                    : Starting Client on ...
...
2020-06-09 14:45:26.986  INFO 1055 --- [           main] example.client.Client                    : Started Client in 2.089 seconds (JVM running for 2.414)
2020-06-09 14:45:27.120  INFO 1055 --- [           main] e.c.verifier.WanVerificationService      : 
Verifying entries for region=Trade

==============
Comparing keys
==============
All keys are not equal. Site 1 contains 10 keys. Site 2 contains 10 keys.
Site 1 contains these 10 keys not found in site 2: [0, 12, 2, 14, 4, 16, 6, 18, 8, 10]
Site 2 contains these 10 keys not found in site 1: [11, 1, 13, 3, 15, 5, 17, 7, 19, 9]

=============================================
Comparing values in site 1 to those in site 2
=============================================
Values are not equal for key=0; site1Value=Trade(id=0, cusip=CRM, shares=44, price=921.92); site2Value=null
Values are not equal for key=12; site1Value=Trade(id=12, cusip=UNH, shares=56, price=846.28); site2Value=null
Values are not equal for key=2; site1Value=Trade(id=2, cusip=BAC, shares=78, price=939.22); site2Value=null
Values are not equal for key=14; site1Value=Trade(id=14, cusip=TM, shares=34, price=708.74); site2Value=null
Values are not equal for key=4; site1Value=Trade(id=4, cusip=V, shares=96, price=242.98); site2Value=null
Values are not equal for key=16; site1Value=Trade(id=16, cusip=WMT, shares=77, price=576.58); site2Value=null
Values are not equal for key=6; site1Value=Trade(id=6, cusip=WMT, shares=0, price=651.09); site2Value=null
Values are not equal for key=18; site1Value=Trade(id=18, cusip=AAPL, shares=47, price=590.66); site2Value=null
Values are not equal for key=8; site1Value=Trade(id=8, cusip=VZ, shares=32, price=271.64); site2Value=null
Values are not equal for key=10; site1Value=Trade(id=10, cusip=CVS, shares=63, price=168.13); site2Value=null

=============================================
Comparing values in site 2 to those in site 1
=============================================
Values are not equal for key=11; site1Value=null; site2Value=Trade(id=11, cusip=PFE, shares=57, price=85.83)
Values are not equal for key=1; site1Value=null; site2Value=Trade(id=1, cusip=CRM, shares=81, price=796.38)
Values are not equal for key=13; site1Value=null; site2Value=Trade(id=13, cusip=INTC, shares=75, price=412.98)
Values are not equal for key=3; site1Value=null; site2Value=Trade(id=3, cusip=FB, shares=33, price=64.32)
Values are not equal for key=15; site1Value=null; site2Value=Trade(id=15, cusip=CVS, shares=18, price=893.04)
Values are not equal for key=5; site1Value=null; site2Value=Trade(id=5, cusip=SBUX, shares=20, price=567.32)
Values are not equal for key=17; site1Value=null; site2Value=Trade(id=17, cusip=C, shares=64, price=84.74)
Values are not equal for key=7; site1Value=null; site2Value=Trade(id=7, cusip=SBUX, shares=77, price=67.04)
Values are not equal for key=19; site1Value=null; site2Value=Trade(id=19, cusip=JNJ, shares=84, price=377.13)
Values are not equal for key=9; site1Value=null; site2Value=Trade(id=9, cusip=PFE, shares=58, price=135.06)
```
### Resume GatewaySender
Resume the GatewaySender using the `resumegatewaysender.sh` script like in Scenario 4.
### Clear Region
Clear the region in both sites using the `runclient.sh` script like in Scenario 1.

## Shutdown Locator and Servers in Both Sites
Shutdown the locator and servers in both sites using the *shutdownall.sh* script like:

```
./shutdownsites.sh 

(1) Executing - connect --locator=localhost[10331]

Connecting to Locator at [host=localhost, port=10331] ..
Connecting to Manager at [host=xxx.xxx.x.x, port=1091] ..
Successfully connected to: [host=xxx.xxx.x.x, port=1091]


(2) Executing - shutdown --include-locators=true

Shutdown is triggered


(1) Executing - connect --locator=localhost[10332]

Connecting to Locator at [host=localhost, port=10332] ..
Connecting to Manager at [host=xxx.xxx.x.x, port=1092] ..
Successfully connected to: [host=xxx.xxx.x.x, port=1092]


(2) Executing - shutdown --include-locators=true

Shutdown is triggered
```
### Remove Locator and Server Files in Both Sites
Remove the server and locator files in both sites using the *cleanupfiles.sh* script like:

```
./cleanupfiles.sh
```
