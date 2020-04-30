#!/usr/bin/env bash

echo
echo "=================================================="
echo "set xrange [25000:75000]"
echo "set yrange [25000:75000]"
echo "set ticslevel 0         "
echo "=================================================="
echo 'splot "zauberons.dat" u 1:2:3 with dots'
echo "=================================================="
echo "Enter 'quit<Enter>' To Exit gunplot"
echo "=================================================="
echo
echo
gnuplot
exit 0
