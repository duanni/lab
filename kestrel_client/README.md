Running the Tests
----

    mvn test

Test Environment
----

- software
    - kestrel
        - version 2.1.1-SNAPSHOT
        - config https://gist.github.com/1121929
        - JVM args : default (kestrel.sh)
    - Java -version (server & client)
        - java version "1.6.0_24"
        - Java(TM) SE Runtime Environment (build 1.6.0_24-b07)
        - Java HotSpot(TM) 64-Bit Server VM (build 19.1-b02, mixed mode)
- hardware
    - server
        - CPU: 4 * Intel(R) Core(TM) i5 CPU 760  @ 2.80GHz
        - O/S: Linux 2.6.32-33-server #71-Ubuntu SMP x86_64 GNU/Linux
        - RAM: 16 GiB
        - NIC: Intel Corporation 82578DC Gigabit Network Connection
    - client
        - CPU: 4 * Intel(R) Core(TM) i5 CPU 760  @ 2.80GHz
        - O/S: Linux 2.6.32-31-server #61-Ubuntu SMP x86_64 GNU/Linux
        - RAM: 16 GiB
        - NIC: Intel Corporation 82578DC Gigabit Network Connection

Test Result
----

    ======================================================================
    threads      valueLength      tps/s      times(s)
    10           64               7787       64.21
    10           128              7959       62.82
    10           512              7239       69.07
    10           1024             6335       78.93
    10           4096             4271       117.06
    10           16384            1874       266.83
    30           64               9602       52.07
    30           128              13237      37.77
    30           512              11696      42.75
    30           1024             9913       50.44
    30           4096             5608       89.15
    30           16384            2354       212.39
    50           64               9478       52.75
    50           128              12377      40.40
    50           512              10916      45.80
    50           1024             9511       52.57
    50           4096             5428       92.12
    50           16384            2292       218.11
    100          64               10152      49.25
    100          128              13558      36.88
    100          512              12050      41.49
    100          1024             10166      49.18
    100          4096             5674       88.12
    100          16384            2386       209.60
    200          64               10404      48.06
    200          128              14160      35.31
    200          512              12552      39.84
    200          1024             10725      46.62
    200          4096             5795       86.28
    200          16384            2426       206.08
    300          64               10263      48.70
    300          128              13876      36.02
    300          512              12301      40.63
    300          1024             10369      48.20
    300          4096             5762       86.75
    300          16384            2412       207.17
