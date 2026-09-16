#=============================================================================
# Copyright (c) 2024 Qualcomm Technologies, Inc.
# All Rights Reserved.
# Confidential and Proprietary - Qualcomm Technologies, Inc.
#=============================================================================

get_num_logical_cores_in_physical_cluster()
{
	i=0
	logical_cores=(0 0 0 0 0 0)
	if [ -f /sys/devices/system/cpu/cpu0/topology/cluster_id ] ; then
		physical_cluster="cluster_id"
	else
		physical_cluster="physical_package_id"
	fi
	for i in `ls -d /sys/devices/system/cpu/cpufreq/policy[0-9]*`
	do
		if [ -e $i ] ; then
			num_cores=$(cat $i/related_cpus | wc -w)
			first_cpu=$(echo "$i" | sed 's/[^0-9]*//g')
			cluster_id=$(cat /sys/devices/system/cpu/cpu$first_cpu/topology/$physical_cluster)
			logical_cores[cluster_id]=$num_cores
		fi
	done
	cpu_topology=""
	j=0
	physical_cluster_count=$1
	while [[ $j -lt $physical_cluster_count ]]; do
		cpu_topology+=${logical_cores[$j]}
		if [ $j -lt $physical_cluster_count-1 ]; then
			cpu_topology+="_"
		fi
		j=$((j+1))
	done
	echo $cpu_topology
}

variant=$(get_num_logical_cores_in_physical_cluster "$1")
echo "CPU topology: ${variant}"
case "$variant" in
	"4_3_1")
	/vendor/bin/sh /vendor/bin/init.kernel.post_boot-volcano_default_4_3_1.sh
	;;
	"4_2_1")
	/vendor/bin/sh /vendor/bin/init.kernel.post_boot-volcano_4_2_1.sh
	;;
	"4_3_0")
	/vendor/bin/sh /vendor/bin/init.kernel.post_boot-volcano_4_3_0.sh
	;;
	*)
	echo "***WARNING***: Defaulting to 4_3_1 variant"
	/vendor/bin/sh /vendor/bin/init.kernel.post_boot-volcano_default_4_3_1.sh
	;;
esac

setprop vendor.post_boot.parsed 1
