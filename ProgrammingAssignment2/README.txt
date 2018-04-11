To run this program I used the virtual machines you provided. 

I run DVCoordinator using the following command:

java DVCoordinator 11610 nodeInfoTest.txt <------------------------------------------------------------------------------ RUN DVCoordinator
									or
java DVCoordinator 11610 nodeInfo.txt	  <------------------------------------------------------------------------------ RUN DVCoordinator

11610 is my port number.
nodeInfoTest.txt is my own map that I made with three nodes for testing purposed.
nodeInfo.txt is the map on the assignment.

Once DVCoordinator is ran, it provided an IP address from amazon. 
If you run dvCoordinator in the virtual machines you provided us, it will take that address, 
if you run it on your computer it will need your actual ip through ipconfig.

i use dvCoordinator's ip to run DVNode by using the following command

java DVNode <coordinatorIP> 11610  <------------------------------------------------------------------------------RUN DVNODE

where 11610 is the coordinator (and the node's) port number.


Unfortunately I haven't had the time to comment my code but I am confident I can explain it to you if you wish. I need to work on commenting my code.

DVCoordinator sends every DVNode it's dv and its neighborIPTable.
Each node then sends its own DVNode to its neighbor through multicast (I finally understand it!).

If there is anything else you would like from me please let me know. While I struggled alot with this assignment, I am glad that I was able to work on it.
