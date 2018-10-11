To reset the git repository on distant server
---------------------------------------------

git reset --hard HEAD
git clean -f -d
git pull

To pull repository from distant server
--------------------------------------

scp -r fouchee@vsim01.ipd.kit.edu:/home/i40/fouchee/git/MCDE/REPOSITORY  .

Start an experiment
-------------------

nohup sbt "run com.edouardfouche.experiments.EXPERIMENT"

Copy the jar to the distant server
----------------------------------

scp /home/fouchee/git/MCDE/target/scala-2.11/MCDE.jar fouchee@i40sim02.ipd.kit.edu:/home/i40/fouchee/experiments/

Copy some data
--------------

scp -r /home/fouchee/data/DATA fouchee@i40sim04.ipd.kit.edu:/home/i40/fouchee/data/

Restrict to some cores
----------------------

nohup taskset -c 0,1,2,3,4,5,6,7


// A few useful JVM options:
// -Xms1024M -Xmx2048M
// Xms - for minimum limit
// Xmx - for maximum limit
// -XX:-UseGCOverheadLimit
// -Xms1024M -Xmx3G
// -Xmx2048M -Xss128M -XX:MaxPermSize=2048M -XX:+CMSClassUnloadingEnabled -XX:+UseConcMarkSweepGC